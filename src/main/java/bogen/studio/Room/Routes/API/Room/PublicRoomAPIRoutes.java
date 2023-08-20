package bogen.studio.Room.Routes.API.Room;

import bogen.studio.Room.DTO.ReservationRequestDTO;
import bogen.studio.Room.DTO.TripRequestDTO;
import bogen.studio.Room.Service.RoomService;
import bogen.studio.Room.Utility.Utility;
import bogen.studio.Room.Validator.DateValidator;
import bogen.studio.Room.Validator.ObjectIdConstraint;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;

import static bogen.studio.Room.Utility.Utility.generateErr;
import static bogen.studio.Room.Utility.Utility.getPast;

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
                            @RequestBody @Valid TripRequestDTO dto) {
        return roomService.calcPrice(id, dto);
    }

    @GetMapping(value = "list/{boomId}")
    @ResponseBody
    public String list(HttpServletRequest request,
                       @PathVariable @ObjectIdConstraint ObjectId boomId,
                       @RequestParam(value = "passengers", required = false) @Positive @Max(20) Integer passengers,
                       @RequestParam(value = "infants", required = false) @Min(0) @Max(5) Integer infants,
                       @RequestParam(value = "nights", required = false) @Positive @Max(10) Integer nights,
                       @RequestParam(value = "startDate", required = false) String startDate) {
        if (
                (passengers == null) == (nights != null) ||
                        (passengers == null) == (startDate != null) ||
                        (passengers == null) == (infants != null)
        )
            return generateErr("لطفا تعداد مسافرین و تعداد شب های اقامت و تاریخ شروع اقامت را وارد نمایید");

        TripRequestDTO dto = null;

        if (passengers != null) {

            if(!DateValidator.isValid2(startDate))
                return generateErr("تاریخ وارد شده معتبر نمی باشد");

            if(!DateValidator.gte(startDate, Utility.getToday("/")))
                return generateErr("تاریخ باید از امروز بزرگ تر باشد");

            String futureLimit = getPast("/", -60);

            if(!DateValidator.gte(futureLimit, startDate))
                return generateErr("امکان رزرو تاریخ مدنظر هنوز باز نشده است");

            dto = new TripRequestDTO(passengers, infants, startDate.replace("-", "/"), nights);
        }

        return roomService.publicList(boomId, dto);
    }

}
