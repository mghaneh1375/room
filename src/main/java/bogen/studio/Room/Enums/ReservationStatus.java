package bogen.studio.Room.Enums;

public enum ReservationStatus {

    ACCEPT, REJECT, PENDING, CANCELED, RESERVED;

    public String getName() {
        return name().toLowerCase();
    }

}
