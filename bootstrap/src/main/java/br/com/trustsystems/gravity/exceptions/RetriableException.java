package br.com.trustsystems.gravity.exceptions;

public class RetriableException extends Exception {

    public RetriableException() {
    }

    public RetriableException(String message) {
        super(message);
    }

    public RetriableException(String message, Throwable var2) {
        super(message, var2);
    }

    public RetriableException(Throwable message) {
        super(message);
    }
}