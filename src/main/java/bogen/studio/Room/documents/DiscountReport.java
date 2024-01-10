package bogen.studio.Room.documents;

import bogen.studio.Room.Enums.DiscountExecution;
import bogen.studio.Room.Enums.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.persistence.Id;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Document(value = "discount_report")
public class DiscountReport {

    @Id
    private String _id;

    @Field(name = "user_id")
    private ObjectId userId; // Person who has used this discount

    @Field(name = "created_at")
    private LocalDateTime createdAt;

    @Field(name = "target_date")
    private LocalDateTime targetDate;

    @Field(name = "room_id")
    private ObjectId roomId;

    @Field(name = "room_name")
    private String roomName;

    @Field(name = "boom_id")
    private ObjectId boomId;

    @Field(name = "boom_name")
    private String boomName;

    @Field(name = "owner_id")
    private ObjectId ownerId;

    private String city;
    private String province;

    @Field(name = "discount_id")
    private String discountId;

    @Field(name = "discount_type")
    private DiscountType discountType;

    @Field(name = "discount_execution")
    private DiscountExecution discountExecution;

    @Field(name = "discount_amount")
    private Long discountAmount; // discount amount is defined by the owner, while creating new discount

    @Field(name = "discount_percent")
    private Integer discountPercent;

    @Field(name = "calculated_discount")
    private Long calculatedDiscount; // calculated discount by Backend app
}
