package br.com.trustsystems.gravity.core.worker.exceptions;


import br.com.trustsystems.gravity.core.worker.state.messages.base.IOTMessage;
import br.com.trustsystems.gravity.exceptions.UnRetriableException;

public class ShutdownException extends UnRetriableException {

    private final boolean disconnect;
    private IOTMessage response = null;

    public ShutdownException(IOTMessage response) {
        this.response = response;
        this.disconnect = false;
    }

    /**
     * Creates a new instance.
     */
    public ShutdownException(boolean disconnect) {
        this.disconnect = disconnect;
    }

    /**
     * Creates a new instance.
     */
    public ShutdownException() {
        this.disconnect = false;
    }

    /**
     * Creates a new instance.
     */
    public ShutdownException(String message, Throwable cause) {
        super(message, cause);
        this.disconnect = false;
    }

    /**
     * Creates a new instance.
     */
    public ShutdownException(String message) {
        super(message);
        this.disconnect = false;
    }

    /**
     * Creates a new instance.
     */
    public ShutdownException(Throwable cause) {
        super(cause);
        this.disconnect = false;
    }


    public IOTMessage getResponse() {
        return response;
    }

    public boolean isDisconnect() {
        return disconnect;
    }
}
