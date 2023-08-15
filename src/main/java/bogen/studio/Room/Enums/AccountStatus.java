package bogen.studio.Room.Enums;

public enum AccountStatus {

    ACTIVE, PENDING, BLOCKED;

    public String getName() {
        return name().toLowerCase();
    }

}
