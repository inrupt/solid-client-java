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

/**
 * A class representing a {@code need_info} error response from an UMA server.
 */
public final class NeedInfo {

    private static final String NEED_INFO = "need_info";

    private final String ticket;
    private final URI redirectUser;
    private final List<RequiredClaims> requiredClaims;

    private NeedInfo(final String ticket, final URI redirectUser, final List<RequiredClaims> requiredClaims) {
        this.ticket = Objects.requireNonNull(ticket);
        this.redirectUser = redirectUser;
        this.requiredClaims = Objects.requireNonNull(requiredClaims);
    }

    /**
     * Return a list of required claims.
     *
     * @return the required claims, never {@code null}
     */
    public List<RequiredClaims> getRequiredClaims() {
        return requiredClaims;
    }

    /**
     * Return an optional redirect URI.
     *
     * @return a user redirection URI
     */
    public Optional<URI> getRedirectUser() {
        return Optional.ofNullable(redirectUser);
    }

    /**
     * Return the ticket to be used when continuing this flow.
     *
     * @return the UMA ticket
     */
    public String getTicket() {
        return ticket;
    }

    /**
     * Create an optional {@link NeedInfo} object from an {@link ErrorResponse}.
     *
     * @param error the error response
     * @return the optional {@code need_info} data
     */
    public static Optional<NeedInfo> ofErrorResponse(final ErrorResponse error) {
        if (NEED_INFO.equals(error.error) && error.ticket != null) {
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
