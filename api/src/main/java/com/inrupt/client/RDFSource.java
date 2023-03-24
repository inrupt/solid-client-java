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

import com.inrupt.client.spi.RDFFactory;
import com.inrupt.client.spi.ServiceProvider;
import com.inrupt.rdf.wrapping.commons.WrapperDataset;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.apache.commons.rdf.api.Dataset;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.api.RDFSyntax;

/**
 * A base class for RDF-based resource mapping.
 *
 * <p>This class can be used as a basis for object mapping with higher-level client applications.
 */
public class RDFSource extends WrapperDataset implements Resource {

    /**
     * The RDF Factory instance.
     */
    protected static final RDF rdf = RDFFactory.getInstance();

    private final URI identifier;
    private final RDFSyntax syntax;

    /**
     * Create a new RDF-bearing resource.
     *
     * <p>Subclasses should have the same constructor signature to work with the provided object mapping mechanism.
     *
     * @param identifier the resource identifier
     * @param dataset the dataset corresponding to this resource, may be {@code null}
     */
    protected RDFSource(final URI identifier, final Dataset dataset) {
        this(identifier, RDFSyntax.TURTLE, dataset);
    }

    /**
     * Create a new RDF-bearing resource.
     *
     * <p>Subclasses should have the same constructor signature to work with the provided object mapping mechanism.
     *
     * @param identifier the resource identifier
     * @param syntax the original RDF syntax in use
     * @param dataset the dataset corresponding to this resource, may be {@code null}
     */
    protected RDFSource(final URI identifier, final RDFSyntax syntax, final Dataset dataset) {
        super(dataset == null ? rdf.createDataset() : dataset);
        this.identifier = identifier;
        this.syntax = syntax;
    }

    @Override
    public URI getIdentifier() {
        return identifier;
    }

    @Override
    public String getContentType() {
        return syntax.mediaType();
    }

    @Override
    public InputStream getEntity() throws IOException {
        try (final ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            serialize(syntax, out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    /**
     * Serialize this object with a defined RDF syntax.
     *
     * @param syntax the RDF syntax
     * @param out the output stream
     * @throws IOException in the case of an I/O error
     */
    public void serialize(final RDFSyntax syntax, final OutputStream out) throws IOException {
        ServiceProvider.getRdfService().fromDataset(this, syntax, out);
    }

    /**
     * Validate the dataset for this object.
     *
     * <p>Subclasses may override this method to perform validation on the provided dataset.
     * By default, this method is a no-op.
     *
     * @return the validation result
     */
    public ValidationResult validate() {
        return new ValidationResult(true);
    }


    @Override
    public void close() {
        try {
            super.close();
        } catch (final Exception ex) {
            throw new InruptClientException("Error closing dataset", ex);
        }
    }
}
