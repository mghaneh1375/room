package bogen.studio.Room.Enums;

public enum DiscountPlace {

    ROOM_DISCOUNT("تخفیف اتاق"),
    BOOM_DISCOUNT("تخفیف اقامت گاه"),
    ;

    private String persianValue;
    DiscountPlace(String persianValue) {
        this.persianValue = persianValue;
    }

    public String gtPersianValue() {
        return persianValue;
    }

}
