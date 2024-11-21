/*
 * Copyright Inrupt Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.inrupt.client.accessgrant;

import com.inrupt.client.util.URIBuilder;

import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A class for representing Access Grant query filters.
 *
 * @param <T> the credential type
 */
public class CredentialFilter<T extends AccessCredential> {

    /** The default size of result pages. */
    public static final int DEFAULT_PAGE_SIZE = 20;
    /** The maximum result page size. */
    public static final int MAX_PAGE_SIZE = 100;

    private static final Map<String, String> TYPE_VALUES = buildTypeValues();

    private final URI fromAgent;
    private final URI toAgent;
    private final CredentialStatus status;
    private final URI resource;
    private final URI purpose;
    private final CredentialDuration revokedWithin;
    private final CredentialDuration issuedWithin;
    private final String page;
    private final int pageSize;
    private final Class<T> clazz;

    CredentialFilter(final URI fromAgent, final URI toAgent, final CredentialStatus status, final URI resource,
            final URI purpose, final CredentialDuration issuedWithin, final CredentialDuration revokedWithin,
            final int pageSize, final String page, final Class<T> clazz) {

        this.clazz = Objects.requireNonNull(clazz, "The clazz parameter must not be null!");
        this.fromAgent = fromAgent;
        this.toAgent = toAgent;
        this.status = status;
        this.resource = resource;
        this.purpose = purpose;
        this.revokedWithin = revokedWithin;
        this.issuedWithin = issuedWithin;
        this.page = page;
        this.pageSize = pageSize;
    }

    /**
     * A filter for an agent that issued credentials.
     *
     * @return the agent issuer, if present
     */
    public Optional<URI> getFromAgent() {
        return Optional.ofNullable(fromAgent);
    }

    /**
     * A filter for an agent that is the target of credentials.
     *
     * @return the target agent, if present
     */
    public Optional<URI> getToAgent() {
        return Optional.ofNullable(toAgent);
    }

    /**
     * A filter for credential status.
     *
     * @return the credential status, if present
     */
    public Optional<CredentialStatus> getStatus() {
        return Optional.ofNullable(status);
    }

    /**
     * A filter for a target resource.
     *
     * @return the credential target resource, if present
     */
    public Optional<URI> getResource() {
        return Optional.ofNullable(resource);
    }

    /**
     * A filter for a target purpose.
     *
     * @return the credential target purpose, if present
     */
    public Optional<URI> getPurpose() {
        return Optional.ofNullable(purpose);
    }

    /**
     * A filter for credential issuance.
     *
     * @return the credential issuance filter, if present
     */
    public Optional<CredentialDuration> getIssuedWithin() {
        return Optional.ofNullable(issuedWithin);
    }

    /**
     * A filter for credential revocation.
     *
     * @return the credential revocation filter, if present
     */
    public Optional<CredentialDuration> getRevokedWithin() {
        return Optional.ofNullable(revokedWithin);
    }

    /**
     * A filter for a result page.
     *
     * @return the page indicator, if present
     */
    public Optional<String> getPage() {
        return Optional.ofNullable(page);
    }

    /**
     * The page size for result sets.
     *
     * @return the result set size
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * Convert the filter to a URI.
     *
     * @param baseUrl the base URL for the filter
     * @return the formatted URI
     */
    public URI asURI(final URI baseUrl) {
        final URIBuilder builder = URIBuilder.newBuilder(baseUrl)
            .queryParam("type", TYPE_VALUES.get(getCredentialType().getSimpleName()))
            .queryParam("pageSize", Integer.toString(getPageSize()));

        getPurpose().map(URI::toString).ifPresent(p -> builder.queryParam("purpose", p));
        getResource().map(URI::toString).ifPresent(r -> builder.queryParam("resource", r));
        getFromAgent().map(URI::toString).ifPresent(a -> builder.queryParam("fromAgent", a));
        getToAgent().map(URI::toString).ifPresent(a -> builder.queryParam("toAgent", a));

        getStatus().map(CredentialStatus::getValue).ifPresent(s -> builder.queryParam("status", s));
        getIssuedWithin().map(CredentialDuration::name).ifPresent(d -> builder.queryParam("issuedWithin", d));
        getRevokedWithin().map(CredentialDuration::name).ifPresent(d -> builder.queryParam("revokedWithin", d));

        getPage().ifPresent(p -> builder.queryParam("page", p));

        return builder.build();
    }

    /* package private */
    Class<T> getCredentialType() {
        return clazz;
    }

    /**
     * Create a new CredentialFilter builder.
     *
     * @return the builder
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Create a new CredentialFilter builder from an existing filter.
     *
     * @param <T> the credential type
     * @param filter an existing credential filter
     * @return the builder
     */
    public static <T extends AccessCredential> Builder newBuilder(final CredentialFilter<T> filter) {
        final Builder builder = new Builder().pageSize(filter.getPageSize());
        filter.getPurpose().ifPresent(builder::purpose);
        filter.getResource().ifPresent(builder::resource);
        filter.getFromAgent().ifPresent(builder::fromAgent);
        filter.getToAgent().ifPresent(builder::toAgent);
        filter.getStatus().ifPresent(builder::status);
        filter.getIssuedWithin().ifPresent(builder::issuedWithin);
        filter.getRevokedWithin().ifPresent(builder::revokedWithin);
        filter.getPage().ifPresent(builder::page);
        return builder;
    }

    /**
     * The CredentialFilter builder.
     */
    public static class Builder {
        private URI builderPurpose;
        private URI builderResource;
        private URI builderFromAgent;
        private URI builderToAgent;
        private CredentialStatus builderStatus;
        private CredentialDuration builderIssuedWithin;
        private CredentialDuration builderRevokedWithin;
        private String builderPage;
        private int builderPageSize = DEFAULT_PAGE_SIZE;

        /* Package private */
        Builder() {
        }

        /**
         * Add a purpose filter.
         *
         * @param purpose the purpose identifier
         * @return this builder
         */
        public Builder purpose(final URI purpose) {
            builderPurpose = purpose;
            return this;
        }

        /**
         * Add a resource filter.
         *
         * @param resource the resource identifier
         * @return this builder
         */
        public Builder resource(final URI resource) {
            builderResource = resource;
            return this;
        }

        /**
         * Add a fromAgent filter.
         *
         * @param fromAgent the agent identifier
         * @return this builder
         */
        public Builder fromAgent(final URI fromAgent) {
            builderFromAgent = fromAgent;
            return this;
        }

        /**
         * Add a toAgent filter.
         *
         * @param toAgent the agent identifier
         * @return this builder
         */
        public Builder toAgent(final URI toAgent) {
            builderToAgent = toAgent;
            return this;
        }

        /**
         * Add a status filter.
         *
         * @param status the status value
         * @return this builder
         */
        public Builder status(final CredentialStatus status) {
            builderStatus = status;
            return this;
        }

        /**
         * Add an issuance date filter.
         *
         * @param issuedWithin the issuance date filter
         * @return this builder
         */
        public Builder issuedWithin(final CredentialDuration issuedWithin) {
            builderIssuedWithin = issuedWithin;
            return this;
        }

        /**
         * Add a revocation date filter.
         *
         * @param revokedWithin the revocation date filter
         * @return this builder
         */
        public Builder revokedWithin(final CredentialDuration revokedWithin) {
            builderRevokedWithin = revokedWithin;
            return this;
        }

        /**
         * Add a page indicator.
         *
         * @param page the page indicator
         * @return this builder
         */
        public Builder page(final String page) {
            builderPage = page;
            return this;
        }

        /**
         * Indicate the size of a page.
         *
         * @param pageSize the size of a page
         * @return this builder
         */
        public Builder pageSize(final int pageSize) {
            if (pageSize > 0 && pageSize < MAX_PAGE_SIZE) {
                builderPageSize = pageSize;
            } else {
                builderPageSize = DEFAULT_PAGE_SIZE;
            }
            return this;
        }

        /**
         * Build a credential filter.
         *
         * @param <T> the credential type
         * @param clazz the credential type
         * @return the credential filter
         */
        public <T extends AccessCredential> CredentialFilter<T> build(final Class<T> clazz) {
            return new CredentialFilter<>(builderFromAgent, builderToAgent, builderStatus, builderResource,
                    builderPurpose, builderIssuedWithin, builderRevokedWithin, builderPageSize, builderPage, clazz);
        }
    }

    /**
     * The duration values for filtering access credentials.
     */
    public enum CredentialDuration {

        /** 1 Day. */
        P1D(Duration.ofDays(1)),
        /** 1 Week. */
        P7D(Duration.ofDays(7)),
        /** 1 Month. */
        P1M(Duration.ofDays(30)),
        /** 3 Months. */
        P3M(Duration.ofDays(90));

        private final Duration value;

        CredentialDuration(final Duration value) {
            this.value = value;
        }

        /**
         * Get the value of the duration.
         *
         * @return the duration filter value
         */
        public Duration asDuration() {
            return this.value;
        }
    }

    /**
     * The status values for filtering access credentials.
     */
    public enum CredentialStatus {

        /** Pending credentials. */
        PENDING("Pending"),
        /** Deined credentials. */
        DENIED("Denied"),
        /** Granted credentials. */
        GRANTED("Granted"),
        /** Canceled credentials. */
        CANCELED("Canceled"),
        /** Expired credentials. */
        EXPIRED("Expired"),
        /** Active credentials. */
        ACTIVE("Active"),
        /** Revoked credentials. */
        REVOKED("Revoked");

        private final String value;

        CredentialStatus(final String value) {
            this.value = value;
        }

        /**
         * Get the value of status filter.
         *
         * @return the value
         */
        public String getValue() {
            return value;
        }
    }

    private static Map<String, String> buildTypeValues() {
        final Map<String, String> values = new HashMap<>();
        values.put("AccessGrant", "SolidAccessGrant");
        values.put("AccessDenial", "SolidAccessDenial");
        values.put("AccessRequest", "SolidAccessRequest");
        return values;
    }
}
