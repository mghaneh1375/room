package bogen.studio.Room.Service;

import bogen.studio.Room.Enums.ReservationStatus;
import bogen.studio.Room.Enums.RoomStatus;
import bogen.studio.Room.Exception.InvalidIdException;
import bogen.studio.Room.Exception.InvalidInputException;
import bogen.studio.Room.Exception.PaymentException;
import bogen.studio.Room.Exception.PaymentTimeoutException;
import bogen.studio.Room.Models.ReservationRequest;
import bogen.studio.Room.Models.ReservationStatusDate;
import bogen.studio.Room.Repository.ReservationRequestRepository;
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
    private final DiscountService discountService;
    private final DiscountReportService discountReportService;

    public void payRoomFee_underDevelopment(ObjectId reservationRequestId, String paymentCode) {
        /* This method will handle payment of room fee */

        // Fetch reservation
        ReservationRequest request = reservationRequestRepository
                .findById(reservationRequestId)
                .orElseThrow(() -> new InvalidIdException("این درخواست رزرو در سیستم وجود ندارد"));

        // Verify reservation status
        isStatusWaitForPayment(request.getStatus());

        // Check if payment is accurate.
        isPaymentAmountAccurate_underDevelopment(paymentCode);

        // Handle payment
        // Todo: handle payment
        log.info(String.format("Successful payment for reservation: %s", reservationRequestId));

        // Todo: If payment was not successful, do not proceed the method

        // Todo: Save payment info in separate collection

        // Set new status for reservation request and new status for room
        setNewStatusForReservationRequestAndRoom(request);

        // Create financial report
        financialReportService.buildAndInsertFinancialReport(request);

        // If discount is code type then increment the current-usage-count
        discountService.incrementCurrentUsageCountForCodeDiscount(request);

        // Build and insert discountReport
        discountReportService.buildAndInsertDiscountReportsOfReservationRequest(request);

    }

    private void setNewStatusForReservationRequestAndRoom(ReservationRequest request) {
        try {
            // Set new status and update status history
            // Todo: set payment in request (field: paid). The following lines sets a temporary value;
            request.setPaid(1000); //
            request.setStatus(ReservationStatus.BOOKED);
            request.addToReservationStatusHistory(new ReservationStatusDate(LocalDateTime.now(), ReservationStatus.BOOKED));
            reservationRequestRepository.save(request);
            log.info(String.format("Status for reservation request: %s, changed to: %s", request.get_id(), ReservationStatus.BOOKED));

        } catch (Exception e) {
            /* ATTENTION: We can not rollback since the payment has been done! */
            if (e instanceof OptimisticLockingFailureException) {
                log.warn(String.format("Optimistic lock activated while changing request: %s, to %s", request.get_id(), ReservationStatus.BOOKED));
            }
            log.error(String.format("For support team attention. Payment is done for request: %s, but there was a problem while updating reservation status to Booked.", request.get_id()));
            // Todo: Insert info in a Separate collection and develop a service for support team to handle the situation.
        }

        try {
            // Set room date status to booked
            roomDateReservationStateService.setRoomDateStatuses(
                    request.getRoomId(),
                    request.getResidenceStartDate(),
                    request.getNumberOfStayingNights(),
                    ReservationStatus.BOOKED,
                    RoomStatus.BOOKED
            );
        } catch (Exception e) {

            if (e instanceof OptimisticLockingFailureException) {
                log.warn(String.format("Optimistic lock activated while changing room: %s, for target dates: %s to BOOKED",
                        request.getRoomId(), request.getGregorianResidenceDates()));
            }

            log.error(String.format("For support team attention. Room status for room: %s should be set to BOOKED. Due to successful payment of reservation request: %s", request.getRoomId(), request.get_id()));

        }
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

    private void isPaymentAmountAccurate_underDevelopment(String paymentCode) {
        /* Check if payment is accurate or not */

        // Todo: Check if payment Code matches with the reservation
        // ...

        // Under development...
        boolean isPaymentSuccessful = true;
        if (!isPaymentSuccessful) {
            throw new PaymentException("کد پرداخت معتبر نیست");
        }

    }


}
