package ru.nsu.aeliseev2.taskj1.protocol;

/**
 * Thrown on protocol violation.
 */
public class ProtocolException extends Exception {
    /**
     * Initializes an instance of {@code ProtolException}.
     *
     * @param message The exception message.
     */
    public ProtocolException(String message) {
        super(message);
    }

    /**
     * Initializes an instance of {@code ProtolException}.
     *
     * @param cause The exception that caused this exception.
     */
    public ProtocolException(Throwable cause) {
        super(cause);
    }
}
