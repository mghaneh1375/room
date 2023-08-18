package bogen.studio.Room.DTO;

import bogen.studio.Room.Validator.ValidatedReservationRequest;
import lombok.Getter;
import lombok.Setter;

@ValidatedReservationRequest
@Getter
@Setter
public class ReservationRequestDTO {

    private Integer passengers;
    private String startDate;
    private Integer nights;
}
