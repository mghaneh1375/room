package bogen.studio.Room.Service;

import bogen.studio.Room.Enums.ReservationStatus;
import bogen.studio.Room.Repository.ReservationRequestsRepository;
import bogen.studio.Room.Repository.RoomRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static bogen.studio.commonkoochita.Utility.Utility.generateSuccessMsg;

@Service
public class AccountantService {

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    ReservationRequestsRepository reservationRequestsRepository;

    public String getPendingCount(ObjectId userId) {
        return generateSuccessMsg("data",
                reservationRequestsRepository.countByOwnerIdAndStatus(userId, ReservationStatus.PENDING.getName().toUpperCase())
        );
    }

}
