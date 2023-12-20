package bogen.studio.Room.Service;

import bogen.studio.Room.Exception.InvalidFieldsException;
import bogen.studio.Room.Exception.InvalidIdException;
import bogen.studio.Room.documents.Place;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;



@Service
@RequiredArgsConstructor
public class PlaceService {

    private final MongoTemplate mongoTemplate;

    public Place fetchById(ObjectId id) throws InvalidIdException {

        Query query = new Query().addCriteria(Criteria.where("_id").is(id));

        Place place = mongoTemplate.findOne(
                query,
                Place.class,
                mongoTemplate.getCollectionName(Place.class)
        );

        if (place == null) {
            throw new InvalidIdException("مکان با این آیدی ثبت نشده است");
        }

        return place;

    }

}
