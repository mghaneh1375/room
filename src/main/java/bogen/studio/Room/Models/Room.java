package bogen.studio.Room.Models;

import bogen.studio.Room.Enums.AccessibilityFeature;
import bogen.studio.Room.Enums.Limitation;
import bogen.studio.Room.Enums.SleepFeature;
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
@Document(collection = "room")
public class Room {

    @Id
    @MongoId
    @Field("_id")
    private ObjectId _id;

    private String title;
    private String description;
    private String image;

    @Field("max_cap")
    private Integer maxCap;

    @Field("cap_price")
    private Integer capPrice;

    private List<Limitation> limitations;

    @Field("sleep_features")
    private List<SleepFeature> sleepFeatures;

    @Field("accessibility_features")
    private List<AccessibilityFeature> accessibilityFeatures;

    private boolean availability;
    private boolean visibility;

    @Field("user_id")
    private Integer userId;

    @Field("boom_id")
    private ObjectId boomId;

    @Field("created_at")
    @CreatedDate
    private Date createdAt;

}
