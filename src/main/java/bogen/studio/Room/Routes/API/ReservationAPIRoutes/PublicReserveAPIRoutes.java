package bogen.studio.Room.Routes.API.ReservationAPIRoutes;

import bogen.studio.Room.Routes.Router;
import bogen.studio.Room.Service.ReservationRequestService;
import bogen.studio.commonkoochita.Validator.ObjectIdConstraint;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.security.Principal;

@RestController
@RequestMapping(path = "/api/public/reserve")
@Validated
public class PublicReserveAPIRoutes extends Router {

    @Autowired
    ReservationRequestService reservationRequestService;

    @DeleteMapping(value = "cancelMyReq/{id}")
    @ResponseBody
    public String cancelMyReq(Principal principal,
                              @PathVariable @ObjectIdConstraint ObjectId id) {
        return reservationRequestService.cancelMyReq(id, getUserId(principal));
    }

    @GetMapping(value = "getMyActiveReq")
    @ResponseBody
    public String getMyActiveReq(Principal principal) {
        return reservationRequestService.getMyActiveReq(getUserId(principal));
    }

    @GetMapping(value = "getMyReqByTrackingCode/{trackingCode}")
    @ResponseBody
    public String getMyReqByTrackingCode(Principal principal,
                           @PathVariable @NotBlank @Size(min = 6, max = 6) String trackingCode) {
        return reservationRequestService.getMyReq(getUserId(principal), trackingCode, null);
    }

    @GetMapping(value = "getMyReqById/{id}")
    @ResponseBody
    public String getMyReqById(Principal principal,
                           @PathVariable @ObjectIdConstraint ObjectId id) {
        return reservationRequestService.getMyReq(getUserId(principal), null, id);
    }


}
