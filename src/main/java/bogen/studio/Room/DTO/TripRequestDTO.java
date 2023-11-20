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

    private Integer adults;
    private Integer children;
    private Integer infants;
    private String startDate; // "1402/09/01"
    private Integer nights;

}
