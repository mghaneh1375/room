package bogen.studio.Room.Enums;

public enum FoodFacility {

    FREE_BREAKFAST, NON_FREE_BREAKFAST, FREE_LAUNCH, NON_LAUNCH, FREE_DINNER, NON_DINNER;

    public String getName() {
        return name().toLowerCase();
    }

}
