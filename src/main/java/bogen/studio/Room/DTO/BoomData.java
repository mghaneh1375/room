package bogen.studio.Room.DTO;

import bogen.studio.Room.Validator.ValidatedBoom;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

@ValidatedBoom
@Getter
@Setter
public class BoomData {

    private int userId;
    private int mysqlId;
    private String title;
    private String image;
    private String createdAt;
    private JSONObject data;

}
