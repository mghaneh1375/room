package bogen.studio.Room.Service;

import bogen.studio.Room.Enums.ReservationStatus;
import bogen.studio.Room.Models.PassengerInfo;
import bogen.studio.Room.Models.ReservationCreatorInfo;
import bogen.studio.Room.Models.ReservationRequest;
import bogen.studio.Room.Repository.FinancialReportRepository;
import bogen.studio.Room.documents.FinancialReport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static bogen.studio.Room.Enums.ReservationStatus.REFUNDED;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinancialReportService {

    private final FinancialReportRepository financialReportRepository;

    public FinancialReport insert(FinancialReport r) {

        return financialReportRepository.insert(r);
    }

    public FinancialReport buildFinancialReport(ReservationRequest request) {
        /* This method creates financial report for the booked request */

        FinancialReport financialReport = new FinancialReport()
                .setReservationId(request.get_id())
                .setPurchaseTime(LocalDateTime.now()) // Todo: replace this with payment timestamp
                .setResidenceDates(request.getGregorianResidenceDates())
                .setReservationCreatorInfo(buildCreatorInfo(request))
                .setPassengersInfo(buildPassengersInfo(request))
                .setStatus(request.getStatus())
                .setDescription(request.getDescription())
                .setNumberOfPassengers(request.getPassengers().size());

        if (request.getStatus().equals(ReservationStatus.BOOKED)) {
            financialReport.setPaymentFee(request.getPaid());
        } else if (request.getStatus().equals(REFUNDED)) {
            financialReport.setRefundFee(request.getRefundFee());
        } else {
            log.error(String.format("Unexpected reservation request status for reservation: %s. Expected BOOKED or " +
                    "REFUNDED, got: %s. For developers attention.", request.get_id(), request.getStatus()));
        }

        return financialReport;
    }

    private List<PassengerInfo> buildPassengersInfo(ReservationRequest request) {
        /* This method returns a list of PassengerInfo, containing data regarding residences of the room */

        var passengersData = request.getPassengers();
        List<PassengerInfo> output = new ArrayList<>();

        for (int i = 0; i < passengersData.size(); i++) {

            Document passengerData = request.getPassengers().get(i);

            PassengerInfo passengerInfo = new PassengerInfo()
                    .setNameFa(passengerData.getString("nameFa"))
                    .setNameEn(passengerData.getString("nameEn"))
                    .setLastNameFa(passengerData.getString("lastNameFa"))
                    .setLastNameEn(passengerData.getString("lastNameEn"))
                    .setNID(passengerData.getString("NID"))
                    .setPassportNo(passengerData.getString("passportNo"))
                    .setCitizenNo(passengerData.getString("citizenNo"))
                    .setStayStatus(passengerData.getString("stayStatus"))
                    .setCitizenship(passengerData.getString("citizenship"))
                    .setAgeType(passengerData.getString("ageType"))
                    .setSex(passengerData.getString("sex"))
                    .setMail(passengerData.getString("mail"))
                    .setPhone(passengerData.getString("phone"));

            output.add(passengerInfo);
        }

        return output;
    }

    private ReservationCreatorInfo buildCreatorInfo(ReservationRequest request) {
        /* This method creates an instance of ReservationCreatorInfo and populates it with input reservation
         * request */

        return new ReservationCreatorInfo()
                .setNameFa(request.getCreator().getString("nameFa"))
                .setNameEn(request.getCreator().getString("nameEn"))
                .setLastNameFa(request.getCreator().getString("lastNameFa"))
                .setLastNameEn(request.getCreator().getString("lastNameEn"))
                .setNID(request.getCreator().getString("NID"))
                .setPassportNo(request.getCreator().getString("passportNo"))
                .setCitizenNo(request.getCreator().getString("citizenNo"))
                .setStayStatus(request.getCreator().getString("stayStatus"))
                .setCitizenship(request.getCreator().getString("citizenship"))
                .setAgeType(request.getCreator().getString("ageType"))
                .setSex(request.getCreator().getString("sex"))
                .setMail(request.getCreator().getString("mail"))
                .setPhone(request.getCreator().getString("phone"));
    }

    public void buildAndInsertFinancialReport(ReservationRequest request) {
        /* This method builds a financial report for booked reservation request, then inserts it in to the database */

        FinancialReport financialReport = buildFinancialReport(request);
        insert(financialReport);
        log.info(String.format("Financial report inserted for reservation request: %s", request.get_id()));

    }

}
