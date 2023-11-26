package bogen.studio.Room.scheduledJobs;

import bogen.studio.Room.Service.RoomDateReservationStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class CreateRoomDateReservationStateDocumentsJob {

    private final RoomDateReservationStateService roomDateReservationStateService;

    @Scheduled(cron = "1 0 0 * * ?")
    private void createSomeDocuments() {

        roomDateReservationStateService.createRoomDateReservationStateDocuments();
    }
}
