package bogen.studio.Room.Models;

import bogen.studio.Room.Enums.DiscountPlace;
import bogen.studio.Room.Enums.DiscountType;
import bogen.studio.Room.documents.KoochitaUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class DiscountDetail {

    private String _id;
    private DiscountPlace discountPlace;
    private DiscountPlaceInfo discountPlaceInfo;
    private DiscountType discountType;
    private GeneralDiscount generalDiscount;
    private LastMinuteDiscount lastMinuteDiscount;
    private CodeDiscount codeDiscount;
    private LocalDateTime createdAt;
    private String createdBy;
    private String createdByFirstName;
    private String createdByLastName;
    private int totalUsageCount;
    private long calculatedDiscountSummation;
    private List<KoochitaUser> consumers;

}