package bogen.studio.Room.Enums;

public enum Limitation {

    DRUG("دخانیات"), SHOE("کفش");

    private String faValue;
    Limitation(String fa) {
        faValue = fa;
    }

    public String getName() {
        return name().toLowerCase();
    }

    public String toFarsi() {
        return faValue;
    }
}
