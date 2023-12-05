package bogen.studio.Room.Service;

import bogen.studio.Room.Enums.ReservationStatus;
import bogen.studio.Room.Enums.RoomStatus;
import bogen.studio.Room.Exception.BackendErrorException;
import bogen.studio.Room.Models.RoomIdTargetDay;
import bogen.studio.Room.Repository.RoomDateReservationStateRepository;
import bogen.studio.Room.Repository.RoomRepository2;
import bogen.studio.Room.Utility.TimeUtility;
import bogen.studio.Room.documents.RoomDateReservationState;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomDateReservationStateService {

    private final RoomRepository2 roomRepository2;
    private final RoomDateReservationStateRepository roomDateReservationStateRepository;
    private final MongoTemplate mongoTemplate;

    @Value("${max.available.days.for.reservation}")
    private String futureDaysReservableThreshold;

    public void createRoomDateReservationStateDocuments() {

        // Get room ids
        List<ObjectId> roomIds = roomRepository2.findDistinctIds();

        // Create list of localDateTimes for which, RoomDateReservationRequest docs is need to generate
        List<LocalDateTime> localDateTimes = TimeUtility.createDatesForRoomDateReservationDocs(LocalDateTime.now(), Integer.valueOf(futureDaysReservableThreshold));

        // Get inserted roomId-localDateTime pairs
        List<RoomIdTargetDay> roomIdTargetDayList = roomDateReservationStateRepository.findListOfInsertedRoomIdTargetDate();


        for (ObjectId roomId : roomIds) {
            for (LocalDateTime localDateTime : localDateTimes) {

                // If target roomId-localDateTime does not exist in DB, then insert a model
                if (!roomIdTargetDayList.contains(new RoomIdTargetDay(roomId, localDateTime))) {

                    // Build a model according to roomId and localDateTime values
                    RoomDateReservationState roomDateReservationState = RoomDateReservationState.builder()
                            .targetDate(localDateTime)
                            .roomObjectId(roomId)
                            .roomStatus(RoomStatus.FREE)
                            .build();

                    try {
                        RoomDateReservationState roomDateReservationState1 = roomDateReservationStateRepository.insert(roomDateReservationState);
                        log.info("Inserted: " + roomDateReservationState1);
                    } catch (Exception e) {
                        log.error("Failed to insert RoomIdLocalDateTime" + e.getMessage());
                    }
                }
            }
        }

        log.info("Job performed: createRoomDateReservationStateDocuments");
    }

    public List<RoomStatus> findRoomStatusInTargetDates(ObjectId roomId, List<LocalDateTime> targetDates) {

        return roomDateReservationStateRepository.findRoomStatusInTargetDates(roomId, targetDates);
    }

    public List<RoomDateReservationState> findRoomDateReservationStateForTargetDates(ObjectId roomId, List<LocalDateTime> targetDates) {

        List<RoomDateReservationState> output = roomDateReservationStateRepository.findRoomDateReservationStateForTargetDates(roomId, targetDates);

        if (output.size() == 0) {
            log.error("There is no document for roomId: " + roomId + ", and dates: " + targetDates);
            throw new BackendErrorException("از شما پوزش می خواهیم. در پردازش درخواست شما مشکلی بوجود آمده است. لطفا با ما تماس بگیرید.");
        }

        return output;
    }

    public void save(RoomDateReservationState input) {

        roomDateReservationStateRepository.save(input);
    }

    public UpdateResult changeRoomStatus(ObjectId roomObjectId, LocalDateTime targetDate, RoomStatus newStatus) {

        Criteria roomIdCriteria = Criteria.where("roomObjectId").gte(roomObjectId);
        Criteria dateCriteria = Criteria.where("targetDate").is(targetDate);
        Criteria criteria = new Criteria();
        criteria.andOperator(List.of(roomIdCriteria, dateCriteria));

        Query query = new Query().addCriteria(criteria);

        Update update = new Update().set("roomStatus", newStatus);

        UpdateResult updateResult = mongoTemplate.updateFirst(
                query,
                update,
                RoomDateReservationState.class,
                mongoTemplate.getCollectionName(RoomDateReservationState.class)
        );

        if (updateResult.getModifiedCount() > 0) {
            log.info(String.format("Status of room: %s, in date: %s, changed to: %s", roomObjectId, targetDate, newStatus));
        }

        return updateResult;

    }

    public void setRoomDateStatuses(ObjectId roomId, LocalDateTime residenceStartTime, int numberOfStayingNights, ReservationStatus reason, RoomStatus newRoomStatus) {

        // Build dates of residence
        List<LocalDateTime> residenceDates = buildResidenceDates(residenceStartTime, numberOfStayingNights);

        for (LocalDateTime residenceDate : residenceDates) {


            try {
                changeRoomStatus(roomId, residenceDate, newRoomStatus);
            } catch (Exception e) {
                log.error(String.format(String.format("Error in setting room status to %s because of: %s, for room: %s (for backup operator attention), in dates: %s", newRoomStatus), reason, roomId, residenceDates));
            }

        }
    }

    private List<LocalDateTime> buildResidenceDates(LocalDateTime startDate, int numberOfStayingNights) {
        /* According to start date and number of staying nights, this method builds residence dates; */

        List<LocalDateTime> output = new ArrayList<>();

        for (int i = 0; i < numberOfStayingNights; i++) {

            output.add(startDate.plusDays(i));
        }

        return output;

    }

}
