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
package com.inrupt.client.uma;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class NeedInfo {

    private final String ticket;
    private final URI redirectUser;
    private final List<RequiredClaims> requiredClaims;

    private NeedInfo(final String ticket, final URI redirectUser, final List<RequiredClaims> requiredClaims) {
        this.ticket = Objects.requireNonNull(ticket);
        this.redirectUser = redirectUser;
        this.requiredClaims = Objects.requireNonNull(requiredClaims);
    }

    public List<RequiredClaims> getRequiredClaims() {
        return requiredClaims;
    }

    public Optional<URI> getRedirectUser() {
        return Optional.ofNullable(redirectUser);
    }

    public String getTicket() {
        return ticket;
    }

    public static Optional<NeedInfo> ofErrorResponse(final ErrorResponse error) {
        if ("need_info".equals(error.error) && error.ticket != null) {
            final var requiredClaims = new ArrayList<RequiredClaims>();
            if (error.requiredClaims != null) {
                for (var item : error.requiredClaims) {
                    requiredClaims.add(new RequiredClaims(item));
                }
            }
            return Optional.of(new NeedInfo(error.ticket, error.redirectUser, requiredClaims));
        }
        return Optional.empty();
    }

}
