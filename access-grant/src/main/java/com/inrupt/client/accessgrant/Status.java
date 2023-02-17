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
package com.inrupt.client.accessgrant;

import java.net.URI;
import java.util.Objects;

public class Status {

    private final URI identifier;
    private final String type;
    private final int index;
    private final URI credential;

    public Status(final URI identifier, final String type, final URI credential, final int index) {
        this.identifier = Objects.requireNonNull(identifier, "Status identifier may not be null!");
        this.type = Objects.requireNonNull(type, "Status type may not be null!");
        this.credential = Objects.requireNonNull(credential, "Status credential may not be null!");
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public URI getCredential() {
        return credential;
    }

    public URI getIdentifier() {
        return identifier;
    }

    public String getType() {
        return type;
    }
}
