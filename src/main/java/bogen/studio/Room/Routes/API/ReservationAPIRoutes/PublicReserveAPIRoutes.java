package bogen.studio.Room.Routes.API.ReservationAPIRoutes;

import bogen.studio.Room.Service.ReservationRequestService;
import bogen.studio.Room.Validator.ObjectIdConstraint;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@RestController
@RequestMapping(path = "/api/public/reserve")
@Validated
public class PublicReserveAPIRoutes {

    @Autowired
    ReservationRequestService reservationRequestService;

    @DeleteMapping(value = "cancelMyReq/{id}")
    @ResponseBody
    public String cancelMyReq(HttpServletRequest request,
                              @PathVariable @ObjectIdConstraint ObjectId id) {
        //todo: userId
        return reservationRequestService.cancelMyReq(id, new ObjectId());
    }

    @GetMapping(value = "getMyActiveReq")
    @ResponseBody
    public String getMyActiveReq(HttpServletRequest request) {
        //todo: userId
        return reservationRequestService.getMyActiveReq(new ObjectId());
    }

    @GetMapping(value = "getMyReqByTrackingCode/{trackingCode}")
    @ResponseBody
    public String getMyReqByTrackingCode(HttpServletRequest request,
                           @PathVariable @NotBlank @Size(min = 6, max = 6) String trackingCode) {
        //todo: userId
        return reservationRequestService.getMyReq(new ObjectId(), trackingCode, null);
    }

    @GetMapping(value = "getMyReqById/{id}")
    @ResponseBody
    public String getMyReqById(HttpServletRequest request,
                           @PathVariable @ObjectIdConstraint ObjectId id) {
        //todo: userId
        return reservationRequestService.getMyReq(new ObjectId(), null, id);
    }


}
