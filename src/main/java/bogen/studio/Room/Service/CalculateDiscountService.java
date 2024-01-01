package bogen.studio.Room.Service;

import bogen.studio.Room.documents.Discount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static bogen.studio.Room.Enums.DiscountPlace.BOOM_DISCOUNT;
import static bogen.studio.Room.Enums.DiscountPlace.ROOM_DISCOUNT;
import static bogen.studio.Room.Enums.DiscountType.*;
import static bogen.studio.Room.Enums.DiscountType.CODE;

@Service
@RequiredArgsConstructor
@Slf4j
public class CalculateDiscountService {

    private final MongoTemplate mongoTemplate;

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

}
