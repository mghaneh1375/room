package bogen.studio.Room.Routes.API.customer;

import bogen.studio.Room.Service.JasperReportService;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.JRException;
import org.bson.types.ObjectId;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;

import static bogen.studio.Room.Routes.Utility.getUserId;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/voucher")
public class VoucherController {

    private final JasperReportService jasperReportService;
    @GetMapping("/download")
    public void downloadVoucher(
            HttpServletResponse httpResponse,
            String reservationRequestId,
            Principal principal
    ) throws IOException, JRException {

        httpResponse.setContentType("application/pdf");

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename:voucher.pdf";
        httpResponse.setHeader(headerKey, headerValue);

        jasperReportService.buildAndExportVoucher(
                httpResponse,
                new ObjectId(reservationRequestId),
                getUserId(principal)
        );
    }

}
