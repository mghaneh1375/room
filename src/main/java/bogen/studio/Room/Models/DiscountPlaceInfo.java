package bogen.studio.Room.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class DiscountPlaceInfo {

    private ObjectId boomId;
    private String roomName;
}
