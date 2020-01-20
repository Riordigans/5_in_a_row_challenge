package Server;

public class InvalidMoveException extends Exception {

    private static final long serialVersionUID = 7718828512143293558L;

    public InvalidMoveException() {
        super();
    }

    public InvalidMoveException(String message, Throwable cause, boolean enableSuppression,
                                boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public InvalidMoveException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidMoveException(String message) {
        super(message);
    }

    public InvalidMoveException(Throwable cause) {
        super(cause);
    }
}
