package bogen.studio.Room.DTO;

import bogen.studio.Room.Validator.ValidatedReservationRequest;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

@ValidatedReservationRequest
@Getter
@Setter
public class ReservationRequestDTO {

    private ObjectId passengersId;
    private ObjectId giftId;
    private String startDate;
    private Integer nights;
}
