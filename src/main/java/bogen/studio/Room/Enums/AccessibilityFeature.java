package bogen.studio.Room.Enums;

public enum AccessibilityFeature {

    WITH_STAIR, WITH_OUT_STAIR, ELEVATOR;

    public String getName() {
        return name().toLowerCase();
    }

}
