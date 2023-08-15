package bogen.studio.Room.DTO;

import bogen.studio.Room.Validator.ValidatedRoom;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@ValidatedRoom
@Getter
@Setter
public class RoomData {

    private String title;
    private String description;
    private Integer maxCap;
    private Integer capPrice;
    private List<String> limitations;
    private List<String> sleepFeatures;
    private List<String> accessibilityFeatures;
    private MultipartFile image;
    private boolean availability;
    private boolean visibility;

}
