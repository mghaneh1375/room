package bogen.studio.Room.Service;

import bogen.studio.Room.Enums.DiscountExecution;
import bogen.studio.Room.Enums.DiscountPlace;
import bogen.studio.Room.Enums.DiscountType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static bogen.studio.Room.Routes.Utility.getUserId;
import static bogen.studio.Room.Utility.TimeUtility.getExactEndTimeOfInputDate;
import static bogen.studio.Room.Utility.TimeUtility.getExactStartTimeOfInputDate;
import static bogen.studio.Room.Utility.UserUtility.getUserAuthorities;

@Service
@RequiredArgsConstructor
public class DiscountSearchService {

    public Query buildQueryForDiscountSearch(
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
            Optional<DiscountType> discountTypeOptional,
            Optional<DiscountExecution> discountExecutionOptional,
            Optional<Integer> discountAmountMinOptional,
            Optional<Integer> discountAmountMaxOptional,
            Optional<Integer> discountPercentMinOptional,
            Optional<Integer> discountPercentMaxOptional,
            Principal principal
    ) {
        /* Build search query for discount according to inputs */

        ArrayList<Criteria> criteriaList = new ArrayList<>();

        criteriaList.add(Criteria.where("_id").exists(true));
        discountPlaceOptional.ifPresent(discountPlace -> criteriaList.add(Criteria.where("discount_place").is(discountPlace)));
        boomNameOptional.ifPresent(boomName -> criteriaList.add(Criteria.where("discount_place_info.boom_name").is(boomName)));
        roomNameOptional.ifPresent(roomName -> criteriaList.add(Criteria.where("discount_place_info.room_name").is(roomName)));
        cityOptional.ifPresent(city -> criteriaList.add(Criteria.where("discount_place_info.city").is(city)));
        provinceOptional.ifPresent(province -> criteriaList.add(Criteria.where("discount_place_info.province").is(province)));
        createdDateOptional.ifPresent(
                createdDate -> criteriaList.add(
                        Criteria.where("created_at").gte(getExactStartTimeOfInputDate(createdDate))
                                .andOperator(Criteria.where("created_at").lte(getExactEndTimeOfInputDate(createdDate)))
                )
        );
        lifeTimeStartOptional.ifPresent(lifeTimeStart ->
                lifeTimeEndOptional.ifPresent(lifeTimeEnd ->
                        criteriaList.add(buildLifeTimeCriteria(lifeTimeStart, getExactEndTimeOfInputDate(lifeTimeEnd)))
                )
        );
        targetDateStartOptional.ifPresent(targetDateStart ->
                targetDateEndOptional.ifPresent(targetDateEnd ->
                        criteriaList.add(buildTargetDateCriteria(targetDateStart, getExactEndTimeOfInputDate(targetDateEnd)))
                )
        );
        discountTypeOptional.ifPresent(discountType -> criteriaList.add(Criteria.where("discount_type").is(discountType)));
        discountExecutionOptional.ifPresent(discountExecution -> criteriaList.add(buildDiscountExecutionCriteria(discountExecution)));
        discountAmountMinOptional.ifPresent(amountMin ->
                discountAmountMaxOptional.ifPresent(amountMax ->
                        criteriaList.add(buildDiscountAmountCriteria(amountMin, amountMax))
                )
        );
        discountPercentMinOptional.ifPresent(percentMin ->
                discountPercentMaxOptional.ifPresent(percentMax ->
                        criteriaList.add(buildDiscountPercentCriteria(percentMin, percentMax))
                )
        );

        // Get user authorities, then if it is not admin, create a criteria where ownerId equals ApiCallerId
        List<String> authorities = getUserAuthorities(principal);
        if (!authorities.contains("ADMIN")) {
            /* If Api caller is not an admin, then the callerId should be same as the createdBy in the discount */
            criteriaList.add(Criteria.where("created_by").is(getUserId(principal).toString()));
        }

        Criteria searchCriteria = new Criteria().andOperator(criteriaList);

        return new Query().addCriteria(searchCriteria);
    }

    private Criteria buildLifeTimeCriteria(LocalDateTime lifeTimeStart, LocalDateTime lifeTimeEnd) {

        // General Discount
        Criteria GeneraLifeTime = buildTimeScopeCriteria("general_discount.life_time_start", lifeTimeStart,
                "general_discount.life_time_end", lifeTimeEnd);

        // LastMinute Discount
        Criteria lastMinuteLifeTime = buildTimeScopeCriteria("last_minute_discount.life_time_start",
                lifeTimeStart, "last_minute_discount.target_date", lifeTimeEnd);

        // Code Discount
        Criteria codeLifeTime = buildTimeScopeCriteria("code_discount.life_time_start", lifeTimeStart,
                "code_discount.life_time_end", lifeTimeEnd);

        return new Criteria().orOperator(GeneraLifeTime, lastMinuteLifeTime, codeLifeTime);
    }

    private Criteria buildTargetDateCriteria(LocalDateTime targetDateStart, LocalDateTime targetDateEnd) {

        // General Discount
        Criteria generalTargetDate = buildTimeScopeCriteria("general_discount.target_date_start",
                targetDateStart, "general_discount.target_date_end", targetDateEnd);

        // LastMinute Discount
        Criteria lastMinuteTargetDate = buildTimeScopeCriteria("last_minute_discount.target_date",
                targetDateStart, "last_minute_discount.target_date", targetDateEnd);

        // Code Discount
        Criteria codeTargetDate = buildTimeScopeCriteria("code_discount.target_date_start",
                targetDateStart, "code_discount.target_date_end", targetDateEnd);

        return new Criteria().orOperator(generalTargetDate, lastMinuteTargetDate, codeTargetDate);
    }

    private Criteria buildTimeScopeCriteria(String scopeStartKey, LocalDateTime scopeStart, String scopeEndKey, LocalDateTime scopeEnd) {
        /* Build time scope criteria */

        Criteria scopteStartCriteria = Criteria.where(scopeStartKey).gte(scopeStart);
        Criteria scopeEndCriteria = Criteria.where(scopeEndKey).lte(scopeEnd);

        return new Criteria().andOperator(scopteStartCriteria, scopeEndCriteria);
    }

    private Criteria buildDiscountExecutionCriteria(DiscountExecution discountExecution) {

        Criteria generalDiscountExecution = Criteria.where("general_discount.discount_execution").is(discountExecution);
        Criteria lastMinuteDiscountExecution = Criteria.where("last_minute_discount.discount_execution").is(discountExecution);
        Criteria codeDiscountExecution = Criteria.where("code_discount.discount_execution").is(discountExecution);

        return new Criteria().orOperator(generalDiscountExecution, lastMinuteDiscountExecution, codeDiscountExecution);
    }

    private Criteria buildDiscountAmountCriteria(int amountMinValue, int amountMaxValue) {

        Criteria generalDiscountAmountCriteria = valueCriteria("general_discount.amount", amountMinValue, amountMaxValue);
        Criteria lastMinuteDiscountAmountCriteria = valueCriteria("last_minute_discount.amount", amountMinValue, amountMaxValue);
        Criteria codeDiscountAmountCriteria = valueCriteria("code_discount.amount", amountMinValue, amountMaxValue);

        return new Criteria().orOperator(generalDiscountAmountCriteria, lastMinuteDiscountAmountCriteria, codeDiscountAmountCriteria);
    }

    private Criteria buildDiscountPercentCriteria(int percentMinValue, int percentMaxValue) {

        Criteria generalDiscountPercentCriteria = valueCriteria("general_discount.percent", percentMinValue, percentMaxValue);
        Criteria lastMinuteDiscountPercentCriteria = valueCriteria("last_minute_discount.percent", percentMinValue, percentMaxValue);
        Criteria codeDiscountPercentCriteria = valueCriteria("code_discount.percent", percentMinValue, percentMaxValue);

        return new Criteria().orOperator(generalDiscountPercentCriteria, lastMinuteDiscountPercentCriteria, codeDiscountPercentCriteria);
    }

    private Criteria valueCriteria(String valueKey, int valueMin, int valueMax) {

        Criteria valueMinCriteria = Criteria.where(valueKey).gte(valueMin);
        Criteria valueMaxCriteria = Criteria.where(valueKey).lte(valueMax);

        return new Criteria().andOperator(valueMinCriteria, valueMaxCriteria);
    }

}
