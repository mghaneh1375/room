package bogen.studio.Room.Repository;

import bogen.studio.Room.Models.Room;
import bogen.studio.Room.Models.RoomStatusDate;
import bogen.studio.Room.documents.RoomDateReservationState;
import com.mongodb.client.DistinctIterable;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static bogen.studio.Room.Utility.TimeUtility.getExactStartTimeOfToday;

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

    public List<RoomStatusDate> getRoomStatusForNext5days(ObjectId roomId) {
        /* This method returns a list including status of room for five days, starting from today */

        Criteria roomIdCriteria     = Criteria.where("roomObjectId").is(roomId);
        Criteria targetDateCriteria = Criteria.where("targetDate").in(createListOfFiveDays());
        Criteria searchCriteria     = new Criteria().andOperator(roomIdCriteria, targetDateCriteria);

        AggregationOperation match   = Aggregation.match(searchCriteria);
        AggregationOperation project = Aggregation.project("targetDate", "roomStatus");
        Aggregation aggregation = Aggregation.newAggregation(match, project);

        return mongoTemplate
                .aggregate(
                        aggregation,
                        mongoTemplate.getCollectionName(RoomDateReservationState.class),
                        RoomStatusDate.class
                )
                .getMappedResults();
    }

    private List<LocalDateTime> createListOfFiveDays() {
        /* This method creates a list including dates starting from today and the next four days */

        List<LocalDateTime> output = new ArrayList<>();
        LocalDateTime todayStartTime = getExactStartTimeOfToday();

        for (int i = 0; i < 5; i++) {
            output.add(todayStartTime.plusDays(i));
        }

        return output;
    }



}
