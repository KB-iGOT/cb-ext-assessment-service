package com.igot.cb.core.exception;

public class CassandraPropertyReaderException extends RuntimeException{
    /**
     * Constructs a new CassandraPropertyReaderException with the specified error message and cause.
     *
     * @param message The error message associated with the exception.
     * @param cause   The cause of the exception.
     */
    public CassandraPropertyReaderException(String message, Throwable cause) {
        super(message, cause);
    }
}
