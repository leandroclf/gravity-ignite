package br.com.trustsystems.gravity.core.worker.exceptions;

import br.com.trustsystems.gravity.exceptions.UnRetriableException;

public class TopicSubscriptionException extends UnRetriableException {

    /**
     * Creates a new instance.
     */
    public TopicSubscriptionException() {
    }

    /**
     * Creates a new instance.
     */
    public TopicSubscriptionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance.
     */
    public TopicSubscriptionException(String message) {
        super(message);
    }

    /**
     * Creates a new instance.
     */
    public TopicSubscriptionException(Throwable cause) {
        super(cause);
    }
}
