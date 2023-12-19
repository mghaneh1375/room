package bogen.studio.Room.Service;

import bogen.studio.Room.Enums.ReservationStatus;
import bogen.studio.Room.Enums.RoomStatus;
import bogen.studio.Room.Exception.InvalidRequestByCustomerException;
import bogen.studio.Room.Models.PassengerInfo;
import bogen.studio.Room.Models.ReservationCreatorInfo;
import bogen.studio.Room.Models.ReservationRequest;
import bogen.studio.Room.Models.VoucherPassengerInfo;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



@Service
@RequiredArgsConstructor
public class JasperReportService {

    private final ReservationRequestService reservationRequestService;
    private final FinancialReportService financialReportService;

    public void buildAndExportVoucher(HttpServletResponse httpResponse, ObjectId requestId) throws IOException, JRException {

        // Fetch reservation request
        ReservationRequest request = reservationRequestService.findById(requestId);
        isRequestStatusBooked(request.getStatus());

        // Read and compile jrxml file
        //File file = ResourceUtils.getFile("classpath:Blank_A4.jrxml");
        File file = ResourceUtils.getFile("classpath:test11.jrxml");
        JasperDesign jasperDesign = JRXmlLoader.load(file.getAbsolutePath());
        JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);

        // Create data source ...
        List<PassengerInfo> passengerInfoList = financialReportService.buildPassengersInfo(request);
        List<VoucherPassengerInfo> voucherPassengerInfos = new ArrayList<>();
        for (int i = 0; i < passengerInfoList.size(); i++) {
            voucherPassengerInfos.add(
                    new VoucherPassengerInfo(
                            i + 1,
                            passengerInfoList.get(i).getNameFa(),
                            passengerInfoList.get(i).getLastNameFa(),
                            passengerInfoList.get(i).getPhone()
                    )
            );
        }
        //List<PassengerInfo> passengerInfos = new ArrayList<>();
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(voucherPassengerInfos);

        // Create Parameter Map
        ReservationCreatorInfo creatorInfo = financialReportService.buildCreatorInfo(request);
        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("creator_first_name", creatorInfo.getNameFa());
        parameterMap.put("creator_last_name", creatorInfo.getLastNameFa());
        parameterMap.put("creator_phone", creatorInfo.getPhone());




        //JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameterMap, new JREmptyDataSource());
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameterMap, dataSource);

        JasperExportManager.exportReportToPdfFile(jasperPrint, "r31.pdf");
        JasperExportManager.exportReportToPdfStream(jasperPrint, httpResponse.getOutputStream());



    }

    private void isRequestStatusBooked(ReservationStatus status) {
        /* Voucher can only be built for booked reservations */

        if (!status.equals(ReservationStatus.BOOKED)) {
            throw new InvalidRequestByCustomerException("امکان صدور ووچر برای این درخواست رزرو امکان پذیر نیست");
        }

    }

}















