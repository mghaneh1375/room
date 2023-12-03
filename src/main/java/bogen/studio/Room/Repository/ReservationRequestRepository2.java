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

import java.time.LocalDateTime;
import java.util.List;

import static bogen.studio.Room.Enums.ReservationStatus.*;

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

//    public UpdateResult changeReservationRequestStatusIfCurrentStatusMatched(ObjectId reservationId, ReservationStatus currentStatus, ReservationStatus newStatus) {
//
//        Query query = new Query().addCriteria(Criteria.where("_id").is(reservationId).andOperator(Criteria.where("status").is(currentStatus)));
//
//        Update update = new Update();
//        update.set("status", newStatus);
//        update.addToSet("reservationStatusHistory", new ReservationStatusDate(LocalDateTime.now(), newStatus));
//
//        UpdateResult updateResult = mongoTemplate.updateFirst(
//                query,
//                update,
//                ReservationRequest.class,
//                mongoTemplate.getCollectionName(ReservationRequest.class)
//        );
//
//        if (updateResult.getModifiedCount() > 0) {
//            log.info(String.format("Status for reservation request: %s, changed to: %s",reservationId, newStatus));
//        }
//
//        return updateResult;
//    }

    public List<ReservationRequest> findActiveReservationsByOwnerId(ObjectId ownerId) {
        /* This method returns reservations, which correspond with input ownerId and active-reservation-criteria */

        Criteria activeReservationCriteria = buildActiveReservationCriteria();
        Criteria ownerIdCriteria = Criteria.where("user_id").is(ownerId);

        Criteria searchCriteria = new Criteria();
        searchCriteria.andOperator(activeReservationCriteria, ownerIdCriteria);

        Query query = new Query().addCriteria(searchCriteria);

        return mongoTemplate.find(
                query,
                ReservationRequest.class,
                mongoTemplate.getCollectionName(ReservationRequest.class)
        );
    }

    public List<ReservationRequest> findActiveReservationsByRoomIdAndOwnerId(ObjectId roomId, ObjectId ownerId) {
        /* This function finds active requests according to active request criteria, roomId and ownerId */

        Criteria activeReservationCriteria = buildActiveReservationCriteria();
        Criteria ownerIdCriteria = Criteria.where("owner_id").is(ownerId);
        Criteria roomIdCriteria = Criteria.where("room_id").is(roomId);

        Criteria searchCriteria = new Criteria();
        searchCriteria.andOperator(activeReservationCriteria, ownerIdCriteria, roomIdCriteria);

        Query query = new Query().addCriteria(searchCriteria);

        return mongoTemplate.find(
                query,
                ReservationRequest.class,
                mongoTemplate.getCollectionName(ReservationRequest.class)
        );
    }

    public List<ReservationRequest> findActiveReservationsByUserId(ObjectId userId) {

        Criteria activeReservationCriteria = buildActiveReservationCriteria();
        Criteria userIdCriteria= Criteria.where("user_id").is(userId);
        Criteria searchCriteria = new Criteria().andOperator(activeReservationCriteria, userIdCriteria);

        Query query = new Query().addCriteria(searchCriteria);

        return mongoTemplate.find(
                query,
                ReservationRequest.class,
                mongoTemplate.getCollectionName(ReservationRequest.class)
        );
    }

    private Criteria buildActiveReservationCriteria() {
        /* This method builds a criteria for active reservations  */

        Criteria criteria1 = Criteria.where("status").is(REGISTERED_RESERVE_REQUEST);
        Criteria criteria2 = Criteria.where("status").is(WAIT_FOR_PAYMENT_1);
        Criteria criteria3 = Criteria.where("status").is(BOOKED);
        Criteria criteria4 = Criteria.where("status").is(CANCEL_BY_CUSTOMER);
        Criteria criteria5 = Criteria.where("status").is(WAIT_FOR_REFUND);
        Criteria criteria6 = Criteria.where("status").is(CANCEL_BY_OWNER);
        Criteria criteria7 = Criteria.where("status").is(WAIT_FOR_OWNER_RESPONSE);
        Criteria criteria8 = Criteria.where("status").is(ACCEPT_BY_OWNER);
        Criteria criteria9 = Criteria.where("status").is(WAIT_FOR_PAYMENT_2);

        Criteria criteria = new Criteria();

        return criteria.orOperator(criteria1, criteria2, criteria3, criteria4, criteria5, criteria6, criteria7, criteria8, criteria9);
    }

    public long countWithOwnerIdAndStatus(ObjectId ownerId, ReservationStatus status) {

        Criteria ownerIdCriteria = Criteria.where("owner_id").is(ownerId);
        Criteria statusCriteria  = Criteria.where("status").is(status);
        Criteria searchCriteria  = new Criteria().andOperator(ownerIdCriteria, statusCriteria);

        Query query = new Query().addCriteria(searchCriteria);

        return mongoTemplate.count(
                query,
                ReservationRequest.class,
                mongoTemplate.getCollectionName(ReservationRequest.class)
        );

    }

    public List<ReservationRequest> findByStatus(ReservationStatus status) {

        Query query = new Query().addCriteria(Criteria.where("status").is(status));

        return mongoTemplate.find(
                query,
                ReservationRequest.class,
                mongoTemplate.getCollectionName(ReservationRequest.class)
        );
    }

}
