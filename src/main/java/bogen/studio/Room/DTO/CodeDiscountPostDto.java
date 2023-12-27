package bogen.studio.Room.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CodeDiscountPostDto {

    private String discountExecution;
    private Integer percent;
    private Long amount;
    private String code;
    private int definedUsageCount;
    private String lifeTimeStart;
    private String lifeTimeEnd;
    private String targetDateStart;
    private String targetDateEnd;

}
