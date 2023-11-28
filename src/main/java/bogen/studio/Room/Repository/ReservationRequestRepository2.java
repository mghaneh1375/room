package bogen.studio.Room.Repository;

import bogen.studio.Room.Enums.ReservationStatus;
import bogen.studio.Room.Models.ReservationRequest;
import bogen.studio.Room.Models.ReservationStatusDate;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import javax.swing.plaf.PanelUI;
import java.time.LocalDateTime;

import static bogen.studio.Room.Enums.ReservationStatus.CANCEL_BY_OWNER_RESPONSE_TIMEOUT;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationRequestRepository2 {

    private final MongoTemplate mongoTemplate;

    public void changeReservationRequestStatus(ObjectId reservationId, ReservationStatus newStatus) {

        Query query = new Query().addCriteria(Criteria.where("_id").is(reservationId));

        Update update = new Update();
        update.set("status", newStatus);
        update.addToSet("reservationStatusHistory", new ReservationStatusDate(LocalDateTime.now(), newStatus));

        UpdateResult updateResult = mongoTemplate.updateFirst(
                query,
                update,
                ReservationRequest.class,
                mongoTemplate.getCollectionName(ReservationRequest.class)
        );

        if (updateResult.getModifiedCount() > 0) {
            log.info(String.format("Status for reservation request: %s, changed to: %s",reservationId, newStatus));
        }

    }

}
