package bogen.studio.Room.DTO;

import bogen.studio.Room.Validator.ValidatedDatePrice;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;


@Getter
@Setter
@AllArgsConstructor
@ValidatedDatePrice
public class DatePrice {

    private Integer price;

    @Field("cap_price")
    private Integer capPrice;

    private String date;
    private String tag;

}
