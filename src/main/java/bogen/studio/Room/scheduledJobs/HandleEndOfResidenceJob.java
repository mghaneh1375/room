package bogen.studio.Room.scheduledJobs;

import bogen.studio.Room.Enums.ReservationStatus;
import bogen.studio.Room.Models.ReservationRequest;
import bogen.studio.Room.Models.ReservationStatusDate;
import bogen.studio.Room.Repository.ReservationRequestRepository;
import bogen.studio.Room.Service.ReservationRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

import static bogen.studio.Room.Enums.ReservationStatus.END_OF_RESIDENCE;

@Component
@Slf4j
@RequiredArgsConstructor
public class HandleEndOfResidenceJob {

    private final ReservationRequestService reservationRequestService;
    private final ReservationRequestRepository reservationRequestRepository;

    @Scheduled(cron = "1 0 0 * * ?")
    private void ScheduledMethod() {
        /* This job will find the booked reservation requests, then it will check the end of residence situation.
         * In case of end of residence situation status and history of reservation request will be updated. */

        List<ReservationRequest> bookedReservationRequests = reservationRequestService.findByStatus(ReservationStatus.BOOKED);

        if (bookedReservationRequests.size() == 0) {
            log.info("There is no reservation request with booked status to check end of residence situation");
        }

        for (ReservationRequest request : bookedReservationRequests) {

            LocalDateTime lastDayOfResidence = request.getResidenceStartDate().plusDays(request.getNumberOfStayingNights() -1);

            if (LocalDateTime.now().isAfter(lastDayOfResidence)) {

                try {
                    request.setStatus(END_OF_RESIDENCE);
                    request.addToReservationStatusHistory(new ReservationStatusDate(LocalDateTime.now(), END_OF_RESIDENCE));
                    reservationRequestRepository.save(request);
                    log.info(String.format("Status of Reservation request: %s, changed to %s", request.get_id(), END_OF_RESIDENCE));
                } catch (Exception e) {
                    log.error(String.format("Exception occurred while changing status of reservation request: %s, to%s", request.get_id(), END_OF_RESIDENCE));
                }

            }

        }

    }

}
