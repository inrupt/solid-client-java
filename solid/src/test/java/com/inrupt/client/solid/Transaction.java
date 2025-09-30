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

import com.inrupt.client.spi.JsonService;
import com.inrupt.client.spi.ServiceProvider;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.time.Instant;

class Transaction extends SolidNonRDFSource {

    private final JsonService jsonService;
    private final TransactionData data;

    public Transaction(final URI identifier, final String contentType, final InputStream entity) {
        super(identifier, contentType, entity);

        this.jsonService = ServiceProvider.getJsonService();
        try {
            this.data = jsonService.fromJson(super.getEntity(), TransactionData.class);
        } catch (IOException ex) {
            throw new UncheckedIOException("Unable to parse Transaction data", ex);
        }
    }

    public String getId() {
        return data.getId();
    }

    public void setId(final String id) {
        data.setId(id);
    }

    public String getDescription() {
        return data.getDescription();
    }

    public void setDescription(final String description) {
        data.setDescription(description);
    }

    public Instant getDate() {
        return data.getDate();
    }

    public void setDate(final Instant date) {
        data.setDate(date);
    }

    public double getAmount() {
        return data.getAmount();
    }

    public void setAmount(final double amount) {
        data.setAmount(amount);
    }

    public String getCategory() {
        return data.getCategory();
    }

    public void setCategory(final String category) {
        data.setCategory(category);
    }

    public TransactionType getType() {
        return data.getType();
    }

    public void setType(final TransactionType type) {
        data.setType(type);
    }

    @Override
    public InputStream getEntity() throws IOException {
        try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            jsonService.toJson(data, output);
            return new ByteArrayInputStream(output.toByteArray());
        }
    }
}
