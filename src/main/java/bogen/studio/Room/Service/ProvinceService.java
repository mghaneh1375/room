package bogen.studio.Room.Service;


import bogen.studio.Room.documents.Province;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProvinceService {

    private final MongoTemplate mongoTemplate;

    public List<Province> fetchListOfProvinces(){

        Query query = new Query().addCriteria(Criteria.where("_id").exists(true));


        var dd = mongoTemplate.findAll(Province.class, "state");


        var ddd = mongoTemplate.find(
                query,
                Province.class,
                mongoTemplate.getCollectionName(Province.class)
        );

        return ddd;
    }

}
