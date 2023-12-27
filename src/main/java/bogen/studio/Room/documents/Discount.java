package bogen.studio.Room.documents;

import bogen.studio.Room.Enums.DiscountPlace;
import bogen.studio.Room.Enums.DiscountType;
import bogen.studio.Room.Models.GeneralDiscount;
import bogen.studio.Room.Models.CodeDiscount;
import bogen.studio.Room.Models.DiscountPlaceInfo;
import bogen.studio.Room.Models.LastMinuteDiscount;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.persistence.Id;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Document(value = "discount")
public class Discount {

    @Id
    private String _id;

    @Field(name = "discount_place")
    private DiscountPlace discountPlace;

    @Field(name = "discount_place_info")
    private DiscountPlaceInfo discountPlaceInfo;

    @Field(name = "discount_type")
    private DiscountType discountType;

    @Field(name = "general_discount")
        private GeneralDiscount generalDiscount;

    @Field(name = "last_minute_discount")
    private LastMinuteDiscount lastMinuteDiscount;

    @Field(name = "code_discount")
    private CodeDiscount codeDiscount;

    // Todo: add creator id and createdAt

}
