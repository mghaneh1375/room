package bogen.studio.Room.Models;

import bogen.studio.Room.DTO.DatePrice;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CalculatePriceResult {

    private Long totalPrice;
    private List<DatePrice> datePriceList;

}
