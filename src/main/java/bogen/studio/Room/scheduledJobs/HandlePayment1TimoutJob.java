package bogen.studio.Room.scheduledJobs;

import bogen.studio.Room.Models.ReservationRequest;
import bogen.studio.Room.Service.ReservationRequestService;
import bogen.studio.Room.Service.RoomDateReservationStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

import static bogen.studio.Room.Enums.ReservationStatus.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class HandlePayment1TimoutJob {

    private final ReservationRequestService reservationRequestService;
    private final RoomDateReservationStateService roomDateReservationStateService;

    @Value("${payment1.timeout}")
    private int payment1Timeout;

    // Todo: This method should be transactional
    @Scheduled(cron = "0 0/30 * * * ?")
    private void scheduledMethod() {
        /* This job will change status of the reservation request to CANCEL_BY_PAYMENT_TIMEOUT if the Customer would
         * not pay the fee by the defined timeout, then reserved rooms will be set free */

        List<ReservationRequest> candidateDocsToChangeStatus = reservationRequestService
                .findExpiredReservationRequests(WAIT_FOR_PAYMENT_1, payment1Timeout);

        if (candidateDocsToChangeStatus.size() == 0) {
            log.info(String.format("There is no reservation request with status: %s, which is expired", WAIT_FOR_PAYMENT_1));
        } else {

            for (ReservationRequest reservationRequest : candidateDocsToChangeStatus) {

                try {

                    // Change reservation request status
                    reservationRequestService.changeReservationRequestStatus(reservationRequest.get_id(), CANCEL_BY_PAYMENT_TIMEOUT);

                    // Change room status to free
                    roomDateReservationStateService.setRoomDateStatusesToFree(
                            reservationRequest.getRoomId(),
                            reservationRequest.getResidenceStartDate(),
                            reservationRequest.getNumberOfStayingNights(),
                            CANCEL_BY_PAYMENT_TIMEOUT
                    );

                } catch (OptimisticLockingFailureException e) { // Handle optimistic lock activation

                    log.warn("Optimistic lock activated while changing status to CANCEL_BY_OWNER_RESPONSE_TIMEOUT in Reservation_request_id: " + reservationRequest.get_id());
                }
            }
        }
    }

}
