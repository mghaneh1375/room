package bogen.studio.Room.Repository;

import bogen.studio.Room.documents.City;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CityRepository {

    private final MongoTemplate mongoTemplate;

    public City fetchById(String id) {

        Criteria criteria = Criteria.where("_id").is(id);

        Query query = new Query().addCriteria(criteria);

        return mongoTemplate.findOne(
                query,
                City.class,
                mongoTemplate.getCollectionName(City.class)
        );
    }

}
