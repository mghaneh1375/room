package bogen.studio.Room.Models;

import bogen.studio.Room.Enums.ReservationStatus;
import bogen.studio.Room.Enums.RoomStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReservationStatusDate {

    private LocalDateTime changeDate;
    private ReservationStatus reservationStatus;

}
