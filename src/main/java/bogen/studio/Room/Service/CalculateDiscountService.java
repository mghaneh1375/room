package bogen.studio.Room.Service;

import bogen.studio.Room.DTO.TripInfo;
import bogen.studio.Room.Enums.DiscountExecution;
import bogen.studio.Room.Enums.DiscountType;
import bogen.studio.Room.Exception.InvalidInputException;
import bogen.studio.Room.Models.DiscountInfo;
import bogen.studio.Room.Models.GeneralDiscount;
import bogen.studio.Room.Models.LastMinuteDiscount;
import bogen.studio.Room.Models.TargetDateDiscountDetail;
import bogen.studio.Room.Repository.DiscountRepository;
import bogen.studio.Room.Utility.TimeUtility;
import bogen.studio.Room.documents.Discount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static bogen.studio.Room.Enums.DiscountExecution.AMOUNT;
import static bogen.studio.Room.Enums.DiscountExecution.PERCENTAGE;
import static bogen.studio.Room.Enums.DiscountPlace.BOOM_DISCOUNT;
import static bogen.studio.Room.Enums.DiscountPlace.ROOM_DISCOUNT;
import static bogen.studio.Room.Enums.DiscountType.GENERAL;
import static bogen.studio.Room.Enums.DiscountType.LAST_MINUTE;
import static my.common.commonkoochita.Utility.Utility.getPast;

@Service
@RequiredArgsConstructor
@Slf4j
public class CalculateDiscountService {

    private final MongoTemplate mongoTemplate;
    private final DiscountRepository discountRepository;

    public List<Discount> fetchDefinedDiscountsForTargetDate(ObjectId boomId, String roomName, LocalDateTime targetDate) {
        /* Find defined discounts for input target date */

        // Create criteria for place, lifetime and target-date
        Criteria placeCriteria = createPlaceCriteria(boomId, roomName);
        Criteria lifeTimeCriteria = createLifeTimeCriteria();
        Criteria targetDateCriteria = createTargetDateCriteria(targetDate);

        // Create query
        Criteria searchCriteria = new Criteria().andOperator(placeCriteria, lifeTimeCriteria, targetDateCriteria);
        Query query = new Query().addCriteria(searchCriteria);

        // Search
        return mongoTemplate.find(
                query,
                Discount.class,
                mongoTemplate.getCollectionName(Discount.class)
        );
    }

    private Criteria createPlaceCriteria(ObjectId boomId, String roomName) {

        // Create Room related criteria for place
        Criteria roomPlaceCriteria = Criteria.where("discount_place").is(ROOM_DISCOUNT);
        Criteria boomIdCriteria = Criteria.where("discount_place_info.boom_id").is(boomId);
        Criteria roomIdCriteria = Criteria.where("discount_place_info.room_name").is(roomName);
        Criteria roomRelated = new Criteria().andOperator(boomIdCriteria, roomIdCriteria, roomPlaceCriteria);

        // Create Boom related criteria for place
        Criteria boomPlaceCriteria = Criteria.where("discount_place").is(BOOM_DISCOUNT);
        Criteria boomRelated = new Criteria().andOperator(boomIdCriteria, boomPlaceCriteria);

        return new Criteria().orOperator(roomRelated, boomRelated);
    }

    private Criteria createLifeTimeCriteria() {

        LocalDateTime now = LocalDateTime.now();

        // Lifetime Criteria for general discount
        Criteria generalTypeCriteria = Criteria.where("discount_type").is(GENERAL);
        Criteria generalStartTimeCriteria = Criteria.where("general_discount.life_time_start").lte(now);
        Criteria generalEndTimeCriteria = Criteria.where("general_discount.life_time_end").gte(now);
        Criteria generalLifeTimeCriteria = new Criteria().andOperator(generalTypeCriteria, generalStartTimeCriteria, generalEndTimeCriteria);

        // LifeTime Criteria for Last-minute discount
        Criteria lastMinuteTypeCriteria = Criteria.where("discount_type").is(LAST_MINUTE);
        Criteria lastMinuteStartTimeCriteria = Criteria.where("last_minute_discount.life_time_start").lte(now);
        Criteria lastMinuteTargetDate = Criteria.where("last_minute_discount.target_date").gte(now);
        Criteria lastMinuteLifeTimeCriteria = new Criteria().andOperator(lastMinuteTypeCriteria, lastMinuteStartTimeCriteria, lastMinuteTargetDate);

        // LifeTime Criteria for Code discount
        //Criteria CodeTypeCriteria = Criteria.where("discount_type").is(CODE);
        //Criteria codeStartTimeCriteria = Criteria.where("code_discount.life_time_start").lte(now);
        //Criteria codeEndTimeCriteria = Criteria.where("code_discount.life_time_end").gte(now);
        //Criteria codeLifeTimeCriteria = new Criteria().andOperator(CodeTypeCriteria, codeStartTimeCriteria, codeEndTimeCriteria);

        return new Criteria().orOperator(generalLifeTimeCriteria, lastMinuteLifeTimeCriteria);
    }

    private Criteria createTargetDateCriteria(LocalDateTime targetDate) {

        // targetDate criteria for general discount
        Criteria generalTypeCriteria = Criteria.where("discount_type").is(GENERAL);
        Criteria generalTargetDateStart = Criteria.where("general_discount.target_date_start").lte(targetDate);
        Criteria generalTargetDataEnd = Criteria.where("general_discount.target_date_end").gte(targetDate);
        Criteria generalTargetDate = new Criteria().andOperator(generalTypeCriteria, generalTargetDateStart, generalTargetDataEnd);

        // targetDate criteria for lastMinute discount
        Criteria lastMinuteTypeCriteria = Criteria.where("discount_type").is(LAST_MINUTE);
        Criteria lastMinuteTargetDateStart = Criteria.where("last_minute_discount.life_time_start").lte(targetDate);
        Criteria lastMinuteTargetDateEnd = Criteria.where("last_minute_discount.target_date").gte(targetDate);
        Criteria lastMinuteTargetDate = new Criteria().andOperator(lastMinuteTypeCriteria, lastMinuteTargetDateStart, lastMinuteTargetDateEnd);

        // targetDate criteria for code discount
        //Criteria codeTypeCriteria = Criteria.where("discount_type").is(CODE);
        //Criteria codeTargetDateStart = Criteria.where("code_discount.target_date_start").lte(targetDate);
        //Criteria codeTargetDateEnd = Criteria.where("code_discount.target_date_end").gte(targetDate);
        //Criteria codeTargetDate = new Criteria().andOperator(codeTypeCriteria, codeTargetDateStart, codeTargetDateEnd);

        return new Criteria().orOperator(generalTargetDate, lastMinuteTargetDate);
    }

    public TargetDateDiscountDetail findMaximumDiscountAmount(
            List<TargetDateDiscountDetail> targetDateDiscountDetailList,
            LocalDateTime targetDate
            ) {

        if (targetDateDiscountDetailList.size() == 0) {
            return new TargetDateDiscountDetail()
                    .setDiscountId(null)
                    .setCalculatedDiscount(null)
                    .setTargetDate(targetDate);
        }

        // Sort calculated discount list in descending order
        targetDateDiscountDetailList.sort(
                Comparator
                        .comparing(TargetDateDiscountDetail::getCalculatedDiscount)
                        .reversed());

        return new TargetDateDiscountDetail()
                .setDiscountId(targetDateDiscountDetailList.get(0).getDiscountId())
                .setCalculatedDiscount(targetDateDiscountDetailList.get(0).getCalculatedDiscount())
                .setTargetDate(targetDateDiscountDetailList.get(0).getTargetDate());

    }

    public List<TargetDateDiscountDetail> calculateDiscountAmountForFetchedDiscounts(
            List<Discount> discounts,
            int nightOrdinalNumber,
            Long nightPrice,
            Long totalAmount,
            LocalDateTime targetDate) {
        /* Calculate discount-amount for fetched discounts*/

        List<TargetDateDiscountDetail> targetDateDiscountDetailList = new ArrayList<>();

        for (Discount discount : discounts) {

            TargetDateDiscountDetail targetDateDiscountDetail = calculateDiscount(discount, nightOrdinalNumber,nightPrice, totalAmount, targetDate);

            if (targetDateDiscountDetail != null) {
                targetDateDiscountDetailList.add(targetDateDiscountDetail);
            }
        }

        return targetDateDiscountDetailList;
    }

    private TargetDateDiscountDetail calculateDiscount(
            Discount discount,
            int nightOrdinalNumber,
            Long nightPrice,
            Long totalAmount,
            LocalDateTime targetDate) {

        DiscountType discountType = discount.getDiscountType();

        if (discountType.equals(GENERAL)) {

            return calculateGeneralDiscount(discount, nightPrice, totalAmount, targetDate);

        } else if (discountType.equals(LAST_MINUTE) && nightOrdinalNumber == 1) {

            return calculateLastMinuteDiscount(discount, nightPrice, targetDate);
        }

        return null;
    }

    private TargetDateDiscountDetail calculateLastMinuteDiscount(
            Discount discount,
            Long nightPrice,
            LocalDateTime targetDate
    ) {
        /* Calculate discount for Last-minute discounts */

        LastMinuteDiscount lastMinuteDiscount = discount.getLastMinuteDiscount();
        DiscountExecution discountExecution = discount.getLastMinuteDiscount().getDiscountExecution();

        if (discountExecution.equals(PERCENTAGE)) {

            Long calculatedDiscount = nightPrice * lastMinuteDiscount.getPercent() / 100;

            return new TargetDateDiscountDetail()
                    .setDiscountId(discount.get_id())
                    .setCalculatedDiscount(calculatedDiscount)
                    .setTargetDate(targetDate);

        } else if (discountExecution.equals(AMOUNT)) {

            long calculatedDiscount = 0L;
            long discountAmount = lastMinuteDiscount.getAmount();
            if (discountAmount > nightPrice) {
                calculatedDiscount = nightPrice;
            } else {
                calculatedDiscount = discountAmount;
            }

            return new TargetDateDiscountDetail()
                    .setDiscountId(discount.get_id())
                    .setCalculatedDiscount(calculatedDiscount)
                    .setTargetDate(targetDate);

        }

        return null;
    }

    private TargetDateDiscountDetail calculateGeneralDiscount(Discount discount,long nightPrice, Long totalAmount, LocalDateTime targetDate) {
        /* Calculate discount if it is a GENERAL one */

        GeneralDiscount generalDiscount = discount.getGeneralDiscount();
        DiscountExecution discountExecution = generalDiscount.getDiscountExecution();

        if (discountExecution.equals(AMOUNT)) {

            return calculateGeneralAmountWiseDiscount(generalDiscount, discount.get_id(), nightPrice, totalAmount, targetDate);

        } else if (discountExecution.equals(PERCENTAGE)) {

            return calculateGeneralPercentWiseDiscount(generalDiscount, discount.get_id(), totalAmount, targetDate);

        } else {
            return null;
        }
    }

    private TargetDateDiscountDetail calculateGeneralPercentWiseDiscount(
            GeneralDiscount generalDiscount,
            String discountId,
            Long totalAmount,
            LocalDateTime targetDate) {
        /* Calculate general discount if execution type is PERCENTAGE */

        Long discountThreshold = generalDiscount.getDiscountThreshold();
        Long minimumRequiredPurchase = generalDiscount.getMinimumRequiredPurchase();

        if (minimumRequiredPurchase != null) {
            if (totalAmount < minimumRequiredPurchase) {
                return null;
            }
        }

        long calculatedDiscount = totalAmount * generalDiscount.getPercent() / 100;
        if (discountThreshold != null) {
            if (calculatedDiscount > discountThreshold) {
                calculatedDiscount = discountThreshold;
            }
        }

        return new TargetDateDiscountDetail()
                .setDiscountId(discountId)
                .setCalculatedDiscount(calculatedDiscount)
                .setTargetDate(targetDate);

    }

    private TargetDateDiscountDetail calculateGeneralAmountWiseDiscount(
            GeneralDiscount generalDiscount,
            String discountId,
            long nightPrice,
            long totalAmount,
            LocalDateTime targetDate) {
        /* Calculate discount if the discount is general, and amount-wise */

        Long minimumRequiredPurchase = generalDiscount.getMinimumRequiredPurchase();

        long discountAmount = generalDiscount.getAmount();
        long calculatedDiscount = 0L;
        if (discountAmount > nightPrice) {
            calculatedDiscount = nightPrice;
        } else {
            calculatedDiscount = discountAmount;
        }

        if (minimumRequiredPurchase != null) {
            // If minimumRequiredPurchase is defined in the discount doc.

            if (totalAmount > minimumRequiredPurchase) {

                return new TargetDateDiscountDetail()
                        .setDiscountId(discountId)
                        .setCalculatedDiscount(calculatedDiscount)
                        .setTargetDate(targetDate);
            } else {
                return null;
            }

        } else {
            // If minimumRequiredPurchase is NOT defined in the discount doc.

            return new TargetDateDiscountDetail()
                    .setDiscountId(discountId)
                    .setCalculatedDiscount(calculatedDiscount)
                    .setTargetDate(targetDate);
        }

    }

    private void checkCodeDiscountLifeTime(Discount codeDiscount) {
        /* Check is the discount code whether is in its lifetime */

        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(codeDiscount.getCodeDiscount().getLifeTimeStart())) {
            throw new InvalidInputException("زمان استفاده از کد تخفیف هنوز فرا نرسیده است");
        }
        if (now.isAfter(codeDiscount.getCodeDiscount().getLifeTimeEnd())) {
            throw new InvalidInputException("کد تخفیف منقضی شده است");
        }
    }

    private void checkCodeDiscountTargetDates(Discount codeDiscount, List<LocalDateTime> stayingDatesInGregorian) {

        for (LocalDateTime night : stayingDatesInGregorian) {

            long stayingNightEpoch = night.toEpochSecond(ZoneOffset.UTC);
            long targetDateEndEpoch = codeDiscount.getCodeDiscount().getTargetDateEnd().toEpochSecond(ZoneOffset.UTC);
            long targetDateStart = codeDiscount.getCodeDiscount().getTargetDateStart().toEpochSecond(ZoneOffset.UTC);

            if ((stayingNightEpoch > targetDateEndEpoch) || (stayingNightEpoch < targetDateStart)) {
                throw new InvalidInputException("تاریخ اقامت با تاریخ در نظر گرفته شده برای کد تخفیف همخوانی ندارد");
            }
        }
    }

    private void checkRoomNameIfCodeDiscountIsForRoom(Discount codeDiscount, String roomName) {
        /* If discount code is defined for the room then room name should match the defined name in the Discount */

        if (codeDiscount.getDiscountPlace().equals(ROOM_DISCOUNT)) {

            if (!codeDiscount.getDiscountPlaceInfo().getRoomName().equals(roomName)) {
                throw new InvalidInputException("کد تخفیف تعریف شده با این اتاق تطابق ندارد");
            }
        }
    }

    private void checkCurrentUsageCount(Discount codeDiscount) {
        /* Check whether current-usage-count is less than defined-usage-count */

        int currentUsageCount = codeDiscount.getCodeDiscount().getCurrentUsageCount();
        int definedUsageCount = codeDiscount.getCodeDiscount().getDefinedUsageCount();

        if (currentUsageCount >= definedUsageCount) {
            throw new InvalidInputException("تعداد استفاده از کد تخفیف به حداکثر میزان تعریف شده رسیده است");
        }

    }

    private long calculateDiscountAmountForCodeDiscount(Discount codeDiscount, long nightPrice) {
        /* Calculate discount amount for Code discount */

        long calculatedDiscount = 0;
        DiscountExecution discountExecution = codeDiscount.getCodeDiscount().getDiscountExecution();

        if (discountExecution.equals(PERCENTAGE)) {

            calculatedDiscount = nightPrice * codeDiscount.getCodeDiscount().getPercent() / 100;

        } else if (discountExecution.equals(AMOUNT)) {

            long discountAmount = codeDiscount.getCodeDiscount().getAmount();

            calculatedDiscount = Math.min(discountAmount, nightPrice);
        }

        return calculatedDiscount;
    }

    private List<TargetDateDiscountDetail> buildTargetDateDiscountDetails(String discountId, long calculatedDiscount, List<LocalDateTime> stayingDatesInGregorian) {
        /* Build a list containing TargetDateDiscountDetail regarding each staying night */

        List<TargetDateDiscountDetail> targetDateDiscountDetails = new ArrayList<>();

        for (LocalDateTime stayingNight : stayingDatesInGregorian) {

            TargetDateDiscountDetail targetDateDiscountDetail = new TargetDateDiscountDetail()
                    .setDiscountId(discountId)
                    .setCalculatedDiscount(calculatedDiscount)
                    .setTargetDate(stayingNight);

            targetDateDiscountDetails.add(targetDateDiscountDetail);
        }

        return targetDateDiscountDetails;
    }

    public DiscountInfo buildDiscountInfoForCodeDiscount(
            ObjectId boomId,
            String discountCode,
            List<LocalDateTime> stayingDatesInGregorian,
            String roomName,
            long nightPrice
    ) {
        /* Build DiscountInfo for Code discount */

        Discount codeDiscount = fetchByBoomIdAndDiscountCode(boomId, discountCode);

        // Check lifetime and targetDates, room name, and current-usage-count
        checkCodeDiscountLifeTime(codeDiscount);
        checkCodeDiscountTargetDates(codeDiscount, stayingDatesInGregorian);
        checkRoomNameIfCodeDiscountIsForRoom(codeDiscount, roomName);
        checkCurrentUsageCount(codeDiscount);

        // Calculate discount for each staying night
        long calculatedDiscount = calculateDiscountAmountForCodeDiscount(codeDiscount, nightPrice);

        // Calculate total discount
        long totalDiscount = calculatedDiscount * stayingDatesInGregorian.size();

        // Build list of targetDateDiscountDetail
        List<TargetDateDiscountDetail> targetDateDiscountDetails =
                buildTargetDateDiscountDetails(codeDiscount.get_id(), calculatedDiscount, stayingDatesInGregorian);

        return new DiscountInfo()
                .setTotalDiscount(totalDiscount)
                .setTargetDateDiscountDetails(targetDateDiscountDetails)
                .setDiscountCodeApplied(true);

    }
    public Discount fetchByBoomIdAndDiscountCode(ObjectId boomId, String code) {

        return discountRepository.fetchByBoomIdAndDiscountCode(boomId, code);
    }

    public DiscountInfo buildDiscountInfoForGeneralAndLastMinuteDiscount(
            List<LocalDateTime> stayingDatesInGregorian,
            ObjectId boomId,
            String roomName,
            long nightPrice,
            long totalPrice
    ) {
        /* Build DiscountInfo for General and LastMinute discount */

        long totalDiscount = 0L;

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
                .setTargetDateDiscountDetails(targetDateDiscountDetails)
                .setDiscountCodeApplied(false);
    }

    private TargetDateDiscountDetail getMaximumDiscountForTargetDate(
            ObjectId boomId,
            String roomName,
            LocalDateTime targetDate,
            int nightOrdinalNumber,
            Long nightPrice,
            Long totalAmount) {
        /* Find related discounts, then return the one with maximum discount amount */

        // Fetch related discounts from db
        List<Discount> discounts = fetchDefinedDiscountsForTargetDate(boomId, roomName, targetDate);

        // Calculate discount-amount for fetched discounts
        List<TargetDateDiscountDetail> targetDateDiscountDetailList =
                        calculateDiscountAmountForFetchedDiscounts(
                                discounts,
                                nightOrdinalNumber,
                                nightPrice,
                                totalAmount,
                                targetDate);

        // Find maximum discount amount
        return findMaximumDiscountAmount(targetDateDiscountDetailList, targetDate);
    }

    public JSONObject createTargetDateDiscountInfoJSONObject(TargetDateDiscountDetail targetDateDiscountDetail) {
        /* Build JSON object according to target date discount info */

        JSONObject targetDateDiscountInfo = new JSONObject();
        targetDateDiscountInfo.put("targetDate", targetDateDiscountDetail.getTargetDate());
        targetDateDiscountInfo.put("calculatedDiscount", targetDateDiscountDetail.getCalculatedDiscount());
        targetDateDiscountInfo.put("DiscountId", targetDateDiscountDetail.getDiscountId());

        return targetDateDiscountInfo;
    }

    public List<LocalDateTime> buildGregorianStayingDates(TripInfo tripInfo) {
        /* Build list of staying dates in gregorian */

        List<String> jalaliDates = new ArrayList<>();
        jalaliDates.add(tripInfo.getStartDate());

        for (int i = 1; i < tripInfo.getNights(); i++)
            jalaliDates.add(getPast("/", tripInfo.getStartDate(), -1 * i));

        return TimeUtility.convertJalaliDatesListToGregorian(jalaliDates);
    }


}
