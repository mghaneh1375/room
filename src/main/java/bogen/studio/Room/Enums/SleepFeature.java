package bogen.studio.Room.Enums;

public enum SleepFeature {

    SINGLE("سینگل"), DOUBLE("دو تخته"), COUPLE("کویین"), BED_LINEN("رخت خواب");

    private String faValue;

    SleepFeature(String fa) { faValue = fa; }
    public String getName() {
        return name().toLowerCase();
    }

    public String toFarsi() {
        return faValue;
    }
}
