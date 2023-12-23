package bogen.studio.Room.Service;

import bogen.studio.Room.Enums.ReservationStatus;
import bogen.studio.Room.Exception.InvalidRequestByCustomerException;
import bogen.studio.Room.Models.*;
import bogen.studio.Room.documents.Place;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.common.commonkoochita.Utility.JalaliCalendar;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
@Slf4j
public class JasperReportService {

    private final ReservationRequestService reservationRequestService;
    private final FinancialReportService financialReportService;
    private final NeshanMapService neshanMapService;
    private final BoomService boomService;
    private final RoomService roomService;
    private final PlaceService placeService;

    public void buildAndExportVoucher(HttpServletResponse httpResponse, ObjectId requestId) throws IOException, JRException {

        // Fetch reservation request
        ReservationRequest request = reservationRequestService.findById(requestId);
        isRequestStatusBooked(request.getStatus());

        // Read and compile jrxml file
        //JasperReport jasperReport = loadAndCompileJrxmlFile("classpath:Blank_A4.jrxml");
        //JasperReport jasperReport = loadAndCompileJrxmlFile("classpath:test11.jrxml");
        //JasperReport jasperReport = loadAndCompileJrxmlFile("classpath:test13.jrxml");
        //JasperReport jasperReport = loadAndCompileJrxmlFile("classpath:test14.jrxml");
        JasperReport jasperReport = loadAndCompileJrxmlFile("classpath:test15.jrxml");

        // Create data source ...
        JRBeanCollectionDataSource dataSource = createDataSource(request);

        // Create Parameter Map
        Map<String, Object> parameterMap = createParametereMap(request);

        // Fill jasper report. If you do not have any data source, then use: new JREmptyDataSource()
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameterMap, dataSource);

        // Insert map of the boom
        String mapPath = neshanMapService.fetchBoomMapPath(request.getRoomId());


        // If you want to save the pdf file
        //JasperExportManager.exportReportToPdfFile(jasperPrint, "r31.pdf");

        // Stream the pdf file to the endpoint
        JasperExportManager.exportReportToPdfStream(jasperPrint, httpResponse.getOutputStream());
    }


    private Map<String, Object> createParametereMap(ReservationRequest request) {
        /* Create parameter map, which contains data regarding reservation request applicant
         * 1. Load map file
         * 2. Build creator info
         * 3. Define map and put params in it
         * 4. Return map*/

        // 0.
        VoucherData voucherData = getVoucherData(request);

        // 1.
        String mapPath = neshanMapService.fetchBoomMapPath(request.getRoomId());
        File mapFile = new File(mapPath);
        File koochitaLogoFile = new File("./room/src/main/resources/voucher_images/koochitaLogo.png");

        // 2.
        ReservationCreatorInfo creatorInfo = financialReportService.buildCreatorInfo(request);

        // 3.
        Map<String, Object> parameterMap = new HashMap<>();

        parameterMap.put("creator_first_name", creatorInfo.getNameFa());
        parameterMap.put("creator_last_name", creatorInfo.getLastNameFa());
        parameterMap.put("creator_phone", creatorInfo.getPhone());

        parameterMap.put("boomMap", mapFile.toString());
        parameterMap.put("koochitaLogo", koochitaLogoFile.toString());
        parameterMap.put("boom_address", voucherData.getBoomAddress());
        parameterMap.put("room_name", voucherData.getRoomName());
        parameterMap.put("residence_start_date", voucherData.getJalaliResidenceStartDate());
        parameterMap.put("number_of_staying_nights", voucherData.getNumberOfStayingNights());
        parameterMap.put("tracking_code", voucherData.getTrackingCode());

        // Todo: define params of voucher data in jasper studio

        // 4.
        return parameterMap;
    }

    private JRBeanCollectionDataSource createDataSource(ReservationRequest request) {
        /* Create data source for table in the voucher, which contains passengers information */

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

        return new JRBeanCollectionDataSource(voucherPassengerInfos);
    }

    private JasperReport loadAndCompileJrxmlFile(String classpathPath) throws FileNotFoundException, JRException {
        /* Load Jrxml file and compile it */

        File file = ResourceUtils.getFile(classpathPath);
        JasperDesign jasperDesign = JRXmlLoader.load(file.getAbsolutePath());

        return JasperCompileManager.compileReport(jasperDesign);
    }

    private void isRequestStatusBooked(ReservationStatus status) {
        /* Voucher can only be built for booked reservations */

        if (!status.equals(ReservationStatus.BOOKED)) {
            throw new InvalidRequestByCustomerException("امکان صدور ووچر برای این درخواست رزرو امکان پذیر نیست");
        }

    }

    private VoucherData getVoucherData(ReservationRequest request) {
        /* Get data required for generating voucher
         * 1. Fetch room
         * 2. Fetch boom
         * 3. Fetch place
         * 4. Create and return VoucherData
         *  */

        // 1.
        Room room = roomService.findById(request.getRoomId());
        // 2.
        Boom boom = boomService.findById(room.getBoomId());
        // 3.
        Place place = placeService.fetchById(boom.getPlaceId());

        // 4.
        return new VoucherData()
                .setBoomAddress(place.getAddress())
                .setRoomName(room.getTitle())
                .setJalaliResidenceStartDate(convertResidenceStartDateToJalali(request.getResidenceStartDate()))
                .setNumberOfStayingNights(request.getNumberOfStayingNights())
                .setTrackingCode(request.getTrackingCode());

    }

    private String convertResidenceStartDateToJalali(LocalDateTime residenceStartTime) {
        /* Convert residence start date from gregorian to jalali */

        return JalaliCalendar
                .gregorianToJalali(
                        new JalaliCalendar.YearMonthDate(
                                residenceStartTime.get(ChronoField.YEAR),
                                residenceStartTime.get(ChronoField.MONTH_OF_YEAR) - 1,
                                residenceStartTime.get(ChronoField.DAY_OF_MONTH)
                        )
                )
                .toString();
    }


}















