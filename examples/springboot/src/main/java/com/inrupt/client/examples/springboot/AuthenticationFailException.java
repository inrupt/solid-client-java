package com.inrupt.client.examples.springboot;

public class AuthenticationFailException extends RuntimeException {

    private static final long serialVersionUID = 3854695545552441025L;

    /**
     * Create a WebId exception.
     *
     * @param message the message
     */
    public AuthenticationFailException(final String message) {
        super(message);
    }

    /**
     * Create a WebId exception.
     *
     * @param message the message
     * @param cause the cause
     */
    public AuthenticationFailException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
