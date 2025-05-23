package Exceptions;
@SuppressWarnings("serial")

/**

  Custom exception class for interpreter-related errors.

 **/
public class InterpreterException extends Exception {
    // Constructors
    public InterpreterException() {
        super();
    }

    public InterpreterException(String message) {
        super(message);
    }

    public InterpreterException(String message, Throwable cause) {
        super(message, cause);
    }

    public InterpreterException(Throwable cause) {
        super(cause);
    }
}
