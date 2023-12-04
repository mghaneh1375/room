package bogen.studio.Room.Enums;

public enum ReservationStatus {

    REGISTERED_RESERVE_REQUEST,
    WAIT_FOR_PAYMENT_1,
    CANCEL_BY_PAYMENT_1_TIMEOUT,
    CANCEL_BY_PAYMENT_2_TIMEOUT,
    UNSUCCESSFUL_PAYMENT,
    BOOKED,
    CANCEL_BY_CUSTOMER,
    WAIT_FOR_REFUND,
    REFUNDED,
    CANCEL_BY_OWNER,
    WAIT_FOR_OWNER_RESPONSE,
    CANCEL_BY_OWNER_RESPONSE_TIMEOUT,
    ACCEPT_BY_OWNER,
    REJECT_BY_OWNER,
    WAIT_FOR_PAYMENT_2,
    END_OF_RESIDENCE,
    SYSTEM_ERROR


    //ACCEPT,
    //REJECT,
    //CANCELED,
    //PENDING,
    //RESERVED,
    //REFUND,
    //ACCEPT_CANCELED,
    //PAID,
    //FINISH;

    ;
//    public String getName() {
//        return name().toLowerCase();
//    }

}
