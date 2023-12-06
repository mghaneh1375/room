package bogen.studio.Room.Models;

import bogen.studio.Room.DTO.DatePrice;
import bogen.studio.Room.Enums.ReservationStatus;
import lombok.*;
import my.common.commonkoochita.MongoDB.Indexed;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import javax.persistence.Id;
import javax.persistence.Transient;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "reservation_request")

public class ReservationRequest {

    @Id
    @MongoId
    @Field("_id")
    private ObjectId _id;

    private ReservationStatus status;
    private Integer paid;

    @Field("total_amount")
    private Integer totalAmount;

    @Field("passengers_id")
    private ObjectId passengersId;

    @Field("owner_id")
    private ObjectId ownerId;

    @Field("gift_id")
    private ObjectId giftId;

    @Field("off_id")
    private ObjectId offId;

    @Field("tracking_code")
    @Indexed(unique = true)
    private String trackingCode;

    private Integer infants;
    private Integer children;
    private Integer adults;

    private List<DatePrice> prices;

    @Field("off_amount")
    private Integer offAmount;

    @Field("user_id")
    private ObjectId userId;

    @Field("room_id")
    private ObjectId roomId;

    @Field("created_at")
    @CreatedDate
    private Date createdAt;

    @Field("answer_at")
    private Date answerAt;

    @Field("cancel_at")
    private Date cancelAt;

    @Field("pay_at")
    private Date payAt;

    @Field("reserve_expire_at")
    private Long reserveExpireAt;

    private String description;

    private List<org.bson.Document> passengers;
    private org.bson.Document creator;

    @Setter(value = AccessLevel.NONE)
    private List<ReservationStatusDate> reservationStatusHistory = new ArrayList<>();

    @Transient
    public void addToReservationStatusHistory(ReservationStatusDate reservationStatusDate) {

        reservationStatusHistory.add(reservationStatusDate);
    }

    private LocalDateTime residenceStartDate;
    private int numberOfStayingNights;
    private List<LocalDateTime> gregorianResidenceDates;

    @Version
    private long version;

}
