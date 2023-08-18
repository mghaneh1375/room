package bogen.studio.Room.Enums;

public enum Welfare {

    FURNITURE, CLOSET, CABINET, DESK, TELEVISION, SOUND, FIREPLACE, HEATING, LAUNDRY,
    COOLING, DISH_WASHING, TANK, OVEN, DISHES, IRON, TEA_MAKER, COFFEE_MAKER;

    public String getName() {
        return name().toLowerCase();
    }

}
