package bogen.studio.Room.Repository;

import bogen.studio.Room.documents.BoomMapInfo;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BoomMapInfoRepository {

    private final MongoTemplate mongoTemplate;

    public BoomMapInfo fetchByBoomId(ObjectId boomId) {
        /* Fetch BoomMapInfo based on boomId */

        Query query = new Query().addCriteria(Criteria.where("boom_id").is(boomId));
        return mongoTemplate.findOne(
                query,
                BoomMapInfo.class,
                mongoTemplate.getCollectionName(BoomMapInfo.class)
        );
    }

    public BoomMapInfo insert(BoomMapInfo boomMapInfo) {
        /* Insert document to the database */

        return mongoTemplate.insert(
                boomMapInfo,
                mongoTemplate.getCollectionName(BoomMapInfo.class)
        );
    }

}
