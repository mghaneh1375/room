package bogen.studio.Room.Models;

import bogen.studio.Room.Enums.RoomStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class RoomStatusDate {

    LocalDateTime targetDate;
    RoomStatus roomStatus;


}
