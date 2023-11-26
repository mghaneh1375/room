package bogen.studio.Room.Exception;

public class RoomExceedCapacityException extends RuntimeException{

    private int roomMaxCapacity;

    public RoomExceedCapacityException(String message, int roomMaxCapacity) {

        super(message);
        this.roomMaxCapacity = roomMaxCapacity;
    }
}
