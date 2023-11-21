package bogen.studio.Room.Repository;

import bogen.studio.Room.Models.RoomIdLocalDateTime;
import bogen.studio.Room.documents.RoomDateReservationState;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomDateReservationStateRepository {

    private final MongoTemplate mongoTemplate;

    public RoomDateReservationState insert(RoomDateReservationState roomDateReservationState) {

        return  mongoTemplate.insert(roomDateReservationState, "room-date-reservation-state");
    }

    public List<RoomIdLocalDateTime> findListOfInsertedRoomIdLocalDates() {

        AggregationOperation match = Aggregation.match(Criteria.where("_id").exists(true));
        AggregationOperation project = Aggregation.project("roomObjectId", "localDateTime");

        Aggregation aggregation = Aggregation.newAggregation(match, project);

        return mongoTemplate.aggregate(
                aggregation,
                mongoTemplate.getCollectionName(RoomDateReservationState.class),
                RoomIdLocalDateTime.class
        ).getMappedResults();

    }
}
