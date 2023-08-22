package bogen.studio.Room.Models;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.*;

import javax.persistence.Id;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "boom")
public class Boom {

    @Id
    @MongoId
    @Field("_id")
    private ObjectId _id;

//    private BoomData data;

    private boolean availability = false;

    @Field("user_id")
    private Integer userId;

    @Field("place_id")
    private ObjectId placeId;

    @Field("business_id")
    private Integer businessId;

    @Field("created_at")
    @CreatedDate
    private Date createdAt;
}
