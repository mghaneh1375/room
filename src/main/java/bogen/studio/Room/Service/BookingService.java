package bogen.studio.Room.Service;

import bogen.studio.Room.Enums.ReservationStatus;
import bogen.studio.Room.Exception.InvalidIdException;
import bogen.studio.Room.Exception.InvalidInputException;
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
        // Todo: Save payment info in separate collection

        try {
            // Set new status and update status history
            request.setStatus(ReservationStatus.BOOKED);
            request.addToReservationStatusHistory(new ReservationStatusDate(LocalDateTime.now(), ReservationStatus.BOOKED));
            reservationRequestRepository.save(request);

        } catch (OptimisticLockingFailureException e) {
            // We can not rollback since the payment has been done!
            log.warn(String.format("Optimistic lock activated while changing request: %s, to %s", reservationRequestId, ReservationStatus.BOOKED));
            log.error(String.format("For support team attention. Payment is done for request: %s, but there was a problem while updating reservation status to Booked.", reservationRequestId));
            // Todo: Insert info in a Separate collection and develop a service for support team to handle the situation.
        }

        // Todo: create financial report

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
