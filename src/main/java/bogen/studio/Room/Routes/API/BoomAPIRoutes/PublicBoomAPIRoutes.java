package bogen.studio.Room.Routes.API.BoomAPIRoutes;

import bogen.studio.Room.Service.BoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import static bogen.studio.Room.Routes.API.Room.OwnerRoomAPIRoutes.userId;

@RestController
@RequestMapping(path = "/api/boom/public")
@Validated
public class PublicBoomAPIRoutes {

    @Autowired
    BoomService boomService;

    @GetMapping(value = "/search")
    @ResponseBody
    public String list(HttpServletRequest request) {
        return boomService.myList(userId);
    }

}
