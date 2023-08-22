package bogen.studio.Room.Enums;

public enum FoodFacility {

    FREE_BREAKFAST("صبحانه رایگان"), NON_FREE_BREAKFAST("صبحانه غیر رایگان"), FREE_LAUNCH("ناهار رایگان"),
    NON_FREE_LAUNCH("ناهار غیر رایگان"), FREE_DINNER("شام رایگان"), NON_FREE_DINNER("شام غیز رایگان");

    private String faValue;

    FoodFacility (String fa) {
        faValue = fa;
    }

    public String getName() {
        return name().toLowerCase();
    }

    public String toFarsi() {
        return faValue;
    }

}
