package bogen.studio.Room.Routes.API.Room;

import bogen.studio.Room.DTO.RoomData;
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

    public final static int userId = 12;

    @Autowired
    private ObjectMapper objectMapper;

    @PutMapping(value = "update/{id}")
    @ResponseBody
    public String update(HttpServletRequest request,
                         @PathVariable @ObjectIdConstraint ObjectId id,
                         final @RequestBody @Valid RoomData roomData) {
        return roomService.update(id, userId, roomData);
    }

    @PutMapping(value = "setPic/{id}")
    @ResponseBody
    public String setPic(HttpServletRequest request,
                         @PathVariable @ObjectIdConstraint ObjectId id,
                         final @RequestBody @ValidatedRegularImage MultipartFile file) {
        return roomService.setPic(id, file);
    }

    @PostMapping(value = "store/{boomId}")
    @ResponseBody
    public String store(
            HttpServletRequest request,
            @PathVariable @ObjectIdConstraint ObjectId boomId,
            final @RequestPart(name = "data") @StrongJSONConstraint(
                    params = {
                            "title", "maxCap", "capPrice",
                            "availability", "visibility"
                    },
                    paramsType = {
                            String.class, Positive.class, Positive.class,
                            Boolean.class, Boolean.class
                    },
                    optionals = {
                            "description", "limitations", "sleepFeatures",
                            "accessibilityFeatures"
                    },
                    optionalsType = {
                            String.class, JSONArray.class, JSONArray.class,
                            JSONArray.class,
                    }
            ) @NotBlank String jsonObject,
            final @RequestPart(value = "file") @ValidatedRegularImage MultipartFile file) {

        RoomData roomData;

        try {
            roomData = objectMapper.readValue(jsonObject, RoomData.class);
            return roomService.store(roomData, userId, boomId, file);
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
