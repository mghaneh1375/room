package bogen.studio.Room.Routes.API.owner;

import bogen.studio.Room.DTO.PaginationResult;
import bogen.studio.Room.Service.FinancialReportService;
import bogen.studio.Room.Validator.bookedDate.ValidBookedDate;
import bogen.studio.Room.documents.FinancialReport;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/financial-report")
@Validated
public class FinancialReportController {

    private final FinancialReportService financialReportService;
    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam List<String> roomIds,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @ValidBookedDate LocalDateTime residenceStartTime,  //2023-12-17T00:00:00.000
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @ValidBookedDate LocalDateTime residenceEndTime,  //2023-12-17T00:00:00.000
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @ValidBookedDate LocalDateTime purchaseStartTime,  //2023-12-17T00:00:00.000
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @ValidBookedDate LocalDateTime purchaseEndTime,  //2023-12-17T00:00:00.000
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size
            ) {

        PaginationResult<FinancialReport> paginationResult = financialReportService.paginatedSearch(
                roomIds,
                Optional.ofNullable(residenceStartTime),
                Optional.ofNullable(residenceEndTime),
                Optional.ofNullable(purchaseStartTime),
                Optional.ofNullable(purchaseEndTime),
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
