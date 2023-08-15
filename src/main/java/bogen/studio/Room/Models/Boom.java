package bogen.studio.Room.Models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

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

    private String title;
    private String image;

    private JSONObject data;

    private boolean availability;
    private boolean visibility;

    @Field("user_id")
    private Integer userId;

    @Field("mysql_id")
    private Integer mysqlId;

    @Field("created_at")
    @CreatedDate
    private Date createdAt;

}
