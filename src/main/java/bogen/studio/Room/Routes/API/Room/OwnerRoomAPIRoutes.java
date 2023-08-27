package bogen.studio.Room.Routes.API.Room;

import bogen.studio.Room.DTO.DatePrice;
import bogen.studio.Room.DTO.RoomDTO;
import bogen.studio.Room.Models.Room;
import bogen.studio.Room.Service.RoomService;
import bogen.studio.Room.Utility.Positive;
import bogen.studio.Room.Validator.DateConstraint;
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

import java.util.ArrayList;

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
        //todo: userId
        return roomService.setPic(id, userId, file);
    }

    @PutMapping(value = "setDatePrice/{id}")
    @ResponseBody
    public String setDatePrice(HttpServletRequest request,
                               @PathVariable @ObjectIdConstraint ObjectId id,
                               final @RequestBody @Valid DatePrice datePrice) {
        //todo: userId
        return roomService.addDatePrice(id, userId, datePrice);
    }

    @DeleteMapping(value = "removeDatePrice/{id}/{date}")
    @ResponseBody
    public String removeDatePrice(HttpServletRequest request,
                                  @PathVariable @ObjectIdConstraint ObjectId id,
                                  @PathVariable @DateConstraint String date) {
        //todo: userId
        return roomService.removeDatePrice(id, userId, date.replace("-", "/"));
    }

    @PutMapping(value = "/toggleAccessibility/{id}")
    @ResponseBody
    public String toggleAccessibility(HttpServletRequest request,
                                      @PathVariable @ObjectIdConstraint ObjectId id
    ) {
        //todo: userId
        return roomService.toggleAccessibility(id, userId);
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
                            "count"
                    },
                    paramsType = {
                            String.class, Positive.class, Positive.class,
                            Positive.class, Positive.class, Boolean.class,
                            Positive.class
                    },
                    optionals = {
                            "description", "limitations", "sleepFeatures",
                            "accessibilityFeatures", "weekendPrice",
                            "vacationPrice", "foodFacilities", "welfares",
                            "additionalFacilities"
                    },
                    optionalsType = {
                            String.class, JSONArray.class, JSONArray.class,
                            JSONArray.class, Positive.class, Positive.class,
                            JSONArray.class, JSONArray.class, JSONArray.class
                    }
            ) @NotBlank String jsonObject,
            final @RequestPart(value = "file", required = false) @ValidatedRegularImage MultipartFile file) {


        RoomDTO roomDTO;

        try {
            roomDTO = objectMapper.readValue(jsonObject, RoomDTO.class);
            return roomService.store(roomDTO, userId, boomId, file);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return e.toString();
        }
    }


    @GetMapping(value = "list/{boomId}")
    @ResponseBody
    public String list(HttpServletRequest request,
                       @PathVariable @ObjectIdConstraint ObjectId boomId) {
        ArrayList<String> filters = new ArrayList<>();
        filters.add(boomId.toString());
        filters.add(userId + "");
        return roomService.list(filters);
    }

    @GetMapping(value = "get/{id}")
    @ResponseBody
    public String get(@PathVariable @ObjectIdConstraint ObjectId id) {
        return roomService.get(id, userId);
    }

    @DeleteMapping(value = "remove/{id}")
    @ResponseBody
    public String remove(HttpServletRequest request,
                         @PathVariable @ObjectIdConstraint ObjectId id
    ) {
        return roomService.remove(id, userId);
    }
}
