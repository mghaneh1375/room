package bogen.studio.Room.Routes.API.ReservationAPIRoutes;

import bogen.studio.Room.Service.ReservationRequestService;
import my.common.commonkoochita.Router.Router;
import my.common.commonkoochita.Validator.ObjectIdConstraint;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;

import java.security.Principal;

@RestController
@RequestMapping(path = "/api/manage/reserve")
@Validated
public class OwnerReserveAPIRoutes extends Router {

    @Autowired
    ReservationRequestService reservationRequestService;

    @PutMapping(value = "answerToRequest/{id}/{status}")
    @ResponseBody
    public String answerToRequest(Principal principal,
                                  @PathVariable @ObjectIdConstraint ObjectId id,
                                  @PathVariable @NotBlank String status) {
        return reservationRequestService.answerToRequest(id, new ObjectId(getUserId(principal)), status);
    }

    @GetMapping(value = "getActiveRequests/{roomId}")
    @ResponseBody
    public String getActiveRequests(Principal principal,
                                    @PathVariable @ObjectIdConstraint ObjectId roomId) {
        return reservationRequestService.getOwnerActiveRequests(roomId, new ObjectId(getUserId(principal)));
    }

    @GetMapping(value = "getAllActiveRequests")
    @ResponseBody
    public String getOwnerAllActiveRequests(Principal principal) {
        return reservationRequestService.getOwnerAllActiveRequests(new ObjectId(getUserId(principal)));
    }


}
