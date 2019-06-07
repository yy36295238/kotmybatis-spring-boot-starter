package kot.bootstarter.kotmybatis.exception;

public class KotException extends RuntimeException {
    public KotException(String message, Throwable cause) {
        super(message, cause);
    }

    public KotException(Throwable cause) {
        super(cause);
    }
}
