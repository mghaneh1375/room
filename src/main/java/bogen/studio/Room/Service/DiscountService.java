package bogen.studio.Room.Service;

import bogen.studio.Room.DTO.DiscountPostDto;
import bogen.studio.Room.DTO.TripInfo;
import bogen.studio.Room.Enums.DiscountPlace;
import bogen.studio.Room.Enums.DiscountType;
import bogen.studio.Room.Models.DiscountInfo;
import bogen.studio.Room.Models.TargetDateDiscountDetail;
import bogen.studio.Room.Models.DiscountPlaceInfo;
import bogen.studio.Room.Repository.DiscountRepository;
import bogen.studio.Room.Utility.TimeUtility;
import bogen.studio.Room.documents.Discount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static bogen.studio.Room.Routes.Utility.getUserId;
import static my.common.commonkoochita.Utility.Utility.getPast;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscountService {

    private final DiscountRepository discountRepository;
    private final InsertDiscountService insertDiscountService;
    private final CalculateDiscountService calculateDiscountService;

    public Discount insert(DiscountPostDto dto, Principal principal) {
        /* Create Discount doc and insert it into DB */

        // Create discountPlace, boomId, DiscountPlaceInfo, and DiscountType from DTO
        DiscountPlace discountPlace = DiscountPlace.valueOf(dto.getDiscountPlace());
        ObjectId boomId = new ObjectId(dto.getDiscountPlaceInfoPostDto().getBoomId());
        DiscountPlaceInfo discountPlaceInfo = new DiscountPlaceInfo(
                boomId,
                dto.getDiscountPlaceInfoPostDto().getRoomName());
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
                .setCreatedBy(getUserId(principal));

        // According to input discountType set general or lastMinute or code discount
        insertDiscountService.setGeneralOrLastMinuteOrCodeDiscount(discountType, discount, dto);

        // Insert to DB
        return discountRepository.insert(discount);
    }

    public TargetDateDiscountDetail getMaximumDiscountForTargetDate(
            ObjectId boomId,
            String roomName,
            LocalDateTime targetDate,
            int nightOrdinalNumber,
            Long nightPrice,
            Long totalAmount) {
        /* Find related discounts, then return the one with maximum discount amount */

        // Fetch related discounts from db
        List<Discount> discounts = calculateDiscountService
                .fetchDefinedDiscountsForTargetDate(boomId, roomName, targetDate);

        // Calculate discount-amount for fetched discounts
        List<TargetDateDiscountDetail> targetDateDiscountDetailList =
                calculateDiscountService.
                        calculateDiscountAmountForFetchedDiscounts(
                                discounts,
                                nightOrdinalNumber,
                                nightPrice,
                                totalAmount,
                                targetDate);

        // Find maximum discount amount
        return calculateDiscountService
                .findMaximumDiscountAmount(targetDateDiscountDetailList, targetDate);
    }

    public void addDiscountInfoToRoomSearchResult(JSONArray modifiedSearchResult, ObjectId boomId, TripInfo tripInfo) {
        /* Add discount info to rooms data */

        if (tripInfo == null) {
            return;
        }

        // Build list of Gregorian dates based on tripInfo
        List<LocalDateTime> stayingDatesInGregorian = buildGregorianStayingDates(tripInfo);

        // Loop over rooms
        for (int i = 0; i < modifiedSearchResult.length(); i++) {

            // Get Room data
            JSONObject roomData = modifiedSearchResult.getJSONObject(i);

            // Build JSONObject, including discount info for each staying date
            JSONArray roomDiscountInfo = buildRoomDiscountInfoJsonArray(
                    roomData.getString("title"),
                    roomData.getLong("price"),
                    roomData.getLong("totalPrice"),
                    boomId,
                    stayingDatesInGregorian);

            // Add roomDiscountInfo to roomData
            roomData.put("roomDiscountInfo", roomDiscountInfo);
        }
    }

    public JSONArray buildRoomDiscountInfoJsonArray(
            String roomName,
            Long nightPrice,
            Long totalPrice, // totalAmount
            ObjectId boomId,
            List<LocalDateTime> stayingDatesInGregorian
    ) {

        Long totalDiscount = 0L;

        // Instantiate JSON Object that will be added to Room data
        JSONArray targetDateDiscountDetails = new JSONArray();

        // Loop over staying nights
        //for (LocalDateTime date : stayingDatesInGregorian) {
        for (int j = 0; j < stayingDatesInGregorian.size(); j++) {

            // define ordinal number of target date in Staying nights list
            int nightOrdinalNumber = j + 1;

            // Get maximum discount for target date
            TargetDateDiscountDetail targetDateDiscountDetail = getMaximumDiscountForTargetDate(
                    boomId,
                    roomName,
                    stayingDatesInGregorian.get(j),
                    nightOrdinalNumber,
                    nightPrice,
                    totalPrice);

            if (targetDateDiscountDetail.getCalculatedDiscount() != null) {
                totalDiscount += targetDateDiscountDetail.getCalculatedDiscount();
            }

            // Build target-date-discount-info
            JSONObject targetDateDiscountInfo = createTargetDateDiscountInfoJSONObject(targetDateDiscountDetail);
            // Add target-date-discount-info to targetDateDiscountDetails array
            targetDateDiscountDetails.put(targetDateDiscountInfo);
        }

        // Build json objects for total-discount-amount and discount-info-details
        JSONObject totalDiscountObject = new JSONObject().put("totalDiscount", totalDiscount);
        JSONObject DiscountInfoDetails = new JSONObject().put("targetDateDiscountDetails", targetDateDiscountDetails);

        return new JSONArray()
                .put(totalDiscountObject)
                .put(DiscountInfoDetails);
    }

    private JSONObject createTargetDateDiscountInfoJSONObject(TargetDateDiscountDetail targetDateDiscountDetail) {
        /* Build JSON object according to target date discount info */

        JSONObject targetDateDiscountInfo = new JSONObject();
        targetDateDiscountInfo.put("targetDate", targetDateDiscountDetail.getTargetDate());
        targetDateDiscountInfo.put("calculatedDiscount", targetDateDiscountDetail.getCalculatedDiscount());
        targetDateDiscountInfo.put("DiscountId", targetDateDiscountDetail.getDiscountId());

        return targetDateDiscountInfo;
    }

    private List<LocalDateTime> buildGregorianStayingDates(TripInfo tripInfo) {
        /* Build list of staying dates in gregorian */

        List<String> jalaliDates = new ArrayList<>();
        jalaliDates.add(tripInfo.getStartDate());

        for (int i = 1; i < tripInfo.getNights(); i++)
            jalaliDates.add(getPast("/", tripInfo.getStartDate(), -1 * i));

        return TimeUtility.convertJalaliDatesListToGregorian(jalaliDates);
    }

    public DiscountInfo buildDiscountInfo(
            String roomName,
            Long nightPrice,
            Long totalPrice,
            ObjectId boomId,
            List<LocalDateTime> stayingDatesInGregorian
    ) {
        /* Build discountInfo for ReservationRequest Doc. */

        Long totalDiscount = 0L;

        // Crate a list of TargetDateDiscountDetail
        List<TargetDateDiscountDetail> targetDateDiscountDetails = new ArrayList<>();

        // Loop over staying nights
        for (int j = 0; j < stayingDatesInGregorian.size(); j++) {

            // define ordinal number of target date in Staying nights list
            int nightOrdinalNumber = j + 1;

            // Get maximum discount for target date
            TargetDateDiscountDetail targetDateDiscountDetail = getMaximumDiscountForTargetDate(
                    boomId,
                    roomName,
                    stayingDatesInGregorian.get(j),
                    nightOrdinalNumber,
                    nightPrice,
                    totalPrice);

            if (targetDateDiscountDetail.getCalculatedDiscount() != null) {
                totalDiscount += targetDateDiscountDetail.getCalculatedDiscount();
            }

            targetDateDiscountDetails.add(targetDateDiscountDetail);
        }

        return new DiscountInfo()
                .setTotalDiscount(totalDiscount)
                .setTargetDateDiscountDetails(targetDateDiscountDetails);

    }

}
