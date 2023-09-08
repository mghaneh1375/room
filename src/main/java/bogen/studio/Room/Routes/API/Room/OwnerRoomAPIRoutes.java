package bogen.studio.Room.Routes.API.Room;

import bogen.studio.Room.DTO.DatePrice;
import bogen.studio.Room.DTO.RoomDTO;
import bogen.studio.Room.Service.RoomService;
import my.common.commonkoochita.Utility.Positive;
import my.common.commonkoochita.Validator.DateConstraint;
import my.common.commonkoochita.Validator.ObjectIdConstraint;
import my.common.commonkoochita.Validator.StrongJSONConstraint;
import bogen.studio.Room.Validator.ValidatedRegularImage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import java.security.Principal;
import java.util.ArrayList;

import static bogen.studio.Room.Routes.Utility.getUserId;

@RestController
@RequestMapping(path = "/api/manage/room")
@Validated
public class OwnerRoomAPIRoutes {

    @Autowired
    RoomService roomService;

    @Autowired
    private ObjectMapper objectMapper;

    @PutMapping(value = "update/{id}")
    @ResponseBody
    public String update(Principal principal,
                         @PathVariable @ObjectIdConstraint ObjectId id,
                         final @RequestBody @Valid RoomDTO roomDTO) {
        return roomService.update(id, getUserId(principal), roomDTO);
    }

    @PutMapping(value = "setPic/{id}")
    @ResponseBody
    public String setPic(Principal principal,
                         @PathVariable @ObjectIdConstraint ObjectId id,
                         final @RequestBody @ValidatedRegularImage MultipartFile file) {
        return roomService.setPic(id, getUserId(principal), file);
    }

    @PutMapping(value = "setDatePrice/{id}")
    @ResponseBody
    public String setDatePrice(Principal principal,
                               @PathVariable @ObjectIdConstraint ObjectId id,
                               final @RequestBody @Valid DatePrice datePrice) {
        return roomService.addDatePrice(id, getUserId(principal), datePrice);
    }

    @DeleteMapping(value = "removeDatePrice/{id}/{date}")
    @ResponseBody
    public String removeDatePrice(Principal principal,
                                  @PathVariable @ObjectIdConstraint ObjectId id,
                                  @PathVariable @DateConstraint String date) {
        return roomService.removeDatePrice(id, getUserId(principal), date.replace("-", "/"));
    }

    @PutMapping(value = "/toggleAccessibility/{id}")
    @ResponseBody
    public String toggleAccessibility(Principal principal,
                                      @PathVariable @ObjectIdConstraint ObjectId id
    ) {
        return roomService.toggleAccessibility(id, getUserId(principal));
    }

    @PostMapping(value = "store/{boomId}")
    @ResponseBody
    public String store(
            Principal principal,
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
            return roomService.store(roomDTO, getUserId(principal), boomId, file);
        } catch (Exception e) {
            return e.toString();
        }
    }


    @GetMapping(value = "list/{boomId}")
    @ResponseBody
    public String list(Principal principal,
                       @PathVariable @ObjectIdConstraint ObjectId boomId) {
        ArrayList<String> filters = new ArrayList<>();
        filters.add(boomId.toString());
        filters.add(getUserId(principal).toString());
        return roomService.list(filters);
    }

    @GetMapping(value = "get/{id}")
    @ResponseBody
    public String get(Principal principal,
                      @PathVariable @ObjectIdConstraint ObjectId id) {
        return roomService.get(id, getUserId(principal));
    }

    @DeleteMapping(value = "remove/{id}")
    @ResponseBody
    public String remove(Principal principal,
                         @PathVariable @ObjectIdConstraint ObjectId id
    ) {
        return roomService.remove(id, getUserId(principal));
    }
}
