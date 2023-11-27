package bogen.studio.Room.Controller;

import bogen.studio.Room.Repository.ReservationRequestRepository;
import bogen.studio.Room.Repository.RoomRepository;
import bogen.studio.Room.Utility.Jobs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JobHandler {

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    ReservationRequestRepository reservationRequestRepository;

    public void run() {
        new Thread(new Jobs(roomRepository, reservationRequestRepository)).start();
    }
}
