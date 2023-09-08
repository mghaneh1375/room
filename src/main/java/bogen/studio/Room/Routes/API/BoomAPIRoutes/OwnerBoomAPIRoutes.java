package bogen.studio.Room.Routes.API.BoomAPIRoutes;

import bogen.studio.Room.Service.BoomService;
import my.common.commonkoochita.Router.Router;
import my.common.commonkoochita.Validator.ObjectIdConstraint;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collections;

@RestController
@RequestMapping(path = "/api/boom/manage")
@Validated
public class OwnerBoomAPIRoutes extends Router {

    @Autowired
    BoomService boomService;

    @GetMapping(value = "/list")
    @ResponseBody
    public String list(Principal principal) {
        return boomService.list(Collections.singletonList(getUserId(principal)));
    }

    @PutMapping(value = "/toggleAccessibility/{id}")
    @ResponseBody
    public String toggleAccessibility(Principal principal,
                                      @PathVariable @ObjectIdConstraint ObjectId id
    ) {
        return boomService.toggleAccessibility(new ObjectId(getUserId(principal)), id);
    }

}
