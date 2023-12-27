package bogen.studio.Room.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class GeneralDiscountPostDto {

    private String discountExecution;
    private Integer discountPercent;
    private Long DiscountAmount;
    private Long minimumRequiredPurchase;
    private Long discountThreshold;
    private String lifeTimeStart; // "2024-12-31T00:00:00"
    private String lifeTimeEnd; // "2024-12-31T00:00:00"
    private String targetDateStart; // "2024-12-31T00:00:00"
    private String targetDateEnd; // "2024-12-31T00:00:00"

}
