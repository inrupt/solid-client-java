package com.inrupt.client;

import java.net.URI;

/**
 * This package-private mutable class is used for JSON deserialization.
 * Once instantiated, it is used to build an immutable {@link ProblemDetails}.
 */
class ProblemDetailsData {
    private URI type;
    private String title;
    private String details;
    private int status;
    private URI instance;

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

    public void setType(URI type) {
        this.type = type;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setInstance(URI instance) {
        this.instance = instance;
    }
}
