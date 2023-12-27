package bogen.studio.Room.Models;

import bogen.studio.Room.Enums.DiscountExecution;
import bogen.studio.Room.Validator.bookedDate.ValidBookedDate;
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
public class GeneralDiscount {

    @Field(name = "discount_execution")
    @Enumerated(EnumType.STRING)
    private DiscountExecution discountExecution;

    private Integer percent;
    private Long amount;

    @Field(name = "minimum_required_purchase")
    private Long minimumRequiredPurchase;

    @Field(name = "discount_threshold")
    private Long discountThreshold;

    @Field(name = "life_time_start")
    private LocalDateTime lifeTimeStart;

    @Field(name = "life_time_end")
    private LocalDateTime lifeTimeEnd;

    @Field(name = "target_date_start")
    private LocalDateTime targetDateStart;

    @Field(name = "target_date_end")
    private LocalDateTime targetDateEnd;
}
