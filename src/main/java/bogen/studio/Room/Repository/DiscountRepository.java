package bogen.studio.Room.Repository;

import bogen.studio.Room.Exception.InvalidIdException;
import bogen.studio.Room.Exception.InvalidInputException;
import bogen.studio.Room.documents.Discount;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import static bogen.studio.Room.Enums.DiscountType.CODE;

@Repository
@RequiredArgsConstructor
public class DiscountRepository {

    private final MongoTemplate mongoTemplate;

    public Discount insert(Discount discount) {
        /* Insert to DB */

        return mongoTemplate.insert(
                discount,
                mongoTemplate.getCollectionName(Discount.class)
        );
    }

    public Discount fetchByBoomIdAndDiscountCode(ObjectId boomId, String code) {
        /* Fetch discount by discount-code */

        Criteria boomIdCriteria = Criteria.where("discount_place_info.boom_id").is(boomId.toString());
        Criteria discountTypeCriteria = Criteria.where("discount_type").is(CODE);
        Criteria discountCodeCriteria = Criteria.where("code_discount.code").is(code);
        Criteria searchCriteria = new Criteria().andOperator(boomIdCriteria, discountTypeCriteria, discountCodeCriteria);

        Query query = new Query().addCriteria(searchCriteria);

        Discount codeDiscount = mongoTemplate.findOne(
                query,
                Discount.class,
                mongoTemplate.getCollectionName(Discount.class)
        );

        if (codeDiscount == null) {
            throw new InvalidInputException("چنین تخفیفی در سیستم تعریف نشده است");
        }

        return codeDiscount;
    }

    public Discount fetchDiscountById(String discountId) {

        Query query = new Query().addCriteria(Criteria.where("_id").is(discountId));

        Discount fetchedDiscount = mongoTemplate.findOne(
                query,
                Discount.class,
                mongoTemplate.getCollectionName(Discount.class)
        );

        if (fetchedDiscount == null) {
            throw new InvalidIdException("آیدی تخفیف نامعتبر است");
        }

        return fetchedDiscount;
    }
}
