package bogen.studio.Room.Routes.API.customer;

import bogen.studio.Room.DTO.Digests.PaymentInfoPostDto;
import bogen.studio.Room.Service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/booking")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/pay-room-fee")
    public ResponseEntity<String> payRoomFee(
            @RequestBody PaymentInfoPostDto paymentInfo
            ) {
        // ATTENTION: THE SERVICE IS UNDER DEVELOPMENT
        /* This endpoint is for paying fee of the room. */


        bookingService.payRoomFee_underDevelopment(paymentInfo.getReservationRequestId());

        return ResponseEntity.ok("Payment SuccessFull. \n Attention: Payment service is under development");

    }


}
