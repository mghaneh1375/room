package bogen.studio.Room.DTO;

import bogen.studio.Room.Validator.ValidatedRoom;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@ValidatedRoom
@Getter
@Setter
public class RoomDTO {

    private String title;
    private String description;
    private Integer maxCap;
    private Integer cap;

    private Integer price;
    private Integer weekendPrice;
    private Integer vacationPrice;

    private Integer capPrice;
    private Integer weekendCapPrice;
    private Integer vacationCapPrice;

    private Integer count;

    private List<String> limitations;
    private List<String> welfares;
    private List<String> sleepFeatures;
    private List<String> additionalFacilities;
    private List<String> accessibilityFeatures;
    private List<String> foodFacilities;

    private Boolean onlineReservation;

    private MultipartFile image;

}
