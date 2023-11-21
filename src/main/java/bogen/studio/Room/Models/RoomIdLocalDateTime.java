package bogen.studio.Room.Models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomIdLocalDateTime {

    ObjectId roomObjectId;
    LocalDateTime localDateTime;

}
