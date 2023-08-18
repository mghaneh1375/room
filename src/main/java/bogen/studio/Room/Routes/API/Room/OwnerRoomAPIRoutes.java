package bogen.studio.Room.Routes.API.Room;

import bogen.studio.Room.DTO.DatePrice;
import bogen.studio.Room.DTO.RoomDTO;
import bogen.studio.Room.Models.Room;
import bogen.studio.Room.Service.RoomService;
import bogen.studio.Room.Utility.Positive;
import bogen.studio.Room.Validator.ObjectIdConstraint;
import bogen.studio.Room.Validator.StrongJSONConstraint;
import bogen.studio.Room.Validator.ValidatedRegularImage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import static bogen.studio.Room.Utility.StaticValues.JSON_NOT_VALID_ID;
import static bogen.studio.Room.Utility.Utility.generateSuccessMsg;

@RestController
@RequestMapping(path = "/api/manage/room")
@Validated
public class OwnerRoomAPIRoutes {

    @Autowired
    RoomService roomService;

    public final static int userId = 1315;

    @Autowired
    private ObjectMapper objectMapper;

    @PutMapping(value = "update/{id}")
    @ResponseBody
    public String update(HttpServletRequest request,
                         @PathVariable @ObjectIdConstraint ObjectId id,
                         final @RequestBody @Valid RoomDTO roomDTO) {
        return roomService.update(id, userId, roomDTO);
    }

    @PutMapping(value = "setPic/{id}")
    @ResponseBody
    public String setPic(HttpServletRequest request,
                         @PathVariable @ObjectIdConstraint ObjectId id,
                         final @RequestBody @ValidatedRegularImage MultipartFile file) {
        return roomService.setPic(id, file);
    }

    @PutMapping(value = "setDatePrice/{id}")
    @ResponseBody
    public String setDatePrice(HttpServletRequest request,
                               @PathVariable @ObjectIdConstraint ObjectId id,
                               final @RequestBody @Valid DatePrice datePrice) {
        return roomService.addDatePrice(id, datePrice);
    }

    @PostMapping(value = "store/{boomId}")
    @ResponseBody
    public String store(
            HttpServletRequest request,
            @PathVariable @ObjectIdConstraint ObjectId boomId,
            final @RequestPart(name = "data") @StrongJSONConstraint(
                    params = {
                            "title", "maxCap", "capPrice",
                            "cap", "price", "availability",
                    },
                    paramsType = {
                            String.class, Positive.class, Positive.class,
                            Positive.class, Positive.class, Boolean.class,
                    },
                    optionals = {
                            "description", "limitations", "sleepFeatures",
                            "accessibilityFeatures", "weekendPrice",
                            "vacationPrice", "foodFacilities", "welfare",
                            "additionalFacilities"
                    },
                    optionalsType = {
                            String.class, JSONArray.class, JSONArray.class,
                            JSONArray.class, Positive.class, Positive.class,
                            JSONArray.class, JSONArray.class, JSONArray.class
                    }
            ) @NotBlank String jsonObject) {
//,
//        final @RequestPart(value = "file", required = false) @ValidatedRegularImage MultipartFile file

        RoomDTO roomDTO;
        System.out.println("dwq");
        try {
            roomDTO = objectMapper.readValue(jsonObject, RoomDTO.class);
            return roomService.store(roomDTO, userId, boomId, null);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return e.toString();
        }
    }


    @GetMapping(value = "get/{id}")
    @ResponseBody
    public String get(@PathVariable @ObjectIdConstraint ObjectId id) {

        Room room = roomService.findById(id);
        if (room == null)
            return JSON_NOT_VALID_ID;

        JSONObject jsonObject = new JSONObject()
                .put("id", room.get_id().toString())
                .put("name", room.getTitle());

        return generateSuccessMsg("data", jsonObject);
    }
}
