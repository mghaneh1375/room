package bogen.studio.Room.Routes.API.BoomAPIRoutes;

import bogen.studio.Room.Service.BoomService;
import bogen.studio.Room.Validator.ObjectIdConstraint;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;

import java.util.Collections;

import static bogen.studio.Room.Routes.API.Room.OwnerRoomAPIRoutes.userId;

@RestController
@RequestMapping(path = "/api/boom/manage")
@Validated
public class OwnerBoomAPIRoutes {

    @Autowired
    BoomService boomService;

    @GetMapping(value = "/list")
    @ResponseBody
    public String list(HttpServletRequest request) {
        return boomService.list(Collections.singletonList(userId + ""));
    }

    @PutMapping(value = "/toggleAccessibility/{id}")
    @ResponseBody
    public String toggleAccessibility(HttpServletRequest request,
                                      @PathVariable @ObjectIdConstraint ObjectId id
    ) {
        return boomService.toggleAccessibility(id);
    }

}
