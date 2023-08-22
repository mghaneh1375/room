package bogen.studio.Room.Enums;

public enum AccessibilityFeature {

    WITH_STAIR("پله"), WITH_OUT_STAIR("بدون پله"), ELEVATOR("آسانسور");

    private String faValue;

    AccessibilityFeature(String fa) { faValue = fa; }

    public String getName() {
        return name().toLowerCase();
    }

    public String toFarsi() {
        return faValue;
    }
}
