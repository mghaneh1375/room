package bogen.studio.Room.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class DiscountConsumptionInfo {

    @Id
    private String discountId;

    private int usageCount;
    private long calculatedDiscountSummation;
    private List<String> consumersId;
}
