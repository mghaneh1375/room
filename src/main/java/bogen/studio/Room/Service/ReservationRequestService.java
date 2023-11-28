package bogen.studio.Room.Service;

import bogen.studio.Room.DTO.ReservationRequestDTO;
import bogen.studio.Room.Enums.ReservationStatus;
import bogen.studio.Room.Models.ReservationRequest;
import bogen.studio.Room.Models.Room;
import bogen.studio.Room.Repository.ReservationRequestRepository;
import bogen.studio.Room.Repository.ReservationRequestRepository2;
import bogen.studio.Room.Repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static bogen.studio.Room.Utility.StaticValues.*;
import static bogen.studio.Room.Utility.TimeUtility.calculateTimeOutThreshold;
import static my.common.commonkoochita.Utility.Statics.*;
import static my.common.commonkoochita.Utility.Utility.*;

@Service
@RequiredArgsConstructor
public class ReservationRequestService extends AbstractService<ReservationRequest, ReservationRequestDTO> {

    //@Autowired
    private final ReservationRequestRepository reservationRequestRepository;

    //@Autowired
    private final RoomRepository roomRepository;

    private final ReservationRequestRepository2 reservationRequestRepository2;
    private final MongoTemplate mongoTemplate;

    @Override
    String list(List<String> filters) {
        return null;
    }

    @Override
    public String update(ObjectId id, ObjectId userId, ReservationRequestDTO dto) {
        return null;
    }

    @Override
    public String store(ReservationRequestDTO dto, Object... additionalFields) {
        return null;
    }

    @Override
    ReservationRequest findById(ObjectId id) {
        Optional<ReservationRequest> reservationRequests = reservationRequestRepository.findById(id);
        return reservationRequests.orElse(null);
    }

    public String getOwnerActiveRequests(ObjectId roomId, ObjectId userId) {

        List<ReservationRequest> reservationRequests = reservationRequestRepository.getActiveReservationsByRoomIdAndOwnerId(roomId, userId);

        JSONArray jsonArray = new JSONArray();

        reservationRequests.forEach(x -> jsonArray.put(convertReqToJSON(x, null, false)));

        return generateSuccessMsg("data", jsonArray);
    }

    public String getOwnerAllActiveRequests(ObjectId userId) {

        List<ReservationRequest> reservationRequests = reservationRequestRepository.getActiveReservationsByOwnerId(userId);
        List<Room> rooms = roomRepository.findDigestForOwnerByIds(
                reservationRequests.stream().map(ReservationRequest::getRoomId).collect(Collectors.toList())
        );

        JSONArray jsonArray = new JSONArray();

        reservationRequests.forEach(x -> {

            Room r = null;

            for (Room room : rooms) {

                if (x.getRoomId().equals(room.get_id())) {
                    r = room;
                    break;
                }
            }

            jsonArray.put(convertReqToJSON(x, r, true));
        });

        return generateSuccessMsg("data", jsonArray);
    }

    public String answerToRequest(ObjectId reqId, ObjectId userId, String status) {

        if (!status.equalsIgnoreCase(ReservationStatus.ACCEPT.getName()) &&
                !status.equalsIgnoreCase(ReservationStatus.REJECT.getName())
        )
            return JSON_NOT_VALID_PARAMS;

        ReservationRequest reservationRequest = findById(reqId);

        if (reservationRequest == null)
            return JSON_NOT_VALID_ID;

        if (!reservationRequest.getOwnerId().equals(userId))
            return JSON_NOT_ACCESS;

        if (!reservationRequest.getStatus().equals(ReservationStatus.PENDING) &&
                !reservationRequest.getStatus().equals(ReservationStatus.ACCEPT)
        )
            return generateErr("امکان تغییر وضعیت این درخواست وجود ندارد");

        reservationRequest.setStatus(status.equalsIgnoreCase(ReservationStatus.ACCEPT.getName()) ?
                ReservationStatus.ACCEPT : ReservationStatus.REJECT
        );

        reservationRequest.setAnswerAt(new Date());

        if (status.equalsIgnoreCase(ReservationStatus.ACCEPT.getName()))
            reservationRequest.setReserveExpireAt(System.currentTimeMillis() + PAY_WAIT_MSEC);

        reservationRequestRepository.save(reservationRequest);
        return JSON_OK;
    }

    public String cancelMyReq(ObjectId reqId, ObjectId userId) {

        ReservationRequest reservationRequest = findById(reqId);

        if (reservationRequest == null)
            return JSON_NOT_VALID_ID;

        if(!reservationRequest.getUserId().equals(userId))
            return JSON_NOT_ACCESS;

        if (!reservationRequest.getStatus().equals(ReservationStatus.PENDING) &&
                !reservationRequest.getStatus().equals(ReservationStatus.ACCEPT)
        )
            return generateErr("امکان کنسل کردن این درخواست وجود ندارد");

        reservationRequest.setStatus(reservationRequest.getStatus().equals(ReservationStatus.ACCEPT) ?
                ReservationStatus.ACCEPT_CANCELED : ReservationStatus.CANCELED
        );

        reservationRequest.setCancelAt(new Date());

        reservationRequestRepository.save(reservationRequest);
        return JSON_OK;
    }

    private JSONObject convertReqToJSON(ReservationRequest reservationRequest, Room r, boolean forAdmin) {

        JSONObject jsonObject = new JSONObject(reservationRequest);

        jsonObject.put("id", reservationRequest.get_id().toString());
        jsonObject.remove("_id");
        jsonObject.remove("roomId");

        jsonObject.put("createdAt", convertDateToJalali(reservationRequest.getCreatedAt()));

        if(jsonObject.has("reserveExpireAt"))
            jsonObject.put("reserveExpireAt", convertDateToJalali(reservationRequest.getReserveExpireAt()));

        if (jsonObject.has("payAt"))
            jsonObject.put("payAt", convertDateToJalali(reservationRequest.getPayAt()));

        if(jsonObject.has("answerAt"))
            jsonObject.put("answerAt", convertDateToJalali(reservationRequest.getAnswerAt()));

        if (r != null) {

            JSONObject roomJSON = new JSONObject()
                    .put("title", r.getTitle())
                    .put("image", ASSET_URL + RoomService.FOLDER + "/" + r.getImage());

            if(forAdmin)
                roomJSON.put("no", r.getNo())
                        .put("id", r.get_id().toString());

            jsonObject.put("room", roomJSON);
        }

        return jsonObject;
    }

    public String getMyActiveReq(ObjectId userId) {

        List<ReservationRequest> reservationRequests = reservationRequestRepository.getActiveReservationsByUserId(userId);
        List<Room> rooms = roomRepository.findDigestByIds(
                reservationRequests.stream().map(ReservationRequest::getRoomId).collect(Collectors.toList())
        );

        JSONArray jsonArray = new JSONArray();

        reservationRequests.forEach(x -> {

            Room r = null;

            for (Room room : rooms) {

                if (x.getRoomId().equals(room.get_id())) {
                    r = room;
                    break;
                }
            }

            jsonArray.put(convertReqToJSON(x, r, false));

        });

        return generateSuccessMsg("data", jsonArray);
    }

    public String getMyReq(ObjectId userId, String trackingCode, ObjectId reqId) {

        ReservationRequest reservationRequest = trackingCode == null ?
                reservationRequestRepository.getReservationsByUserIdAndId(userId, reqId) :
                reservationRequestRepository.getReservationsByUserIdAndTrackingCode(userId, trackingCode);

        if (reservationRequest == null)
            return JSON_NOT_ACCESS;

        Room r = roomRepository.findDigestById(reservationRequest.getRoomId());
        JSONObject jsonObject = convertReqToJSON(reservationRequest, r, false);

        return generateSuccessMsg("data", jsonObject);

    }

    public void changeReservationRequestStatus(ObjectId reservationId, ReservationStatus newStatus) {

        reservationRequestRepository2.changeReservationRequestStatus(reservationId, newStatus);
    }

    public List<ReservationRequest> findExpiredReservationRequests(ReservationStatus currentStatus, int timeoutInMinutes) {
        /* Find reservation requests, which has input status and are expired according to createdAt field and input
         * timeout */

        Criteria createdAtCriteria = Criteria.where("created_at").lt(Date.from(calculateTimeOutThreshold(timeoutInMinutes)));
        Criteria statusCriteria = Criteria.where("status").is(currentStatus);
        Criteria criteria = new Criteria();
        criteria.andOperator(List.of(createdAtCriteria, statusCriteria));

        Query query = new Query().addCriteria(criteria);

        return mongoTemplate.find(
                query,
                ReservationRequest.class,
                mongoTemplate.getCollectionName(ReservationRequest.class)
        );

    }

}
