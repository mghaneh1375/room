package bogen.studio.Room.Models;

import bogen.studio.Room.Enums.DiscountExecution;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class LastMinuteDiscount {

    @Field(name = "discount_execution")
    @Enumerated(EnumType.STRING)
    private DiscountExecution discountExecution;

    private Integer percent;
    private Long amount;

    @Field(name = "target_date")
    private LocalDateTime targetDate;

    @Field(name = "life_time_start")
    private LocalDateTime lifeTimeStart;

}
