package bogen.studio.Room.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class DiscountInfo {

    /* If isDiscountCodeApplied is true, then totalDiscount is calculated discount regarding CodeDiscount.
     * Otherwise, totalDiscount is the summation of discounts in targetDateDiscountDetails */
    private Long totalDiscount;
    private List<TargetDateDiscountDetail> targetDateDiscountDetails;
    private boolean isDiscountCodeApplied;

}
