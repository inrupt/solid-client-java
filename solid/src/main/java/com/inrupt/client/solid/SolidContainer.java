/*
 * Copyright 2022 Inrupt Inc.
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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * A WebID Profile for use with Solid.
 */
public class SolidContainer extends SolidResource{

    protected final List<SolidResource> containedResources = new ArrayList<>();

    public List<SolidResource> getContainedResources() {
        return containedResources;
    }

    /**
     * Create a new SolidContainer profile resource.
     *
     * @param id the webid URI
     */
    protected SolidContainer(final URI id) {
        super(id);
    }

    /**
     * Create a new {@link SolidContainer} builder.
     *
     * @return the builder
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * A builder class for WebIdProfile objects.
     */
    public static final class Builder extends SolidResource.Builder{

        private List<SolidResource> builderContainedResources = new ArrayList<>();

        /**
         * Add a resource to container.
         *
         * @param resource the solid resource
         * @return this builder
         */
        public Builder containedResource(final SolidResource resource) {
            builderContainedResources.add(resource);
            return this;
        }

        /**
         * Build the SolidResource object.
         *
         * @param id the Solid container URI
         * @return the Solid containerss
         */
        @Override
        public SolidContainer build(final URI id) {
            final var container = new SolidContainer(id);
            container.containedResources.addAll(builderContainedResources);
            container.storage.addAll(builderStorage);
            container.type.addAll(builderType);
            container.wacAllow.putAll(builderWacAllow);
            container.statements.addAll(builderStatements);
            container.allowedMethods.addAll(builderAllowedMethods);
            container.allowedPatchSyntaxes.addAll(builderAllowedPatchSyntaxes);
            container.allowedPostSyntaxes.addAll(builderAllowedPostSyntaxes);
            container.allowedPutSyntaxes.addAll(builderAllowedPutSyntaxes);

            return container;
        }
    }
}
