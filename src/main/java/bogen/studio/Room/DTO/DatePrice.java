package bogen.studio.Room.DTO;

import bogen.studio.Room.Utility.Utility;
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

    public boolean isLargerThanToday() {
        int d = Utility.convertStringToDate(date);
        int today = Utility.convertStringToDate(Utility.getToday("/"));
        return today < d;
    }
}
