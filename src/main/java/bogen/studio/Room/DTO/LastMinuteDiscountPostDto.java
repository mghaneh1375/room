package bogen.studio.Room.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class LastMinuteDiscountPostDto {

    private String discountExecution;
    private Integer percent;
    private Long amount;
    private String targetDate; // "2024-12-31T00:00:00"
    private String lifeTimeStart; // "2024-12-31T00:00:00"
}
