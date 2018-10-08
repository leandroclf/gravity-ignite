package br.com.trustsystems.gravity.core.worker.exceptions;

import br.com.trustsystems.gravity.exceptions.UnRetriableException;

public class UnknownProtocalException extends UnRetriableException {

    /**
     * Creates a new instance.
     */
    public UnknownProtocalException() {
    }

    /**
     * Creates a new instance.
     */
    public UnknownProtocalException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance.
     */
    public UnknownProtocalException(String message) {
        super(message);
    }

    /**
     * Creates a new instance.
     */
    public UnknownProtocalException(Throwable cause) {
        super(cause);
    }

}