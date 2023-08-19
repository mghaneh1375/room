package bogen.studio.Room.Enums;

public enum Welfare {

    FURNITURE("مبلمان"), CLOSET("کمد"), CABINET("کابینت"), DESK("میز"), TELEVISION("تلوزیون"),
    SOUND("سیستم صوتی"), FIREPLACE("شومینه"), HEATING("سیستم گرمایشی"), LAUNDRY("ماشین لباس شویی"),
    COOLING("سیستم سرمایشی"), DISH_WASHING("ماشین ظرف شویی"), TANK("حوض"), OVEN("گاز"),
    DISHES("سرویس ظروف"), IRON("اتو"), TEA_MAKER("چایی ساز"), COFFEE_MAKER("قهوه ساز");

    private String faValue;

    Welfare(String fa) { faValue = fa; }

    public String getName() {
        return name().toLowerCase();
    }

    public String toFarsi() {
        return faValue;
    }
}
