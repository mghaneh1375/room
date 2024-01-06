package bogen.studio.Room.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class TargetDateDiscountDetail {

    private String discountId;
    private Long calculatedDiscount;
    private LocalDateTime targetDate;

}
