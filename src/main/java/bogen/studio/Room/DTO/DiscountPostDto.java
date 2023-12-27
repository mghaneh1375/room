package bogen.studio.Room.DTO;

import bogen.studio.Room.Models.CodeDiscount;
import bogen.studio.Room.Models.LastMinuteDiscount;
import bogen.studio.Room.Validator.discount.ValidDiscount;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ValidDiscount
public class DiscountPostDto {

    private String discountPlace;
    private DiscountPlaceInfoPostDto discountPlaceInfoPostDto;
    private String discountType;

    private GeneralDiscountPostDto generalDiscountPostDto;
    private LastMinuteDiscountPostDto lastMinuteDiscountPostDto;
    private CodeDiscountPostDto codeDiscountPostDto;

}
