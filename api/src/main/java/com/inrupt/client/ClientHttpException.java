package com.inrupt.client;

import com.inrupt.client.spi.JsonService;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class ClientHttpException extends InruptClientException {
    private ProblemDetails problemDetails;

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

    private static ClientHttpException buildDefaultException(String message, int code, String status) {
        return new ClientHttpException(
                ProblemDetails.fromDefaultResponse(code, status),
                message
        );
    }

    public static ClientHttpException fromResponse(String message, int code, String status, Headers headers, InputStream body, JsonService jsonService) {
        if (!headers.allValues("Content-Type").contains(ProblemDetails.MIME_TYPE)) {
            return buildDefaultException(message, code, status);
        }
        try {
            ProblemDetails pd = jsonService.fromJson(body, ProblemDetails.class);
            return new ClientHttpException(pd, message);
        } catch (IOException e) {
            return buildDefaultException(message, code, status);
        }
    }
}
