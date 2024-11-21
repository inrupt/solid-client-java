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

import java.util.List;
import java.util.Optional;

/**
 * A page-able result from a credential query.
 *
 * @param <T> the access credential type
 */
public class CredentialResult<T extends AccessCredential> {

    private final List<T> items;
    private final CredentialFilter<T> first;
    private final CredentialFilter<T> last;
    private final CredentialFilter<T> prev;
    private final CredentialFilter<T> next;

    /**
     * A page of access credential results.
     *
     * @param items the result items
     * @param first a filter for the first page of results, may be {@code null}
     * @param prev a filter for the previous page of results, may be {@code null}
     * @param next a filter for the next page of results, may be {@code null}
     * @param last a filter for the last page of results, may be {@code null}
     */
    public CredentialResult(final List<T> items, final CredentialFilter<T> first, final CredentialFilter<T> prev,
            final CredentialFilter<T> next, final CredentialFilter<T> last) {
        this.items = items;
        this.first = first;
        this.prev = prev;
        this.next = next;
        this.last = last;
    }

    /**
     * Get a filter for the first page of results.
     *
     * @return the first page filter, if present
     */
    public Optional<CredentialFilter<T>> firstPage() {
        return Optional.ofNullable(first);
    }

    /**
     * Get a filter for the previous page of results.
     *
     * @return the previous page filter, if present
     */
    public Optional<CredentialFilter<T>> prevPage() {
        return Optional.ofNullable(prev);
    }

    /**
     * Get a filter for the next page of results.
     *
     * @return the next page filter, if present
     */
    public Optional<CredentialFilter<T>> nextPage() {
        return Optional.ofNullable(next);
    }

    /**
     * Get a filter for the last page of results.
     *
     * @return the last page filter, if present
     */
    public Optional<CredentialFilter<T>> lastPage() {
        return Optional.ofNullable(last);
    }

    /**
     * Get the credential items in the result page.
     *
     * @return the credential items
     */
    public List<T> getItems() {
        return items;
    }
}
