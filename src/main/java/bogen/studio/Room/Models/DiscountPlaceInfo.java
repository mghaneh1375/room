package bogen.studio.Room.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class DiscountPlaceInfo {

    @Field(name = "boom_id")
    private String boomId;

    @Field(name = "room_name")
    private String roomName;

    @Field(name = "boom_name")
    private String boomName;

    private String city;
    private String province;
}
