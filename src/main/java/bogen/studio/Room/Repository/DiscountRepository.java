package bogen.studio.Room.Repository;

import bogen.studio.Room.documents.Discount;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

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

}
