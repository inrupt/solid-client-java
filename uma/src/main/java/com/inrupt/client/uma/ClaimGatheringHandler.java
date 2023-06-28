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
package com.inrupt.client.uma;

import java.util.concurrent.CompletionStage;

/**
 * A handler capable of gathering claims for a given type, from a given issuer.
 */
public interface ClaimGatheringHandler {

    /**
     * Get the claim token format supported by this handler.
     *
     * @return the supported claim token format
     */
    String getClaimTokenFormat();

    /**
     * Get the issuer supported by this handler.
     *
     * @return the supported issuer
     */
    String getIssuer();

    /**
     * Get the claim type supported by this handler.
     *
     * @return the supported claim type
     */
    String getClaimType();

    /**
     * Gather a claim token asynchronously.
     *
     * @return the next stage of completion containing a new claim token
     */
    CompletionStage<ClaimToken> gather();

    /**
     * Determine if this handler is compatible with the listed UMA requirements.
     *
     * @param requirements the requirements for an interaction to proceed
     * @return true if this handler is compatible with the described requirements; false otherwise
     */
    default boolean isCompatibleWith(final RequiredClaims requirements) {
        if (!requirements.getClaimTokenFormats().isEmpty() &&
                !requirements.getClaimTokenFormats().contains(getClaimTokenFormat())) {
            return false;
        }

        if (!requirements.getIssuers().isEmpty() &&
                !requirements.getIssuers().contains(getIssuer())) {
            return false;
        }

        return requirements.getClaimType().filter(getClaimType()::equals).isPresent();
    }
}
