package bogen.studio.Room.Routes.API.Room;

import bogen.studio.Room.DTO.ReservationRequestDTO;
import bogen.studio.Room.Service.RoomService;
import bogen.studio.Room.Validator.ObjectIdConstraint;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import static bogen.studio.Room.Routes.API.Room.OwnerRoomAPIRoutes.userId;

@RestController
@RequestMapping(path = "/api/public/room")
@Validated
public class PublicRoomAPIRoutes {

    @Autowired
    RoomService roomService;

    @PostMapping(value = "reserve/{id}")
    @ResponseBody
    public String reserve(HttpServletRequest request,
                          @PathVariable @ObjectIdConstraint ObjectId id,
                          @RequestBody @Valid ReservationRequestDTO dto) {
        //todo: userId
        return roomService.reserve(id, dto, new ObjectId());
    }

    @PostMapping(value = "calcPrice/{id}")
    @ResponseBody
    public String calcPrice(HttpServletRequest request,
                            @PathVariable @ObjectIdConstraint ObjectId id,
                            @RequestBody @Valid ReservationRequestDTO dto) {
        return roomService.calcPrice(id, dto);
    }

}
