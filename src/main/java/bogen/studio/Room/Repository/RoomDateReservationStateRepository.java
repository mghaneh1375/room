package bogen.studio.Room.Repository;

import bogen.studio.Room.Enums.RoomStatus;
import bogen.studio.Room.Models.RoomIdLocalDateTime;
import bogen.studio.Room.documents.RoomDateReservationState;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomDateReservationStateRepository {

    private final MongoTemplate mongoTemplate;

    public RoomDateReservationState insert(RoomDateReservationState roomDateReservationState) {

        return mongoTemplate.insert(roomDateReservationState, "room-date-reservation-state");
    }

    public List<RoomIdLocalDateTime> findListOfInsertedRoomIdLocalDates() {

        AggregationOperation match = Aggregation.match(Criteria.where("_id").exists(true));
        AggregationOperation project = Aggregation.project("roomObjectId", "targetDate");

        Aggregation aggregation = Aggregation.newAggregation(match, project);

        return mongoTemplate.aggregate(
                aggregation,
                mongoTemplate.getCollectionName(RoomDateReservationState.class),
                RoomIdLocalDateTime.class
        ).getMappedResults();

    }

    public List<RoomStatus> findRoomStatusInTargetDates(ObjectId roomId, List<LocalDateTime> targetDates) {
        /* This functions finds the status of the room in input dates */

        Criteria criteriaRoomId = Criteria.where("roomObjectId").is(roomId);
        Criteria criteriaStartDate = Criteria.where("targetDate").gte(targetDates.get(0));
        Criteria criteriaEndDate = Criteria.where("targetDate").lte(targetDates.get(targetDates.size() - 1));

        Criteria criteria = new Criteria();
        criteria.andOperator(List.of(criteriaRoomId, criteriaStartDate, criteriaEndDate));

        Query query = new Query().addCriteria(criteria);


        List<RoomDateReservationState> roomDateReservationStates = mongoTemplate.find(query, RoomDateReservationState.class, "room-date-reservation-state");

        return roomDateReservationStates
                .stream()
                .map(item -> item.getRoomStatus())
                .collect(Collectors.toList());

    }

    public List<RoomDateReservationState> findRoomDateReservationStateForTargetDates(ObjectId roomId, List<LocalDateTime> targetDates) {
        /* This functions finds the RoomDateReservationState documents of the room for input dates */

        Criteria criteriaRoomId = Criteria.where("roomObjectId").is(roomId);
        Criteria criteriaStartDate = Criteria.where("targetDate").gte(targetDates.get(0));
        Criteria criteriaEndDate = Criteria.where("targetDate").lte(targetDates.get(targetDates.size() - 1));

        Criteria criteria = new Criteria();
        criteria.andOperator(List.of(criteriaRoomId, criteriaStartDate, criteriaEndDate));

        Query query = new Query().addCriteria(criteria);


        return mongoTemplate.find(query, RoomDateReservationState.class, "room-date-reservation-state");
    }

    public void save(RoomDateReservationState roomDateReservationState) {

        mongoTemplate.save(roomDateReservationState, mongoTemplate.getCollectionName(RoomDateReservationState.class));

    }

}
