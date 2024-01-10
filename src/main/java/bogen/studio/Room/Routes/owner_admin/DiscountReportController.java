package bogen.studio.Room.Routes.owner_admin;

import bogen.studio.Room.DTO.PaginationResult;
import bogen.studio.Room.Enums.DiscountExecution;
import bogen.studio.Room.Service.DiscountReportService;
import bogen.studio.Room.documents.DiscountReport;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
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
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime targetDate,
            @RequestParam(required = false) DiscountExecution discountExecution,
            @RequestParam(required = false) Integer discountAmountMin,
            @RequestParam(required = false) Integer discountAmountMax,
            @RequestParam(required = false) Integer discountPercentMin,
            @RequestParam(required = false) Integer discountPercentMax,
            @RequestParam Integer page,
            @RequestParam Integer size,
            Principal principal
    ) {

        discountReportService.validateSearchFields(
                Optional.ofNullable(boomName),
                Optional.ofNullable(roomName),
                Optional.ofNullable(cityName),
                Optional.ofNullable(provinceName),
                Optional.ofNullable(createdDate),
                Optional.ofNullable(targetDate),
                Optional.ofNullable(discountExecution),
                Optional.ofNullable(discountAmountMin),
                Optional.ofNullable(discountAmountMax),
                Optional.ofNullable(discountPercentMin),
                Optional.ofNullable(discountPercentMax)
        );

        PaginationResult<DiscountReport> paginationResult = discountReportService.paginatedSearch(
                Optional.ofNullable(boomName),
                Optional.ofNullable(roomName),
                Optional.ofNullable(cityName),
                Optional.ofNullable(provinceName),
                Optional.ofNullable(createdDate),
                Optional.ofNullable(targetDate),
                Optional.ofNullable(discountExecution),
                Optional.ofNullable(discountAmountMin),
                Optional.ofNullable(discountAmountMax),
                Optional.ofNullable(discountPercentMin),
                Optional.ofNullable(discountPercentMax),
                principal,
                page,
                size
        );

        // :(
        Map<String, Object> map = new HashMap<>();
        map.put("status", "ok");
        map.put("data", paginationResult);

        return ResponseEntity.ok(map);
    }

}
