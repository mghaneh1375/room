package bogen.studio.Room.Routes.API.BoomAPIRoutes;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(path = "/api/boom/manage")
@Validated
public class OwnerBoomAPIRoutes {

    @GetMapping(value = "/")
    @ResponseBody
    public String list(HttpServletRequest request) {

        return "ok";
    }

}
