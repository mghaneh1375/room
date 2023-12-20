package bogen.studio.Room.Repository;

import bogen.studio.Room.documents.NeshanMapData;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NeshanMapDataRepository {

    private final MongoTemplate mongoTemplate;

    public List<NeshanMapData> fetchAll() {

        return mongoTemplate.findAll(
                NeshanMapData.class,
                mongoTemplate.getCollectionName(NeshanMapData.class)
        );
    }

    public NeshanMapData insert(NeshanMapData neshanMapData) {

        return mongoTemplate.insert(
                neshanMapData,
                mongoTemplate.getCollectionName(NeshanMapData.class)
                );
    }

}
