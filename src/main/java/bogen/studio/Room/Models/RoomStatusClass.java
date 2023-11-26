package bogen.studio.Room.Models;

import bogen.studio.Room.Enums.RoomStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomStatusClass {

    private RoomStatus roomStatus;
}
