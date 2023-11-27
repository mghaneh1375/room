package bogen.studio.Room.documents;


import bogen.studio.Room.Enums.RoomStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "room-date-reservation-state")
@Builder
@CompoundIndexes({
        @CompoundIndex(name = "roomId_date", def = "{'roomObjectId' : 1, 'targetDate': 1}", unique = true)
})
public class RoomDateReservationState {

    @Id
    private String _id;

    private LocalDateTime targetDate;
    private ObjectId roomObjectId;

    @Enumerated(EnumType.STRING)
    private RoomStatus roomStatus;

    @CreatedDate
    private LocalDateTime createdAt;

    @Version
    private Long version;

}
