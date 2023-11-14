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
package com.inrupt.client.solid;

import com.inrupt.client.Headers;
import com.inrupt.client.vocabulary.PIM;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Solid Resource Metadata.
 */
public class Metadata {

    private static final URI STORAGE = URI.create(PIM.getNamespace() + "Storage");
    private static final String CONTENT_TYPE = "Content-Type";

    private final URI acl;
    private final URI storage;
    private final Set<URI> types = new HashSet<>();
    private final Map<String, Set<String>> wacAllow = new HashMap<>();
    private final Set<String> allowedMethods = new HashSet<>();
    private final Set<String> allowedPatchSyntaxes = new HashSet<>();
    private final Set<String> allowedPostSyntaxes = new HashSet<>();
    private final Set<String> allowedPutSyntaxes = new HashSet<>();
    private final String contentType;

    /**
     * The Solid Storage location.
     *
     * @return the storage location, if known.
     */
    public Optional<URI> getStorage() {
        return Optional.ofNullable(storage);
    }

    /**
     * The Solid Resource types.
     *
     * <p>This data typically comes from HTTP Link headers and may be different than
     * {@code rdf:type} data explicitly set on a resource.
     *
     * @return the type values for a resource
     */
    public Set<URI> getTypes() {
        return types;
    }

    /**
     * The WAC-Allow permission information.
     *
     * @return authorization hints as expressed in a WAC-Allow header
     */
    public Map<String, Set<String>> getWacAllow() {
        return wacAllow;
    }

    /**
     * The access control resource location.
     *
     * @return the access control resource location, if present
     */
    public Optional<URI> getAcl() {
        return Optional.ofNullable(acl);
    }

    /**
     * The supported HTTP methods for the associated resource.
     *
     * <p>This collection of methods may be different from the methods that
     * an agent may be authorized to perform.
     *
     * @return the supported HTTP methods
     */
    public Set<String> getAllowedMethods() {
        return allowedMethods;
    }

    /**
     * The supported HTTP PATCH MIME types for the associated resource.
     *
     * <p>This collection represents the PATCH MIME types that a server supports,
     * which may be different from what an agent is authorized to perform.
     *
     * @return the supported PATCH MIME types
     */
    public Set<String> getAllowedPatchSyntaxes() {
        return allowedPatchSyntaxes;
    }

    /**
     * The supported HTTP POST MIME types for the associated resource.
     *
     * <p>This collection represents the POST MIME types that a server supports,
     * which may be different from what an agent is authorized to perform.
     *
     * @return the supported POST MIME types
     */
    public Set<String> getAllowedPostSyntaxes() {
        return allowedPostSyntaxes;
    }

    /**
     * The supported HTTP PUT MIME types for the associated resource.
     *
     * <p>This collection represents the PUT MIME types that a server supports,
     * which may be different from what an agent is authorized to perform.
     *
     * @return the supported PUT MIME types
     */
    public Set<String> getAllowedPutSyntaxes() {
        return allowedPutSyntaxes;
    }

    /**
     * The content types associated with the Solid Resource.
     *
     * @return the content types
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Create a new Metadata object.
     *
     * @param storage the Solid storage in which this resource is managed
     * @param acl the ACL location for this resource
     * @param contentType the content type of the respective Solid resource
     */
    protected Metadata(final URI storage, final URI acl, final String contentType) {
        this.storage = storage;
        this.acl = acl;
        this.contentType = contentType;
    }

    /**
     * Create a new {@link Metadata} builder.
     *
     * @return a Metadata builder object
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * A Builder class for Metadata instances.
     */
    public static final class Builder {

        private URI builderAcl;
        private URI builderStorage;
        private Set<URI> builderType = new HashSet<>();
        private Map<String, Set<String>> builderWacAllow = new HashMap<>();
        private Set<String> builderAllowedMethods = new HashSet<>();
        private Set<String> builderAllowedPatchSyntaxes = new HashSet<>();
        private Set<String> builderAllowedPostSyntaxes = new HashSet<>();
        private Set<String> builderAllowedPutSyntaxes = new HashSet<>();
        private String builderContentType;

        /**
         * Add a storage property.
         *
         * @param uri the storage URI
         * @return this builder
         */
        public Builder storage(final URI uri) {
            builderStorage = uri;
            return this;
        }

        /**
         * Add a type property.
         *
         * @param uri the type URI
         * @return this builder
         */
        public Builder type(final URI uri) {
            builderType.add(uri);
            return this;
        }

        /**
         * Add a wacAllow property.
         *
         * @param accessParam the Access Parameter
         * @return this builder
         */
        public Builder wacAllow(final Map.Entry<String, Set<String>> accessParam) {
            builderWacAllow.put(accessParam.getKey(), accessParam.getValue());
            return this;
        }

        /**
         * Add an acl property.
         *
         * @param acl the acl location
         * @return this builder
         */
        public Builder acl(final URI acl) {
            builderAcl = acl;
            return this;
        }

        /**
         * Add an allowedMethod property.
         *
         * @param method the method
         * @return this builder
         */
        public Builder allowedMethod(final String method) {
            builderAllowedMethods.add(method);
            return this;
        }

        /**
         * Add a allowedPatchSyntax property.
         *
         * @param syntax the syntax
         * @return this builder
         */
        public Builder allowedPatchSyntax(final String syntax) {
            builderAllowedPatchSyntaxes.add(syntax);
            return this;
        }

        /**
         * Add a allowedPostSyntax property.
         *
         * @param syntax the syntax
         * @return this builder
         */
        public Builder allowedPostSyntax(final String syntax) {
            builderAllowedPostSyntaxes.add(syntax);
            return this;
        }

        /**
         * Add a allowedPutSyntax property.
         *
         * @param syntax the syntax
         * @return this builder
         */
        public Builder allowedPutSyntax(final String syntax) {
            builderAllowedPutSyntaxes.add(syntax);
            return this;
        }

        /**
         * Add a content type property.
         *
         * @param type the content type
         * @return this builder
         */
        public Builder contentType(final String type) {
            builderContentType = type;
            return this;
        }

        /**
         * Build the Metadata object.
         *
         * @return the resource Metadata object
         */
        public Metadata build() {
            final Metadata metadata = new Metadata(builderStorage, builderAcl, builderContentType);
            metadata.wacAllow.putAll(builderWacAllow);
            metadata.types.addAll(builderType);
            metadata.allowedMethods.addAll(builderAllowedMethods);
            metadata.allowedPatchSyntaxes.addAll(builderAllowedPatchSyntaxes);
            metadata.allowedPostSyntaxes.addAll(builderAllowedPostSyntaxes);
            metadata.allowedPutSyntaxes.addAll(builderAllowedPutSyntaxes);
            return metadata;
        }

        Builder() {
            // Prevent external instantiation
        }
    }

    public static Metadata of(final URI identifier, final Headers headers) {
        // Gather metadata from HTTP headers
        final Metadata.Builder metadata = Metadata.newBuilder();
        headers.allValues("Link").stream()
            .flatMap(l -> Headers.Link.parse(l).stream())
            .forEach(link -> {
                if (link.getParameter("rel").contains("type")) {
                    if ((link.getUri().equals(STORAGE))) {
                        metadata.storage(identifier);
                    }
                    metadata.type(link.getUri());
                } else if (link.getParameter("rel").contains("acl")) {
                    metadata.acl(link.getUri());
                } else if (link.getParameter("rel").contains(PIM.storage.toString())) {
                    metadata.storage(link.getUri());
                }
            });

        headers.allValues("WAC-Allow").stream()
            .map(Headers.WacAllow::parse)
            .map(Headers.WacAllow::getAccessParams)
            .flatMap(p -> p.entrySet().stream())
            .forEach(metadata::wacAllow);

        headers.allValues("Allow").stream()
            .flatMap(s -> Arrays.stream(s.split(",")))
            .map(String::trim)
            .forEach(metadata::allowedMethod);

        headers.allValues("Accept-Post").stream()
            .flatMap(s -> Arrays.stream(s.split(",")))
            .map(String::trim)
            .forEach(metadata::allowedPostSyntax);

        headers.allValues("Accept-Patch").stream()
            .flatMap(s -> Arrays.stream(s.split(",")))
            .map(String::trim)
            .forEach(metadata::allowedPatchSyntax);

        headers.allValues("Accept-Put").stream()
            .flatMap(s -> Arrays.stream(s.split(",")))
            .map(String::trim)
            .forEach(metadata::allowedPutSyntax);

        metadata.contentType(headers.firstValue(CONTENT_TYPE).orElse("application/octet-stream"));

        return metadata.build();
    }
}

