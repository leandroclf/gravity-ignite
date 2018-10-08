package br.com.trustsystems.gravity.exceptions;

public class UnRetriableException extends Exception {

    public UnRetriableException() {
    }

    public UnRetriableException(String message) {
        super(message);
    }

    public UnRetriableException(String message, Throwable var2) {
        super(message, var2);
    }

    public UnRetriableException(Throwable message) {
        super(message);
    }
}