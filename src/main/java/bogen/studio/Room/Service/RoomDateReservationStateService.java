package bogen.studio.Room.Service;

import bogen.studio.Room.Enums.RoomStatus;
import bogen.studio.Room.Exception.BackendErrorException;
import bogen.studio.Room.Models.RoomIdLocalDateTime;
import bogen.studio.Room.Repository.RoomDateReservationStateRepository;
import bogen.studio.Room.Repository.RoomRepository2;
import bogen.studio.Room.Utility.TimeUtility;
import bogen.studio.Room.documents.RoomDateReservationState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomDateReservationStateService {

    private final RoomRepository2 roomRepository2;
    private final RoomDateReservationStateRepository roomDateReservationStateRepository;

    @Value("${max.available.days.for.reservation}")
    private String futureDaysReservableThreshold;

    public void createRoomDateReservationStateDocuments() {

        // Get room ids
        List<ObjectId> roomIds = roomRepository2.findDistinctIds();

        // Get list of localDateTimes
        List<LocalDateTime> localDateTimes = TimeUtility.createLocalDateTimeList(LocalDateTime.now(), Integer.valueOf(futureDaysReservableThreshold));

        // Get inserted roomId-localDateTime pairs
        List<RoomIdLocalDateTime> roomIdLocalDateTimeList = roomDateReservationStateRepository.findListOfInsertedRoomIdLocalDates();


        for (ObjectId roomId:roomIds) {
            for (LocalDateTime localDateTime:localDateTimes) {

                // If target roomId-localDateTime does not exist in DB, then insert a model
                if (!roomIdLocalDateTimeList.contains(new RoomIdLocalDateTime(roomId, localDateTime))) {

                    // Build a model according to roomId and localDateTime values
                    RoomDateReservationState roomDateReservationState = RoomDateReservationState.builder()
                            .targetDate(localDateTime)
                            .roomObjectId(roomId)
                            .roomStatus(RoomStatus.FREE)
                            .build();

                    try {
                        RoomDateReservationState roomDateReservationState1 =  roomDateReservationStateRepository.insert(roomDateReservationState);
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

}
