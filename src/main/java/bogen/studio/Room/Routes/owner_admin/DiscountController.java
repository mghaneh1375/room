package bogen.studio.Room.Routes.owner_admin;

import bogen.studio.Room.DTO.DiscountPostDto;
import bogen.studio.Room.Enums.DiscountExecution;
import bogen.studio.Room.Enums.DiscountPlace;
import bogen.studio.Room.Enums.DiscountType;
import bogen.studio.Room.Service.DiscountService;
import bogen.studio.Room.documents.Discount;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;

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

        Discount savedDiscount = discountService.insert(dto, principal);
        return ResponseEntity.ok("Successfully inserted Discount to DB: " + savedDiscount);
    }

    @GetMapping("/search")
    public ResponseEntity<String> search(
            @RequestParam(required = false) DiscountPlace discountPlace,
            @RequestParam(required = false) String boomName,
            @RequestParam(required = false) String roomName,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lifeTimeStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lifeTimeEnd,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime targetDateStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime targetDateEnd,
            @RequestParam(required = false) DiscountType discountType,
            @RequestParam(required = false) DiscountExecution discountExecution,
            @RequestParam(required = false) Integer discountAmountMin,
            @RequestParam(required = false) Integer discountAmountMax,
            @RequestParam(required = false) Integer discountPercentMin,
            @RequestParam(required = false) Integer discountPercentMax,
            @RequestParam Integer page,
            @RequestParam Integer size,
            Principal principal
    ) {

        discountService.validateSearchFields(
                Optional.ofNullable(discountPlace),
                Optional.ofNullable(boomName),
                Optional.ofNullable(roomName),
                Optional.ofNullable(city),
                Optional.ofNullable(province),
                Optional.ofNullable(createdDate),
                Optional.ofNullable(lifeTimeStart),
                Optional.ofNullable(lifeTimeEnd),
                Optional.ofNullable(targetDateStart),
                Optional.ofNullable(targetDateEnd),
                Optional.ofNullable(discountExecution),
                Optional.ofNullable(discountAmountMin),
                Optional.ofNullable(discountAmountMax),
                Optional.ofNullable(discountPercentMin),
                Optional.ofNullable(discountPercentMax)
        );

        return ResponseEntity.ok("...");
    }
}
