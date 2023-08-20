package bogen.studio.Room.Service;

import bogen.studio.Room.DTO.ReservationRequestDTO;
import bogen.studio.Room.Enums.ReservationStatus;
import bogen.studio.Room.Models.ReservationRequests;
import bogen.studio.Room.Models.Room;
import bogen.studio.Room.Repository.ReservationRequestsRepository;
import bogen.studio.Room.Repository.RoomRepository;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static bogen.studio.Room.Utility.StaticValues.*;
import static bogen.studio.Room.Utility.Utility.generateErr;
import static bogen.studio.Room.Utility.Utility.generateSuccessMsg;

@Service
public class ReservationRequestService extends AbstractService<ReservationRequests, ReservationRequestDTO> {

    @Autowired
    private ReservationRequestsRepository reservationRequestsRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Override
    String list(List<String> filters) {
        return null;
    }

    @Override
    public String update(ObjectId id, Object userId, ReservationRequestDTO dto) {
        return null;
    }

    @Override
    public String store(ReservationRequestDTO dto, Object... additionalFields) {
        return null;
    }

    @Override
    ReservationRequests findById(ObjectId id) {
        Optional<ReservationRequests> reservationRequests = reservationRequestsRepository.findById(id);
        return reservationRequests.orElse(null);
    }

    public String answerToRequest(ObjectId reqId, int userId, String status) {

        if(!status.equalsIgnoreCase(ReservationStatus.ACCEPT.getName()) &&
                !status.equalsIgnoreCase(ReservationStatus.REJECT.getName())
        )
            return JSON_NOT_VALID_PARAMS;

        ReservationRequests reservationRequests = findById(reqId);

        if(reservationRequests == null)
            return JSON_NOT_VALID_ID;

        if(reservationRequests.getOwnerId() != userId)
            return JSON_NOT_ACCESS;

        if(!reservationRequests.getStatus().equals(ReservationStatus.PENDING) &&
                !reservationRequests.getStatus().equals(ReservationStatus.ACCEPT)
        )
            return generateErr("امکان تغییر وضعیت این درخواست وجود ندارد");

        reservationRequests.setStatus(status.equalsIgnoreCase(ReservationStatus.ACCEPT.getName()) ?
                ReservationStatus.ACCEPT : ReservationStatus.REJECT
        );

        reservationRequests.setAnswerAt(new Date());

        if(status.equalsIgnoreCase(ReservationStatus.ACCEPT.getName()))
            reservationRequests.setReserveExpireAt(System.currentTimeMillis() + PAY_WAIT_MSEC);

        reservationRequestsRepository.save(reservationRequests);
        return JSON_OK;
    }

    public String cancelMyReq(ObjectId reqId, ObjectId userId) {

        ReservationRequests reservationRequests = findById(reqId);

        if(reservationRequests == null)
            return JSON_NOT_VALID_ID;

        //todo: check userId
//        if(!reservationRequests.getUserId().equals(userId))
//            return JSON_NOT_ACCESS;

        if(!reservationRequests.getStatus().equals(ReservationStatus.PENDING) &&
                !reservationRequests.getStatus().equals(ReservationStatus.ACCEPT)
        )
            return generateErr("امکان کنسل کردن این درخواست وجود ندارد");

        reservationRequests.setStatus(reservationRequests.getStatus().equals(ReservationStatus.ACCEPT) ?
                ReservationStatus.ACCEPT_CANCELED : ReservationStatus.CANCELED
        );

        reservationRequests.setCancelAt(new Date());

        reservationRequestsRepository.save(reservationRequests);
        return JSON_OK;
    }

    public String getMyActiveReq(ObjectId userId) {

        List<ReservationRequests> reservationRequests = reservationRequestsRepository.getActiveReservationsByUserId(userId);
        List<Room> rooms = roomRepository.findDigestByIds(
                reservationRequests.stream().map(ReservationRequests::getRoomId).collect(Collectors.toList())
        );

        JSONArray jsonArray = new JSONArray();

        reservationRequests.forEach(x -> {

            JSONObject jsonObject = new JSONObject(x);

            jsonObject.put("id", x.get_id().toString());
            jsonObject.remove("_id");
            jsonObject.remove("roomId");

            Room r = null;

            for(Room room : rooms) {

                if(x.getRoomId().equals(room.get_id())) {
                    r = room;
                    break;
                }
            }

            if(r != null)
                jsonObject.put("room", new JSONObject(r));

            jsonArray.put(jsonObject);

        });

        return generateSuccessMsg("data", jsonArray);
    }

}
