package bogen.studio.Room.DTO;

import bogen.studio.Room.Validator.ValidatedReservationRequest;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

import javax.validation.constraints.Size;

@ValidatedReservationRequest
@Getter
@Setter
public class ReservationRequestDTO {

    private ObjectId passengersId;
    private ObjectId giftId;

    @Size(min = 10, max = 10)
    private String startDate;

    private Integer nights;

    private String description;
}
