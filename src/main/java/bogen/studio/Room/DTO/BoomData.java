package bogen.studio.Room.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoomData {

    private String description;
    private String city;
    private String state;
    private String tel;
    private int room_count;
    private int stair_count;
    private Long lat;
    private Long lng;

}
