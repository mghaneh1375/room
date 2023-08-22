package bogen.studio.Room.Routes.API;

import bogen.studio.Room.Service.AccountantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import static bogen.studio.Room.Routes.API.Room.OwnerRoomAPIRoutes.userId;

@RestController
@RequestMapping(path = "/api/manage/accountant")
@Validated
public class AccountantAPIRoutes {

    @Autowired
    AccountantService accountantService;

    @GetMapping(value = "getPendingCount")
    @ResponseBody
    public String getPendingCount(HttpServletRequest request) {
        return accountantService.getPendingCount(userId);
    }

}
