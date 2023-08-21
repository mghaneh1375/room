package bogen.studio.Room.Models;

import bogen.studio.Room.DTO.DatePrice;
import bogen.studio.Room.Enums.*;
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

    private int no;
    private String title;
    private String description;
    private String image;
    private boolean main = false;

    private Integer cap;

    @Field("max_cap")
    private Integer maxCap;

    @Field("cap_price")
    private Integer capPrice;

    @Field("vacation_cap_price")
    private Integer vacationCapPrice;

    @Field("weekend_cap_price")
    private Integer weekendCapPrice;

    private Integer price;

    @Field("weekend_price")
    private Integer weekendPrice;

    @Field("vacation_price")
    private Integer vacationPrice;

    @Field("date_prices")
    private List<DatePrice> datePrices;

    private List<Limitation> limitations;
    private List<Welfare> welfares;

    @Field("sleep_features")
    private List<SleepFeature> sleepFeatures;

    @Field("additional_facilities")
    private List<AdditionalFacility> additionalFacilities;

    @Field("food_facilities")
    private List<FoodFacility> foodFacilities;

    @Field("accessibility_features")
    private List<AccessibilityFeature> accessibilityFeatures;

    private boolean availability = false;

    @Field("user_id")
    private Integer userId;

    @Field("online_reservation")
    private boolean onlineReservation = false;

    @Field("boom_id")
    private ObjectId boomId;

    @Field("created_at")
    @CreatedDate
    private Date createdAt;

}
