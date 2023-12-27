package bogen.studio.Room.Routes.API.owner;

import bogen.studio.Room.DTO.DiscountPostDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/discount")
public class DiscountController {

    @PostMapping("/create")
    public ResponseEntity<String> createDiscount(
            @RequestBody @Valid DiscountPostDto dto
            ) {


        return ResponseEntity.ok("Temp...");
    }

}
