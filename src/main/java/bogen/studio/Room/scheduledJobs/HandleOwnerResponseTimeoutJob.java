package bogen.studio.Room.scheduledJobs;

import bogen.studio.Room.Models.ReservationRequest;
import bogen.studio.Room.Service.ReservationRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import static bogen.studio.Room.Enums.ReservationStatus.CANCEL_BY_OWNER_RESPONSE_TIMEOUT;
import static bogen.studio.Room.Enums.ReservationStatus.WAIT_FOR_OWNER_RESPONSE;

@Component
@Slf4j
@RequiredArgsConstructor
public class HandleOwnerResponseTimeoutJob {

    private final MongoTemplate mongoTemplate;
    private final ReservationRequestService reservationRequestService;

    @Value("${owner.response.timeout}")
    private int ownerResponseTimeout;

    @Scheduled(cron = "0 0/30 * * * ?")
    private void findExpiredReservationRequests () {
        /* This job will change status of the reservation request to CANCEL_BY_OWNER_RESPONSE_TIMEOUT if the owner would
         * not respond to the request by the defined timeout */

        List<ReservationRequest> candidateDocsToChangeStatus = findCandidateReservationRequestDocsToChangeStatus();

        if (candidateDocsToChangeStatus.size() == 0) {
            log.info(String.format("There is no reservation request with status: %s, which is expired", WAIT_FOR_OWNER_RESPONSE));
        }


        for (ReservationRequest reservationRequest : candidateDocsToChangeStatus) {

            try {

                reservationRequestService.changeReservationRequestStatus(reservationRequest.get_id(), CANCEL_BY_OWNER_RESPONSE_TIMEOUT);
                log.info(String.format("Status for reservation request: %s, changed to: %s", reservationRequest.get_id(), CANCEL_BY_OWNER_RESPONSE_TIMEOUT));

                // Todo: Inform the customer: Owner did not respond to your request in the defined timeout. Please choose another room.

            } catch (OptimisticLockingFailureException e) { // Handle optimistic lock activation

                log.warn("Optimistic lock activated while changing status to CANCEL_BY_OWNER_RESPONSE_TIMEOUT in Reservation_request_id: " + reservationRequest.get_id());
            }

        }


    }

    private Instant calculateTimeOutThreshold() {
        /* Calculate instance of time out, since created_at filed of reservation request document has a type of Date.
         * Obviously, for avoiding side effects I did not change the type :)  */

        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(ownerResponseTimeout);
        ZonedDateTime timeoutThresholdZoned = timeoutThreshold.atZone(ZoneId.systemDefault());

        return timeoutThresholdZoned.toInstant();
    }

    private List<ReservationRequest> findCandidateReservationRequestDocsToChangeStatus() {
        /* Find reservation requests, which has status: WAIT_FOR_OWNER_RESPONSE and are expired according to created_at
         * field. */

        Criteria createdAtCriteria = Criteria.where("created_at").lt(Date.from(calculateTimeOutThreshold()));
        Criteria statusCriteria = Criteria.where("status").is(WAIT_FOR_OWNER_RESPONSE);


        Criteria criteria = new Criteria();
        criteria.andOperator(List.of(createdAtCriteria, statusCriteria));

        Query query = new Query().addCriteria(criteria);

        return mongoTemplate.find(
                query,
                ReservationRequest.class,
                mongoTemplate.getCollectionName(ReservationRequest.class)
        );

    }

}
