/*
 * Copyright 2023 Inrupt Inc.
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
package com.inrupt.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.Objects;

public class NonRDFSource implements Resource {

    private final URI identifier;
    private final String contentType;
    private final InputStream entity;

    protected NonRDFSource(final URI identifier, final String contentType, final InputStream entity) {
        this.identifier = Objects.requireNonNull(identifier, "identifier may not be null!");
        this.contentType = Objects.requireNonNull(contentType, "contentType may not be null!");
        this.entity = Objects.requireNonNull(entity, "entity may not be null!");
    }

    @Override
    public URI getIdentifier() {
        return identifier;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public InputStream getEntity() throws IOException {
        return entity;
    }

    @Override
    public void close() {
        try {
            entity.close();
        } catch (final IOException ex) {
            throw new UncheckedIOException("Unable to close NonRDFSource entity", ex);
        }
    }
}
