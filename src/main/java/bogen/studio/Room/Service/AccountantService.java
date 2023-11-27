package bogen.studio.Room.Service;

import bogen.studio.Room.Enums.ReservationStatus;
import bogen.studio.Room.Repository.ReservationRequestRepository;
import bogen.studio.Room.Repository.RoomRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static my.common.commonkoochita.Utility.Utility.generateSuccessMsg;


@Service
public class AccountantService {

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    ReservationRequestRepository reservationRequestRepository;

    public String getPendingCount(ObjectId userId) {
        return generateSuccessMsg("data",
                reservationRequestRepository.countByOwnerIdAndStatus(userId, ReservationStatus.PENDING.getName().toUpperCase())
        );
    }

}
