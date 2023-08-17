package bogen.studio.Room.DTO;

import bogen.studio.Room.Validator.ValidatedBoom;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

@ValidatedBoom
@Getter
@Setter
public class BoomDTO {

    private Integer userId;
    private ObjectId placeId;
    private Integer businessId;
//    private BoomData data;

}
