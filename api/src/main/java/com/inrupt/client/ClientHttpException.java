package com.inrupt.client;

public class ClientHttpException extends InruptClientException {
    private final ProblemDetails problemDetails;

    /**
     * Create a ClientHttpException.
     * @param problemDetails the {@link ProblemDetails} instance
     * @param message the exception message
     */
    public ClientHttpException(final ProblemDetails problemDetails, final String message) {
        super(message);
        this.problemDetails = problemDetails;
    }

    /**
     * Create a ClientHttpException.
     * @param problemDetails the {@link ProblemDetails} instance
     * @param message the exception message
     * @param cause a wrapped exception cause
     */
    public ClientHttpException(final ProblemDetails problemDetails, final String message, final Exception cause) {
        super(message, cause);
        this.problemDetails = problemDetails;
    }

    public ProblemDetails getProblemDetails() {
        return this.problemDetails;
    }
}
