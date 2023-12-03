package bogen.studio.Room.scheduledJobs;

import bogen.studio.Room.Enums.ReservationStatus;
import bogen.studio.Room.Models.ReservationRequest;
import bogen.studio.Room.Models.ReservationStatusDate;
import bogen.studio.Room.Repository.ReservationRequestRepository;
import bogen.studio.Room.Service.ReservationRequestService;
import bogen.studio.Room.Service.RoomDateReservationStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static bogen.studio.Room.Enums.ReservationStatus.CANCEL_BY_PAYMENT_2_TIMEOUT;
import static bogen.studio.Room.Enums.ReservationStatus.WAIT_FOR_PAYMENT_2;

@Component
@Slf4j
@RequiredArgsConstructor
public class HandlePayment2TimeoutJob {

    private final ReservationRequestService reservationRequestService;
    private final ReservationRequestRepository reservationRequestRepository;
    private final RoomDateReservationStateService roomDateReservationStateService;

    @Value("${payment2.timout.minute}")
    private int payment2timeoutMinute;

    @Scheduled(fixedRate = 30, timeUnit = TimeUnit.MINUTES)
    private void scheduledMethod() {
        /* This job will change status of reservations, which has wait-for-payment-2 status with expired related timeout */

        List<ReservationRequest> requests = reservationRequestService.findByStatus(WAIT_FOR_PAYMENT_2);

        if (requests.size() == 0) {
            log.info("There is no reservation request with WAIT_FOR_PAYMENT_2 to be checked by HandlePayment2TimeoutJob");
        }

        for (ReservationRequest request : requests) {

            try {
                // Get log time of wait-for-payment-2
                LocalDateTime waitForPayment2LogTime = getWaitForPayment2LogTime(request.getReservationStatusHistory());
                if (waitForPayment2LogTime.isBefore(LocalDateTime.now().minusMinutes(payment2timeoutMinute))) {
                    // Change status and update status history
                    request.setStatus(CANCEL_BY_PAYMENT_2_TIMEOUT);
                    request.addToReservationStatusHistory(new ReservationStatusDate(LocalDateTime.now(), CANCEL_BY_PAYMENT_2_TIMEOUT));
                    reservationRequestRepository.save(request);
                    log.info(String.format("Status of reservation request: %s, changed to: %s", request.get_id(), CANCEL_BY_PAYMENT_2_TIMEOUT));
                    // Set reserved rooms free
                    roomDateReservationStateService.setRoomDateStatusesToFree(
                            request.getRoomId(),
                            request.getResidenceStartDate(),
                            request.getNumberOfStayingNights(),
                            CANCEL_BY_PAYMENT_2_TIMEOUT
                    );
                }

            } catch (OptimisticLockingFailureException e) {
                log.warn(String.format("Optimistic lock activated for reservation request: %s, while changing status to: %s",
                        request.get_id(), CANCEL_BY_PAYMENT_2_TIMEOUT));
            } catch (RuntimeException e) {
                log.error(e.getMessage());
            }
        }
    }

    private LocalDateTime getWaitForPayment2LogTime(List<ReservationStatusDate> statusHistory) {
        /* This method finds log time of wait-for-payment-2 status */

        for (ReservationStatusDate statusDate : statusHistory) {

            if (statusDate.getReservationStatus().equals(WAIT_FOR_PAYMENT_2)) {
                return statusDate.getChangeDate();
            }

        }

        throw new RuntimeException("Expected to have WAIT_FOR_PAYMENT_2 log, but not found any. For developers attention");
    }
}
