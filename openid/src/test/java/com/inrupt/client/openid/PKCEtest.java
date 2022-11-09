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
package com.inrupt.client.openid;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PKCEtest {

    @Test
    void createChallengeTest() {
        assertTrue(PKCE.createChallenge("verifier").getBytes(UTF_8).length >= 43);
        assertTrue(PKCE.createChallenge("verifier").getBytes(UTF_8).length <= 128);
        assertTrue(PKCE.createChallenge("").getBytes(UTF_8).length >= 43);
        assertTrue(PKCE.createChallenge("").getBytes(UTF_8).length <= 128);
        assertThrows(NullPointerException.class, () -> PKCE.createChallenge(null));
    }

    @Test
    void createVerifierTest() {
        assertTrue(PKCE.createVerifier().getBytes(UTF_8).length >= 43);
        assertTrue(PKCE.createVerifier().getBytes(UTF_8).length <= 128);
    }
}
