package bogen.studio.Room.Routes.API.ReservationAPIRoutes;

import bogen.studio.Room.Service.ReservationRequestService;
import bogen.studio.Room.Validator.ObjectIdConstraint;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;

import static bogen.studio.Room.Routes.API.Room.OwnerRoomAPIRoutes.userId;

@RestController
@RequestMapping(path = "/api/manage/reserve")
@Validated
public class OwnerReserveAPIRoutes {

    @Autowired
    ReservationRequestService reservationRequestService;

    @PutMapping(value = "answerToRequest/{id}/{status}")
    @ResponseBody
    public String answerToRequest(HttpServletRequest request,
                                  @PathVariable @ObjectIdConstraint ObjectId id,
                                  @PathVariable @NotBlank String status) {
        return reservationRequestService.answerToRequest(id, userId, status);
    }

}
