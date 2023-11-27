package bogen.studio.Room.Repository;

import bogen.studio.Room.Models.Room;
import com.mongodb.client.DistinctIterable;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomRepository2 {

    private final MongoTemplate mongoTemplate;

    public List<Room> findAll() {

        return mongoTemplate.findAll(Room.class, "room");
    }

    public List<ObjectId> findDistinctIds() {

        // Get distinct ids of rooms
        DistinctIterable<ObjectId> roomIdsIterable = mongoTemplate
                .getCollection("room")
                .distinct("_id", ObjectId.class);

        // Define output
        List<ObjectId> objectIds = new ArrayList<>();

        // Extract data from distinct iterable to the output list
        roomIdsIterable.forEach(objectIds::add);

        return objectIds;

    }



}
