package bogen.studio.Room.Exception;

public class PaymentTimeoutException extends RuntimeException{

    public PaymentTimeoutException(String message) {
        super(message);
    }
}
