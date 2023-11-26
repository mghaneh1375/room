package bogen.studio.Room.Service;

import bogen.studio.Room.Enums.RoomStatus;
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
                            .localDateTime(localDateTime)
                            .roomObjectId(roomId)
                            .roomStatus(RoomStatus.FREE)
                            .userId(null)
                            .reservationRequestId(null)
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

        return roomDateReservationStateRepository.findRoomDateReservationStateForTargetDates(roomId, targetDates);
    }

    public void save(RoomDateReservationState input) {

        roomDateReservationStateRepository.save(input);
    }

}
