package bogen.studio.Room.Controller;

import bogen.studio.Room.Repository.ReservationRequestsRepository;
import bogen.studio.Room.Repository.RoomRepository;
import bogen.studio.Room.Utility.Jobs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JobHandler {

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    ReservationRequestsRepository reservationRequestsRepository;

    public void run() {
        new Thread(new Jobs(roomRepository, reservationRequestsRepository)).start();
    }
}
