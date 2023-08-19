package bogen.studio.Room.Enums;

public enum ReservationStatus {

    ACCEPT, REJECT, CANCELED, RESERVED, REFUND, ACCEPT_CANCELED, PAID, FINISH;

    public String getName() {
        return name().toLowerCase();
    }

}
