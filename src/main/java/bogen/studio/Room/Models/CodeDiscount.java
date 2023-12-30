package bogen.studio.Room.Models;

import bogen.studio.Room.Enums.DiscountExecution;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CodeDiscount {

    @Field(name = "discount_execution")
    @Enumerated(EnumType.STRING)
    private DiscountExecution discountExecution;

    private Integer percent;
    private Long amount;

    private String code;

    @Field(name = "defined_usage_count")
    private int definedUsageCount;

    @Field(name = "current_usage_count")
    private int currentUsageCount;

    @Field(name = "life_time_start")
    private LocalDateTime lifeTimeStart;

    @Field(name = "life_time_end")
    private LocalDateTime lifeTimeEnd;

    @Field(name = "target_date_start")
    private LocalDateTime targetDateStart;

    @Field(name = "target_date_end")
    private LocalDateTime targetDateEnd;


}
