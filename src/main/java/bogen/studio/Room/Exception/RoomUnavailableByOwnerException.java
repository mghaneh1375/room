package bogen.studio.Room.Exception;

public class RoomUnavailableByOwnerException extends RuntimeException{

    public RoomUnavailableByOwnerException(String message) {
        super(message);
    }
}
