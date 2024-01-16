package bogen.studio.Room.Service;

import bogen.studio.Room.documents.KoochitaUser;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.orm.jpa.hibernate.SpringJtaPlatform;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KoochitaUserService {

    private final MongoTemplate mongoTemplate;

    public KoochitaUser fetchKoochitaUserById(String id) {

        Query query = new Query().addCriteria(Criteria.where("_id").is(id));

        return mongoTemplate.findOne(
                query,
                KoochitaUser.class,
                mongoTemplate.getCollectionName(KoochitaUser.class));
    }

}
