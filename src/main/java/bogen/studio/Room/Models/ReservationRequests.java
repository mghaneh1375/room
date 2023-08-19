package bogen.studio.Room.Models;

import bogen.studio.Room.Enums.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import javax.persistence.Id;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "reservation_request")
public class ReservationRequests {

    @Id
    @MongoId
    @Field("_id")
    private ObjectId _id;

    private ReservationStatus status = ReservationStatus.RESERVED;
    private Integer paid;

    @Field("total_amount")
    private Integer totalAmount;

    private Integer passengers;

    private List<String> nights;
    private List<Integer> prices;

    @Field("off_id")
    private ObjectId offId;

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
    private Date acceptAt;

    @Field("pay_at")
    private Date payAt;

    @Field("reserve_expire_at")
    private Long reserveExpireAt;

}
