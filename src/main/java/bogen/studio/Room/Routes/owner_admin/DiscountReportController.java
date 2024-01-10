package bogen.studio.Room.Routes.owner_admin;

import bogen.studio.Room.Enums.DiscountExecution;
import bogen.studio.Room.Service.DiscountReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/discount-report")
public class DiscountReportController {

    private final DiscountReportService discountReportService;
    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam(required = false) String boomName,
            @RequestParam(required = false) String roomName,
            @RequestParam(required = false) String cityName,
            @RequestParam(required = false) String provinceName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime issueDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime targetDate,
            @RequestParam(required = false) DiscountExecution discountExecution,
            @RequestParam(required = false) Integer discountAmountMin,
            @RequestParam(required = false) Integer discountAmountMax,
            @RequestParam(required = false) Integer discountPercentMin,
            @RequestParam(required = false) Integer discountPercentMax
    ) {

        discountReportService.validateSearchFields(
                Optional.ofNullable(boomName),
                Optional.ofNullable(roomName),
                Optional.ofNullable(cityName),
                Optional.ofNullable(provinceName),
                Optional.ofNullable(issueDate),
                Optional.ofNullable(targetDate),
                Optional.ofNullable(discountExecution),
                Optional.ofNullable(discountAmountMin),
                Optional.ofNullable(discountAmountMax),
                Optional.ofNullable(discountPercentMin),
                Optional.ofNullable(discountPercentMax)
        );

        return null;
    }


}
