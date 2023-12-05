package bogen.studio.Room.scheduledJobs;

import bogen.studio.Room.Enums.RoomStatus;
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

import static bogen.studio.Room.Enums.ReservationStatus.CANCEL_BY_OWNER_RESPONSE_TIMEOUT;
import static bogen.studio.Room.Enums.ReservationStatus.WAIT_FOR_OWNER_RESPONSE;

@Component
@Slf4j
@RequiredArgsConstructor
public class HandleOwnerResponseTimeoutJob {

    private final ReservationRequestService reservationRequestService;
    private final RoomDateReservationStateService roomDateReservationStateService;

    @Value("${owner.response.timeout}")
    private int ownerResponseTimeout;

    // Todo: This method should be transactional
    @Scheduled(cron = "0 0/30 * * * ?")
    private void scheduledMethod() {
        /* This job will change status of the reservation request to CANCEL_BY_OWNER_RESPONSE_TIMEOUT if the owner would
         * not respond to the request by the defined timeout, then reserved rooms will be set free */

        List<ReservationRequest> candidateDocsToChangeStatus = reservationRequestService
                .findExpiredReservationRequestsRelatedToPayment1Timeout(WAIT_FOR_OWNER_RESPONSE, ownerResponseTimeout);

        if (candidateDocsToChangeStatus.size() == 0) {
            log.info(String.format("There is no reservation request with status: %s, which is expired", WAIT_FOR_OWNER_RESPONSE));
        } else {

            for (ReservationRequest reservationRequest : candidateDocsToChangeStatus) {

                try {

                    // Change reservation request status
                    reservationRequestService.changeReservationRequestStatus(reservationRequest.get_id(), CANCEL_BY_OWNER_RESPONSE_TIMEOUT);

                    // Change room status to free
                    roomDateReservationStateService.setRoomDateStatuses(
                            reservationRequest.getRoomId(),
                            reservationRequest.getResidenceStartDate(),
                            reservationRequest.getNumberOfStayingNights(),
                            CANCEL_BY_OWNER_RESPONSE_TIMEOUT,
                            RoomStatus.FREE
                    );

                    // Todo: Inform the customer: Owner did not respond to your request in the defined timeout. Please choose another room.

                } catch (OptimisticLockingFailureException e) { // Handle optimistic lock activation

                    log.warn("Optimistic lock activated while changing status to CANCEL_BY_OWNER_RESPONSE_TIMEOUT in Reservation_request_id: " + reservationRequest.get_id());
                }
            }
        }
    }

}
