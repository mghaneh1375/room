package bogen.studio.Room.Enums;

public enum AdditionalFacility {

    TERRACE("تراس"), SERVICE_ROOM("سرویس فرنگی"), IRAN_REST_ROOM("سرویس ایرانی"), BATH("حمام"), KITCHEN("آشپزخانه");

    private String faValue;

    AdditionalFacility(String fa) { faValue = fa; }

    public String getName() {
        return name().toLowerCase();
    }

    public String toFarsi() {
        return faValue;
    }
}
