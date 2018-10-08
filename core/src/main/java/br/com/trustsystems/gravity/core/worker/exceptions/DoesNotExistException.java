package br.com.trustsystems.gravity.core.worker.exceptions;

import br.com.trustsystems.gravity.exceptions.UnRetriableException;

public class DoesNotExistException extends UnRetriableException {

    /**
     * Creates a new instance.
     */
    public DoesNotExistException() {
    }

    /**
     * Creates a new instance.
     */
    public DoesNotExistException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance.
     */
    public DoesNotExistException(String message) {
        super(message);
    }

    /**
     * Creates a new instance.
     */
    public DoesNotExistException(Throwable cause) {
        super(cause);
    }
}
