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
import java.util.Objects;

class TransactionResource extends SolidNonRDFSource {

    private final JsonService jsonService;

    private Transaction transaction;

    public TransactionResource(final URI identifier, final String contentType, final InputStream entity) {
        super(identifier, contentType, entity);

        this.jsonService = ServiceProvider.getJsonService();
        try {
            this.transaction = jsonService.fromJson(super.getEntity(), Transaction.class);
        } catch (IOException ex) {
            throw new UncheckedIOException("Unable to parse Transaction data", ex);
        }
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(final Transaction transaction) {
        this.transaction = Objects.requireNonNull(transaction, "transaction may not be null!");
    }

    @Override
    public InputStream getEntity() throws IOException {
        try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            jsonService.toJson(transaction, output);
            return new ByteArrayInputStream(output.toByteArray());
        }
    }

    record Transaction(String id, String description, Instant date, double amount, TransactionType type,
            String category) {
        static class Builder {
            private String transactionId;
            private String transactionDescription;
            private Instant transactionDate;
            private double transactionAmount;
            private TransactionType transactionType;
            private String transactionCategory;

            public Builder id(final String id) {
                this.transactionId = id;
                return this;
            }

            public Builder description(final String description) {
                this.transactionDescription = description;
                return this;
            }

            public Builder date(final Instant date) {
                this.transactionDate = date;
                return this;
            }

            public Builder amount(final double amount) {
                this.transactionAmount = amount;
                return this;
            }

            public Builder type(final TransactionType type) {
                this.transactionType = type;
                return this;
            }

            public Builder category(final String category) {
                this.transactionCategory = category;
                return this;
            }

            public Transaction build() {
                return new Transaction(this.transactionId, this.transactionDescription, this.transactionDate,
                        this.transactionAmount, this.transactionType, this.transactionCategory);
            }
        }

        static Builder newBuilder() {
            return new Builder();
        }

        static Builder newBuilder(final Transaction transaction) {
            return new Builder()
                .id(transaction.id())
                .description(transaction.description())
                .date(transaction.date())
                .amount(transaction.amount())
                .type(transaction.type())
                .category(transaction.category());
        }
    }

    enum TransactionType {
        CREDIT, DEBIT
    }
}
