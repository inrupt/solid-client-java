package com.inrupt.client;

public class ClientHttpException extends InruptClientException {
    private final ProblemDetails problemDetails;

    /**
     * Create a ClientHttpException.
     * @param message the exception message
     */
    public ClientHttpException(ProblemDetails problemDetails, String message) {
        super(message);
        this.problemDetails = problemDetails;
    }

    /**
     * Create a ClientHttpException.
     * @param message the exception message
     * @param cause a wrapped exception cause
     */
    public ClientHttpException(ProblemDetails problemDetails, String message, Exception cause) {
        super(message, cause);
        this.problemDetails = problemDetails;
    }

    public ProblemDetails getProblemDetails() {
        return this.problemDetails;
    }
}
