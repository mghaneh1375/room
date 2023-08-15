package bogen.studio.Room.Enums;

public enum Access {

    ADMIN, GROUP, REPORTER, TRIP_REPORTER, WAREHOUSE_KEEPER, JAHADI;

    public String getName() {
        return name().toLowerCase();
    }

}
