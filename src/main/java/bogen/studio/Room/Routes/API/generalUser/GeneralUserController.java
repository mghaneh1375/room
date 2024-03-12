package bogen.studio.Room.Routes.API.generalUser;

import bogen.studio.Room.Service.ProvinceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/general/user")
public class GeneralUserController {

    private final ProvinceService provinceService;


    @GetMapping("/get-list-of-province")
    public ResponseEntity<?> getListOfProvinces() {

        var dd = provinceService.fetchListOfProvinces();

        return ResponseEntity.ok(dd);
    }

}
