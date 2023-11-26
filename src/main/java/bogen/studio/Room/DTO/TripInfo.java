package bogen.studio.Room.DTO;

import bogen.studio.Room.Validator.ValidatedTripRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@ValidatedTripRequest
public class TripInfo {

    private Integer adults;
    private Integer children;
    private Integer infants;
    private String startDate;
    private Integer nights;

}
