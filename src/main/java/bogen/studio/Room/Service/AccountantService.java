package bogen.studio.Room.Service;

import bogen.studio.Room.Enums.ReservationStatus;
import bogen.studio.Room.Repository.ReservationRequestRepository;
import bogen.studio.Room.Repository.ReservationRequestRepository2;
import bogen.studio.Room.Repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static my.common.commonkoochita.Utility.Utility.generateSuccessMsg;


@Service
@RequiredArgsConstructor
public class AccountantService {

    //@Autowired
    //private final RoomRepository roomRepository;

    //@Autowired
    private final ReservationRequestRepository2 reservationRequestRepository2;

//    public String getPendingCount(ObjectId userId) {
//        return generateSuccessMsg("data",
//                reservationRequestRepository.countByOwnerIdAndStatus(userId, ReservationStatus.PENDING.getName().toUpperCase())
//        );
//    }

    public String getPendingCount(ObjectId userId) {

        return generateSuccessMsg(
                "data",
                reservationRequestRepository2.countWithOwnerIdAndStatus(userId, ReservationStatus.WAIT_FOR_OWNER_RESPONSE));
    }

}
