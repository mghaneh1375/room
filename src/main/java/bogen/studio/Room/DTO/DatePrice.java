package bogen.studio.Room.DTO;

import bogen.studio.Room.Validator.ValidatedDatePrice;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@ValidatedDatePrice
public class DatePrice {

    private Integer price;
    private String date;

}
