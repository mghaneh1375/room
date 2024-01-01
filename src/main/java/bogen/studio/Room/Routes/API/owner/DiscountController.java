package bogen.studio.Room.Routes.API.owner;

import bogen.studio.Room.DTO.DiscountPostDto;
import bogen.studio.Room.Service.DiscountService;
import bogen.studio.Room.documents.Discount;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.security.Principal;

import static bogen.studio.Room.Routes.Utility.getUserId;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/discount")
public class DiscountController {

    private final DiscountService discountService;

    @PostMapping("/create")
    public ResponseEntity<String> createDiscount(
            @RequestBody @Valid DiscountPostDto dto,
            Principal principal
            ) {

        var dd = discountService.insert(dto, principal);
        return ResponseEntity.ok("Successfully inserted Discount to DB: " + dd);
    }

}
