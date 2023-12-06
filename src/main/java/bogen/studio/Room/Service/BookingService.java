package bogen.studio.Room.Service;

import bogen.studio.Room.Enums.ReservationStatus;
import bogen.studio.Room.Enums.RoomStatus;
import bogen.studio.Room.Exception.InvalidIdException;
import bogen.studio.Room.Exception.InvalidInputException;
import bogen.studio.Room.Exception.PaymentTimeoutException;
import bogen.studio.Room.Models.ReservationRequest;
import bogen.studio.Room.Models.ReservationStatusDate;
import bogen.studio.Room.Repository.ReservationRequestRepository;
import bogen.studio.Room.documents.FinancialReport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingService {

    private final ReservationRequestRepository reservationRequestRepository;
    private final RoomDateReservationStateService roomDateReservationStateService;
    private final FinancialReportService financialReportService;

    public void payRoomFee_underDevelopment(ObjectId reservationRequestId) {
        /* This method will handle payment of room fee */

        // Fetch reservation
        ReservationRequest request = reservationRequestRepository
                .findById(reservationRequestId)
                .orElseThrow(() -> new InvalidIdException("این درخواست رزرو در سیستم وجود ندارد"));

        // Verify reservation status
        isStatusWaitForPayment(request.getStatus());

        // Handle payment
        // Todo: handle payment
        log.info(String.format("Successful payment for reservation: %s", reservationRequestId));

        // Todo: If payment was not successful, do not proceed the method

        // Todo: Save payment info in separate collection

        try {
            // Set new status and update status history
            // Todo: set payment in request (field: paid). The following lines sets a temporary value;
            request.setPaid(1000); //
            request.setStatus(ReservationStatus.BOOKED);
            request.addToReservationStatusHistory(new ReservationStatusDate(LocalDateTime.now(), ReservationStatus.BOOKED));
            reservationRequestRepository.save(request);
            log.info(String.format("Status for reservation request: %s, changed to: %s", request.get_id(), ReservationStatus.BOOKED));

            // Set room date status to booked
            roomDateReservationStateService.setRoomDateStatuses(
                    request.getRoomId(),
                    request.getResidenceStartDate(),
                    request.getNumberOfStayingNights(),
                    ReservationStatus.BOOKED,
                    RoomStatus.BOOKED
            );

        } catch (OptimisticLockingFailureException e) {
            // We can not rollback since the payment has been done!
            log.warn(String.format("Optimistic lock activated while changing request: %s, to %s", reservationRequestId, ReservationStatus.BOOKED));
            log.error(String.format("For support team attention. Payment is done for request: %s, but there was a problem while updating reservation status to Booked.", reservationRequestId));
            // Todo: Insert info in a Separate collection and develop a service for support team to handle the situation.
        }

        // Create financial report
        buildAndInsertFinancialReport(request);
    }

    private void buildAndInsertFinancialReport(ReservationRequest request) {
        /* This method builds a financial report for booked reservation request, then inserts it in to the database */

        FinancialReport financialReport = financialReportService.buildFinancialReport(request);
        financialReportService.insert(financialReport);
        log.info(String.format("Financial report inserted for reservation request: %s", request.get_id()));

    }

    private void isStatusWaitForPayment(ReservationStatus status) {
        /* This method verifies that status of reservation is either wait-for-payment-1 or wait-for-payment-2 */

        if (status == ReservationStatus.CANCEL_BY_PAYMENT_1_TIMEOUT || status == ReservationStatus.CANCEL_BY_PAYMENT_2_TIMEOUT) {
            throw new PaymentTimeoutException("درخواست رزرو شما لحظاتی پیش منقضی شد. لطفا دوباره برای رزرو اتاق اقدام نمایید");
        }

        if (!(status == ReservationStatus.WAIT_FOR_PAYMENT_1) && !(status == ReservationStatus.WAIT_FOR_PAYMENT_2)) {
            throw new InvalidInputException("شما قادر به پرداخت مبلغ در این مرحله نیستید");
        }
    }


}
