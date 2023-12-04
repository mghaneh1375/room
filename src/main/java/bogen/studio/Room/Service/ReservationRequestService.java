package bogen.studio.Room.Service;

import bogen.studio.Room.DTO.ReservationRequestDTO;
import bogen.studio.Room.Enums.ReservationStatus;
import bogen.studio.Room.Exception.DocumentVersionChangedException;
import bogen.studio.Room.Exception.InvalidIdException;
import bogen.studio.Room.Exception.InvalidInputException;
import bogen.studio.Room.Exception.InvalidRequestByCustomerException;
import bogen.studio.Room.Models.ReservationRequest;
import bogen.studio.Room.Models.ReservationStatusDate;
import bogen.studio.Room.Models.Room;
import bogen.studio.Room.Repository.ReservationRequestRepository;
import bogen.studio.Room.Repository.ReservationRequestRepository2;
import bogen.studio.Room.Repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static bogen.studio.Room.Enums.ReservationStatus.*;
import static bogen.studio.Room.Utility.StaticValues.*;
import static bogen.studio.Room.Utility.TimeUtility.calculateTimeOutThreshold;
import static my.common.commonkoochita.Utility.Statics.*;
import static my.common.commonkoochita.Utility.Utility.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationRequestService extends AbstractService<ReservationRequest, ReservationRequestDTO> {

    //@Autowired
    private final ReservationRequestRepository reservationRequestRepository;

    //@Autowired
    private final MongoTemplate mongoTemplate;
    private final RoomRepository roomRepository;
    private final ReservationRequestRepository2 reservationRequestRepository2;
    private final RoomDateReservationStateService roomDateReservationStateService;

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
        return reservationRequests.orElseThrow(() -> new InvalidIdException("درخواست رزور با این آیدی وجود ندارد"));
    }

    public String getOwnerActiveRequests(ObjectId roomId, ObjectId userId) {

        //List<ReservationRequest> reservationRequests = reservationRequestRepository.getActiveReservationsByRoomIdAndOwnerId(roomId, userId);
        List<ReservationRequest> reservationRequests = reservationRequestRepository2.findActiveReservationsByRoomIdAndOwnerId(roomId, userId);

        JSONArray jsonArray = new JSONArray();

        reservationRequests.forEach(x -> jsonArray.put(convertReqToJSON(x, null, false)));

        return generateSuccessMsg("data", jsonArray);
    }

    public String getOwnerAllActiveRequests(ObjectId userId) {

        //List<ReservationRequest> reservationRequests = reservationRequestRepository.getActiveReservationsByOwnerId(userId);
        List<ReservationRequest> reservationRequests = reservationRequestRepository2.findActiveReservationsByOwnerId(userId);
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

    // Todo : make this method transactional
    public String answerToRequest(ObjectId reqId, ObjectId userId, String status) {

        // Validate status
        checkOwnerResponseToRequestIntegrity(status);

        // Find reservation request
        ReservationRequest request = findById(reqId);

        // Check whether the API caller owns the room or not
        if (!request.getOwnerId().equals(userId))
            return JSON_NOT_ACCESS;

        // Check whether current status of the request matches WAIT_FOR_OWNER_RESPONSE
        if (!request.getStatus().equals(WAIT_FOR_OWNER_RESPONSE))
            return generateErr("امکان تغییر وضعیت این درخواست وجود ندارد");


        if (status.equalsIgnoreCase("accept")) {

            try {
                request.addToReservationStatusHistory(new ReservationStatusDate(LocalDateTime.now(), ACCEPT_BY_OWNER));
                request.addToReservationStatusHistory(new ReservationStatusDate(LocalDateTime.now(), WAIT_FOR_PAYMENT_2));
                request.setStatus(WAIT_FOR_PAYMENT_2);
                reservationRequestRepository.save(request);

            }
            catch (OptimisticLockingFailureException e) {
                throw new DocumentVersionChangedException("لحظاتی پیش تغییری در وضعیت درخواست ایجاد شد. لطفا دوباره اقدام کنید.");
            }

            // Todo: inform the customer to pay the bill
        } else {

            try {
                request.addToReservationStatusHistory(new ReservationStatusDate(LocalDateTime.now(), REJECT_BY_OWNER));
                request.setStatus(REJECT_BY_OWNER);
                reservationRequestRepository.save(request);
                // Set reserved rooms to free
                roomDateReservationStateService.setRoomDateStatusesToFree(
                        request.getRoomId(),
                        request.getResidenceStartDate(),
                        request.getNumberOfStayingNights(),
                        REJECT_BY_OWNER
                );

                // Todo: inform the customer that their response is rejected by boom owner

            } catch (OptimisticLockingFailureException e) {
                throw new DocumentVersionChangedException("لحظاتی پیش تغییری در وضعیت درخواست ایجاد شد. لطفا دوباره اقدام کنید.");
            }
        }

        return JSON_OK;
    }

    private void checkOwnerResponseToRequestIntegrity(String response) {

        if (!response.toLowerCase().matches("accept") && !response.toLowerCase().matches("reject")) {
            throw new InvalidInputException("پاسخ ارسالی به درخواست نامعتبر است");
        }

    }

    // Todo: make this function transactional
    public String cancelMyReq(ObjectId reqId, ObjectId userId) {

        ReservationRequest reservationRequest = findById(reqId);
        ReservationStatus status = reservationRequest.getStatus();

        if (!reservationRequest.getUserId().equals(userId))
            return JSON_NOT_ACCESS;

        // Customer can only cancel requests with state: booked or wait for owner response
        checkIfRequestStateIsBookedOrWaitForOwnerResponse(status);

        boolean isCancelSuccessful = false;


        try {

            if (status.equals(BOOKED)) {
                reservationRequest.addToReservationStatusHistory(new ReservationStatusDate(LocalDateTime.now(), CANCEL_BY_CUSTOMER));
                reservationRequest.addToReservationStatusHistory(new ReservationStatusDate(LocalDateTime.now(), WAIT_FOR_REFUND));
                reservationRequest.setStatus(WAIT_FOR_REFUND);
            } else if (status.equals(WAIT_FOR_OWNER_RESPONSE)) {
                reservationRequest.addToReservationStatusHistory(new ReservationStatusDate(LocalDateTime.now(), CANCEL_BY_CUSTOMER));
                reservationRequest.setStatus(CANCEL_BY_CUSTOMER);
            }
            reservationRequestRepository.save(reservationRequest);
            isCancelSuccessful = true;
            log.info(String.format("Status for reservation request: %s, changed to: %s", reservationRequest.get_id(), status.equals(BOOKED) ? WAIT_FOR_REFUND : CANCEL_BY_CUSTOMER));

        } catch (OptimisticLockingFailureException e) {
            log.warn(String.format("Optimistic lock activated for canceling request by customer: %s, requestId: %s", reservationRequest.getUserId(), reservationRequest.get_id()));
            throw new DocumentVersionChangedException("وضعیت درخواست شما لحظاتی پیش تغییر کرد، لطفا دوباره اقدام کنید.");
        }

        if (isCancelSuccessful){
            roomDateReservationStateService.setRoomDateStatusesToFree(
                    reservationRequest.getRoomId(),
                    reservationRequest.getResidenceStartDate(),
                    reservationRequest.getNumberOfStayingNights(),
                    CANCEL_BY_CUSTOMER
            );
        }

        return JSON_OK;
    }

    private void checkIfRequestStateIsBookedOrWaitForOwnerResponse(ReservationStatus status) {
        /* Customer can only cancel requests with state: booked or wait for owner response */

        if (!status.equals(BOOKED) && !status.equals(WAIT_FOR_OWNER_RESPONSE)) {
            log.warn(String.format("Invalid request by customer to cancel reservation request by status: %s", status));
            throw new InvalidRequestByCustomerException("شما قادر به کنسل کردن درخواست رزور در این مرحله نیستید");
        }

    }

    private JSONObject convertReqToJSON(ReservationRequest reservationRequest, Room r, boolean forAdmin) {

        JSONObject jsonObject = new JSONObject(reservationRequest);

        jsonObject.put("id", reservationRequest.get_id().toString());
        jsonObject.remove("_id");
        jsonObject.remove("roomId");

        jsonObject.put("createdAt", convertDateToJalali(reservationRequest.getCreatedAt()));

        if (jsonObject.has("reserveExpireAt"))
            jsonObject.put("reserveExpireAt", convertDateToJalali(reservationRequest.getReserveExpireAt()));

        if (jsonObject.has("payAt"))
            jsonObject.put("payAt", convertDateToJalali(reservationRequest.getPayAt()));

        if (jsonObject.has("answerAt"))
            jsonObject.put("answerAt", convertDateToJalali(reservationRequest.getAnswerAt()));

        if (r != null) {

            JSONObject roomJSON = new JSONObject()
                    .put("title", r.getTitle())
                    .put("image", ASSET_URL + RoomService.FOLDER + "/" + r.getImage());

            if (forAdmin)
                roomJSON.put("no", r.getNo())
                        .put("id", r.get_id().toString());

            jsonObject.put("room", roomJSON);
        }

        return jsonObject;
    }

    public String getMyActiveReq(ObjectId userId) {

        //List<ReservationRequest> reservationRequests = reservationRequestRepository.getActiveReservationsByUserId(userId);
        List<ReservationRequest> reservationRequests = reservationRequestRepository2.findActiveReservationsByUserId(userId);
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

    public List<ReservationRequest> findExpiredReservationRequestsRelatedToPayment1Timeout(ReservationStatus currentStatus, int timeoutInMinutes) {
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

    public List<ReservationRequest> findByStatus(ReservationStatus status) {

        return reservationRequestRepository2.findByStatus(status);
    }

}
