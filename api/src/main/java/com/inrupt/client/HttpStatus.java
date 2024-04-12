package com.inrupt.client;

import java.util.Arrays;

public final class HttpStatus {

    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int METHOD_NOT_ALLOWED = 405;
    public static final int NOT_ACCEPTABLE = 406;
    public static final int CONFLICT = 409;
    public static final int GONE = 410;
    public static final int PRECONDITION_FAILED = 412;
    public static final int UNSUPPORTED_MEDIA_TYPE = 415;
    public static final int TOO_MANY_REQUESTS = 429;
    public static final int INTERNAL_SERVER_ERROR = 500;

    enum StatusMessages {
        BAD_REQUEST(HttpStatus.BAD_REQUEST, "Bad Request"),
        UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Unauthorized"),
        FORBIDDEN(HttpStatus.FORBIDDEN, "Forbidden"),
        NOT_FOUND(HttpStatus.NOT_FOUND, "Not Found"),
        METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "Method Not Allowed"),
        NOT_ACCEPTABLE(HttpStatus.NOT_ACCEPTABLE, "Not Acceptable"),
        CONFLICT(HttpStatus.CONFLICT, "Conflict"),
        GONE(HttpStatus.GONE, "Gone"),
        PRECONDITION_FAILED(HttpStatus.PRECONDITION_FAILED, "Precondition Failed"),
        UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported Media Type"),
        TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "Too Many Requests"),
        INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");

        private final int code;
        final String message;

        StatusMessages(final int code, final String message) {
            this.code = code;
            this.message = message;
        }

        static String getStatusMessage(final int statusCode) {
            return Arrays.stream(StatusMessages.values())
                .filter(status -> status.code == statusCode)
                .findFirst()
                .map(knownStatus -> knownStatus.message)
                .orElseGet(() -> {
                    // If the status is unknown, default to 400 for client errors and 500 for server errors
                    if (statusCode >= 400 && statusCode <= 499) {
                        return BAD_REQUEST.message;
                    }
                    return INTERNAL_SERVER_ERROR.message;
                });
        }
    }

    // Prevents instantiation.
    private HttpStatus() {
        /* no-op */
    }
}
