package bogen.studio.Room.Repository;

import bogen.studio.Room.Enums.ReservationStatus;
import bogen.studio.Room.Models.ReservationRequest;
import bogen.studio.Room.Models.ReservationStatusDate;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import javax.swing.plaf.PanelUI;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReservationRequestRepository2 {

    private final MongoTemplate mongoTemplate;

    public void changeReservationRequestStatus(ObjectId reservationId, ReservationStatus newStatus) {

        Query query = new Query().addCriteria(Criteria.where("_id").is(reservationId));

        Update update = new Update();
        update.set("status", newStatus);
        update.addToSet("reservationStatusHistory", new ReservationStatusDate(LocalDateTime.now(), newStatus));

        mongoTemplate.findAndModify(
                query,
                update,
                ReservationRequest.class,
                mongoTemplate.getCollectionName(ReservationRequest.class)
        );

    }

}
