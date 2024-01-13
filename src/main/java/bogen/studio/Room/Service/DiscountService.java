package bogen.studio.Room.Service;

import bogen.studio.Room.DTO.DiscountPostDto;
import bogen.studio.Room.DTO.TripInfo;
import bogen.studio.Room.Enums.DiscountExecution;
import bogen.studio.Room.Enums.DiscountPlace;
import bogen.studio.Room.Enums.DiscountType;
import bogen.studio.Room.Exception.InvalidInputException;
import bogen.studio.Room.Models.*;
import bogen.studio.Room.Repository.DiscountRepository;
import bogen.studio.Room.documents.City;
import bogen.studio.Room.documents.Discount;
import bogen.studio.Room.documents.Place;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static bogen.studio.Room.Enums.DiscountType.CODE;
import static bogen.studio.Room.Routes.Utility.getUserId;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscountService {

    private final DiscountRepository discountRepository;
    private final InsertDiscountService insertDiscountService;
    private final CalculateDiscountService calculateDiscountService;
    private final MongoTemplate mongoTemplate;
    private final BoomService boomService;
    private final PlaceService placeService;
    private final CityService cityService;
    private final DiscountReportValidatorService discountReportValidatorService;

    public Discount insert(DiscountPostDto dto, Principal principal) {
        /* Create Discount doc and insert it into DB */

        // Create discountPlace, boomId, DiscountPlaceInfo, and DiscountType from DTO
        DiscountPlace discountPlace = DiscountPlace.valueOf(dto.getDiscountPlace());
        ObjectId boomId = new ObjectId(dto.getDiscountPlaceInfoPostDto().getBoomId());
        Boom boom = boomService.findById(boomId);
        Place place = placeService.fetchById(boom.getPlaceId());
        City city = cityService.fetchCityById(place.getCityId());
        DiscountPlaceInfo discountPlaceInfo = new DiscountPlaceInfo(
                boomId.toString(),
                dto.getDiscountPlaceInfoPostDto().getRoomName(),
                place.getName(),
                city.getName(),
                city.getState()
        );
        DiscountType discountType = DiscountType.valueOf(dto.getDiscountType());

        // Check the integrity of boomId roomName, and discount code uniqueness;
        insertDiscountService.checkBoomIdExistence(boomId);
        insertDiscountService.checkRoomExistence(discountPlace, boomId, discountPlaceInfo.getRoomName());
        insertDiscountService.checkCodeUniqueness(discountType, boomId, dto);

        // Check the user to be the owner of the boom or the system admin
        insertDiscountService.isUserAllowedToCreateDiscount(boomId, principal);

        // Instantiate discount
        Discount discount = new Discount()
                .setDiscountPlace(discountPlace)
                .setDiscountPlaceInfo(discountPlaceInfo)
                .setDiscountType(discountType)
                .setCreatedBy(getUserId(principal).toString());

        // According to input discountType set general or lastMinute or code discount
        insertDiscountService.setGeneralOrLastMinuteOrCodeDiscount(discountType, discount, dto);

        // Insert to DB
        return discountRepository.insert(discount);
    }


    public void addDiscountInfoToRoomSearchResult(
            JSONArray modifiedSearchResult,
            ObjectId boomId,
            TripInfo tripInfo,
            String discountCode) {
        /* Add discount info to rooms data */

        if (tripInfo == null) {
            return;
        }

        // Build list of Gregorian dates based on tripInfo
        List<LocalDateTime> stayingDatesInGregorian = calculateDiscountService.buildGregorianStayingDates(tripInfo);

        // Loop over rooms
        for (int i = 0; i < modifiedSearchResult.length(); i++) {

            // Get Room data
            JSONObject roomData = modifiedSearchResult.getJSONObject(i);

            // Build JSONObject, including discount info for each staying date
            JSONObject roomDiscountInfo = buildRoomDiscountInfoJsonObject(
                    roomData.getString("title"),
                    roomData.getLong("price"),
                    roomData.getLong("totalPrice"),
                    boomId,
                    stayingDatesInGregorian,
                    discountCode);

            // Add roomDiscountInfo to roomData
            roomData.put("roomDiscountInfo", roomDiscountInfo);
        }
    }

    public JSONObject buildRoomDiscountInfoJsonObject(
            String roomName,
            Long nightPrice,
            Long totalPrice, // totalAmount
            ObjectId boomId,
            List<LocalDateTime> stayingDatesInGregorian,
            String discountCode
    ) {
        // Calculate discount for each target date
        DiscountInfo discountInfo = buildDiscountInfo(
                roomName,
                nightPrice,
                totalPrice,
                boomId,
                stayingDatesInGregorian,
                discountCode
        );

        // Instantiate JSON Object that will be added to Room data
        JSONArray targetDateDiscountDetails = new JSONArray();

        // Build JSON Object for discount-info-detail of each target date
        for (TargetDateDiscountDetail info : discountInfo.getTargetDateDiscountDetails()) {

            // Build target-date-discount-info
            JSONObject targetDateDiscountInfo = calculateDiscountService.createTargetDateDiscountInfoJSONObject(info);
            // Add target-date-discount-info to targetDateDiscountDetails array
            targetDateDiscountDetails.put(targetDateDiscountInfo);
        }

        return new JSONObject()
                .put("totalDiscount", discountInfo.getTotalDiscount())
                .put("targetDateDiscountDetails", targetDateDiscountDetails)
                .put("isDiscountCodeApplied", discountInfo.isDiscountCodeApplied());
    }

    public DiscountInfo buildDiscountInfo(
            String roomName,
            Long nightPrice,
            Long totalPrice,
            ObjectId boomId,
            List<LocalDateTime> stayingDatesInGregorian,
            String discountCode
    ) {
        /* Build discountInfo for ReservationRequest Doc. */

        // If there is a discount code
        if (discountCode != null) {

            return calculateDiscountService.buildDiscountInfoForCodeDiscount(
                    boomId,
                    discountCode,
                    stayingDatesInGregorian,
                    roomName,
                    nightPrice
            );
        }

        return calculateDiscountService.buildDiscountInfoForGeneralAndLastMinuteDiscount(
                stayingDatesInGregorian,
                boomId,
                roomName,
                nightPrice,
                totalPrice
        );
    }

    public void incrementCurrentUsageCountForCodeDiscount(ReservationRequest request) {

        if (request.getDiscountInfo().isDiscountCodeApplied()) {

            String discountId = request.getDiscountInfo().getTargetDateDiscountDetails().get(0).getDiscountId();

            Criteria typeCriteria = Criteria.where("discount_type").is(CODE);
            Criteria idCriteria = Criteria.where("_id").is(discountId);
            Criteria searchCriteria = new Criteria().andOperator(typeCriteria, idCriteria);

            Query query = new Query().addCriteria(searchCriteria);

            Update update = new Update().inc("code_discount.current_usage_count", 1);

            mongoTemplate.findAndModify(
                    query,
                    update,
                    Discount.class,
                    mongoTemplate.getCollectionName(Discount.class)
            );
        }
    }

    public Discount fetchDiscountById(String discountId) {

        return discountRepository.fetchDiscountById(discountId);
    }

    public void validateSearchFields(
            Optional<DiscountPlace> discountPlaceOptional,
            Optional<String> boomNameOptional,
            Optional<String> roomNameOptional,
            Optional<String> cityOptional,
            Optional<String> provinceOptional,
            Optional<LocalDateTime> createdDateOptional,
            Optional<LocalDateTime> lifeTimeStartOptional,
            Optional<LocalDateTime> lifeTimeEndOptional,
            Optional<LocalDateTime> targetDateStartOptional,
            Optional<LocalDateTime> targetDateEndOptional,
            Optional<DiscountExecution> discountExecutionOptional,
            Optional<Integer> discountAmountMinOptional,
            Optional<Integer> discountAmountMaxOptional,
            Optional<Integer> discountPercentMinOptional,
            Optional<Integer> discountPercentMaxOptional
    ) {

        StringBuffer sb = new StringBuffer();

        boolean haseDiscountPlaceAndBoomAndRoomAnyError = hasDiscountPlaceAndBoomAndRoomAnyError(
                discountPlaceOptional,
                boomNameOptional,
                roomNameOptional,
                sb
        );
        boolean hasBoomNameAnyError = discountReportValidatorService.hasPlaceNameAnyError(boomNameOptional, sb, "نام اقامت گاه");
        boolean hasRoomNameAnyError = discountReportValidatorService.hasPlaceNameAnyError(roomNameOptional, sb, "نام اتاق");
        boolean hasCityNameAnyError = discountReportValidatorService.hasPlaceNameAnyError(cityOptional, sb, "نام شهر");
        boolean hasProvinceNameAnyError = discountReportValidatorService.hasPlaceNameAnyError(provinceOptional, sb, "نام استان");
        boolean hasCreatedDateAnyError = discountReportValidatorService.hasDateAnyError(createdDateOptional, sb, "تاریخ ایجاد");
        boolean hasLifeTimeStartAnyError = discountReportValidatorService.hasDateAnyError(lifeTimeStartOptional, sb, "زمان شروع چرخه عمر");
        boolean hasLifeTimeEndAnyError = discountReportValidatorService.hasDateAnyError(lifeTimeEndOptional, sb, "زمان پایان چرخه عمر");
        boolean hasTargetDateStartAnyError = discountReportValidatorService.hasDateAnyError(targetDateStartOptional, sb, "زمان شروع تاریخ هدف");
        boolean hasTargetDateEndAnyError = discountReportValidatorService.hasDateAnyError(targetDateEndOptional, sb, "زمان پایان تاریخ هدف");
        boolean hasLifeTimeScopeAnyError = hasTimeScopeAnyError(lifeTimeStartOptional, lifeTimeEndOptional, sb, "چرخه عمر");
        boolean hasTargetDateScopeAnyError = hasTimeScopeAnyError(targetDateStartOptional, targetDateEndOptional, sb, "تاریخ هدف");
        boolean hasDiscountExecutionAmountAndPercentAnyError =
                discountReportValidatorService.hasDiscountExecutionAmountPercentAnyError(
                        discountExecutionOptional,
                        discountAmountMinOptional,
                        discountAmountMaxOptional,
                        discountPercentMinOptional,
                        discountPercentMaxOptional,
                        sb);

        if (haseDiscountPlaceAndBoomAndRoomAnyError || hasBoomNameAnyError || hasRoomNameAnyError ||
                hasCityNameAnyError || hasProvinceNameAnyError || hasCreatedDateAnyError || hasLifeTimeStartAnyError ||
                hasLifeTimeEndAnyError || hasTargetDateStartAnyError || hasTargetDateEndAnyError ||
                hasLifeTimeScopeAnyError || hasTargetDateScopeAnyError || hasDiscountExecutionAmountAndPercentAnyError
        ) {
            String errorMsg = sb.toString().replace("\n", ",");
            log.error("Error in Validating discount search fields: " + errorMsg);
            throw new InvalidInputException(sb.toString());
        }
    }

    private boolean hasDiscountPlaceAndBoomAndRoomAnyError(
            Optional<DiscountPlace> discountPlaceOptional,
            Optional<String> boomNameOptional,
            Optional<String> roomNameOptional,
            StringBuffer sb
    ) {

        boolean hasError = false;

        if (
                discountPlaceOptional.isEmpty() &&
                        (boomNameOptional.isPresent() || roomNameOptional.isPresent())
        ) {
            hasError = true;
            sb.append("در صورت تهی بودن مکان تخفیف، نام اقامت گاه و نام اتاق هم باید تهی باشند");
            sb.append("\n");
        }

        if (discountPlaceOptional.isPresent()) {

            if (
                    discountPlaceOptional.get().equals(DiscountPlace.BOOM_DISCOUNT) &&
                            roomNameOptional.isPresent()
            ) {
                hasError = true;
                sb.append("در صورتیکه مکان تخفیف، اقامتگاه باشد، نام اتاق باید تهی باشد");
                sb.append("\n");
            }

        }


        return hasError;
    }

    private boolean hasTimeScopeAnyError(Optional<LocalDateTime> start, Optional<LocalDateTime> end, StringBuffer sb, String scopeName) {

        boolean hasError = false;

        if (start.isEmpty() && end.isEmpty()) {
            return false;
        } else if (start.isPresent() && end.isPresent()) {

            if (end.get().isBefore(start.get())) {
                hasError = true;
                sb.append("بازه " + scopeName + " صحیح نیست");
                sb.append("\n");
            }

        } else {
            hasError = true;
            sb.append("بازه " + scopeName + " به طور کامل وارد نشده است");
            sb.append("\n");
        }


        return hasError;
    }
}
