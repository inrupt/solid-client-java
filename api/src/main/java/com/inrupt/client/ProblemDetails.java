package com.inrupt.client;

import java.net.URI;

public class ProblemDetails {
    public static final String MIME_TYPE = "application/problem+json";
    public static final String DEFAULT_TYPE = "about:blank";
    private final URI type;
    private final String title;
    private final String details;
    private final int status;
    private final URI instance;

    public ProblemDetails(
        final URI type,
        final String title,
        final String details,
        final int status,
        final URI instance
    ) {
        // The `type` is not mandatory in RFC9457, so we want to set
        // a default value here even when deserializing from JSON.
        if (type != null) {
            this.type = type;
        } else {
            this.type = URI.create(DEFAULT_TYPE);
        }
        this.title = title;
        this.details = details;
        this.status = status;
        this.instance = instance;
    }

    public URI getType() {
        return this.type;
    };

    public String getTitle() {
        return this.title;
    };

    public String getDetails() {
        return this.details;
    };

    public int getStatus() {
        return this.status;
    };

    public URI getInstance() {
        return this.instance;
    };
}
