package bogen.studio.Room.Routes.API.BoomAPIRoutes;

import bogen.studio.Room.DTO.BoomDTO;
import bogen.studio.Room.Service.BoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping(path = "/api/boom/system")
@Validated
public class SystemBoomAPIRoutes {

    @Autowired
    BoomService boomService;

    @PostMapping("store")
    @ResponseBody
    public String store(final @RequestBody @Valid BoomDTO boomDTO) {
        return boomService.store(boomDTO);
    }

}
