package bogen.studio.Room.DTO;

import bogen.studio.Room.Validator.ValidatedTripRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@ValidatedTripRequest
public class TripRequestDTO {

    private Integer passengers;
    private Integer infants;
    private String startDate;
    private Integer nights;

}
