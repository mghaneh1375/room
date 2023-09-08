package bogen.studio.Room.Routes.API.Room;

import bogen.studio.Room.DTO.ReservationRequestDTO;
import bogen.studio.Room.DTO.TripRequestDTO;
import bogen.studio.Room.Service.RoomService;
import my.common.commonkoochita.Router.Router;
import my.common.commonkoochita.Validator.DateValidator;
import my.common.commonkoochita.Validator.ObjectIdConstraint;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;

import java.security.Principal;

import static my.common.commonkoochita.Utility.Utility.*;

@RestController
@RequestMapping(path = "/api/public/room")
@Validated
public class PublicRoomAPIRoutes extends Router {

    @Autowired
    RoomService roomService;

    @PostMapping(value = "reserve/{id}")
    @ResponseBody
    public String reserve(Principal principal,
                          @PathVariable @ObjectIdConstraint ObjectId id,
                          @RequestBody @Valid ReservationRequestDTO dto) {
        return roomService.reserve(id, dto, new ObjectId(getUserId(principal)));
    }

    @PostMapping(value = "calcPrice/{id}")
    @ResponseBody
    public String calcPrice(@PathVariable @ObjectIdConstraint ObjectId id,
                            @RequestBody @Valid TripRequestDTO dto) {
        return roomService.calcPrice(id, dto);
    }

    @GetMapping(value = "list/{boomId}")
    @ResponseBody
    public String list(@PathVariable ObjectId boomId, //@ObjectIdConstraint
                       @RequestParam(value = "adults", required = false) @Positive @Max(20) Integer adults,
                       @RequestParam(value = "infants", required = false) @Min(0) @Max(5) Integer infants,
                       @RequestParam(value = "children", required = false) @Min(0) @Max(5) Integer children,
                       @RequestParam(value = "nights", required = false) @Positive @Max(10) Integer nights,
                       @RequestParam(value = "startDate", required = false) String startDate) {
        if (
                (adults == null) == (nights != null) ||
                        (adults == null) == (startDate != null) ||
                        (adults == null) == (infants != null) ||
                        (adults == null) == (children != null)
        )
            return generateErr("لطفا تعداد مسافرین و تعداد شب های اقامت و تاریخ شروع اقامت را وارد نمایید");

        TripRequestDTO dto = null;

        if (adults != null) {

            if(!DateValidator.isValid2(startDate))
                return generateErr("تاریخ وارد شده معتبر نمی باشد");

            if(!DateValidator.gte(startDate, getToday("/")))
                return generateErr("تاریخ باید از امروز بزرگ تر باشد");

            String futureLimit = getPast("/", -60);

            if(!DateValidator.gte(futureLimit, startDate))
                return generateErr("امکان رزرو تاریخ مدنظر هنوز باز نشده است");

            dto = new TripRequestDTO(adults, children, infants, startDate.replace("-", "/"), nights);
        }

        return roomService.publicList(boomId, dto);
    }

}
