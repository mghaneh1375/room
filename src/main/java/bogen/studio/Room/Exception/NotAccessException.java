package bogen.studio.Room.Exception;

public class NotAccessException extends RuntimeException {

    private static final long serialVersionUID = 58L;

    private final String message;

    public NotAccessException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

}
