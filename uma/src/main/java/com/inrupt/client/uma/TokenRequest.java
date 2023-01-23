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
package com.inrupt.client.uma;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * An UMA token request class.
 */
public class TokenRequest {

    private final String ticket;
    private final String pct;
    private final String rpt;
    private final ClaimToken claimToken;
    private final List<String> scopes;

    public TokenRequest(final String ticket, final String pct, final String rpt,
            final ClaimToken claimToken, final List<String> scopes) {
        this.ticket = Objects.requireNonNull(ticket);
        this.pct = pct;
        this.rpt = rpt;
        this.claimToken = claimToken;
        this.scopes = scopes;
    }

    public String getTicket() {
        return ticket;
    }

    public Optional<String> getPersistedClaimToken() {
        return Optional.ofNullable(pct);
    }

    public Optional<String> getRequestingPartyToken() {
        return Optional.ofNullable(rpt);
    }

    public Optional<ClaimToken> getClaimToken() {
        return Optional.ofNullable(claimToken);
    }

    public List<String> getScopes() {
        if (scopes != null) {
            return scopes;
        }
        return Collections.emptyList();
    }
}
