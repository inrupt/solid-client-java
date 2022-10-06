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
package com.inrupt.client.vc;

import com.inrupt.client.spi.VerifiableCredential;
import com.inrupt.client.spi.VerifiablePresentation;

import java.time.Instant;
import java.util.List;
import java.util.Map;
final class VCtestData {

    static final VerifiableCredential VC = createVC();
    static final VerifiablePresentation VP = createVP();

    private static VerifiableCredential createVC() {
        final var vc = new VerifiableCredential();
        vc.context = List.of(
        "https://www.w3.org/2018/credentials/v1",
        "https://www.w3.org/2018/credentials/examples/v1"
        );

        vc.id = "http://example.test/credentials/1872";
        vc.type = List.of(
            "VerifiableCredential",
            "UniversityDegreeCredential"
        );
        vc.issuer = "https://example.test/issuers/565049";
        vc.issuanceDate = Instant.parse("2018-12-04T18:47:38.927Z");
        vc.credentialSubject = Map.of(
            "id", "did:example:ebfeb1f712ebc6f1c276e12ec21",
            "alumniOf", Map.of(
                        "id", "did:example:c276e12ec21ebfeb1f712ebc6f1",
                        "name", "Example University")
        );

        vc.proof = Map.of(
            "type", "RsaSignature2018",
            "created", Instant.parse("2017-06-18T21:19:10Z"),
            "proofPurpose", "assertionMethod",
            "verificationMethod", "https://example.test/issuers/565049#key-1",
            "jws", "eyJhbGciOiJSUzI1NiIsImI2NCI6ZmFsc2UsImNyaXQiOlsiYjY0Il19"
        );
        return vc;
    }

    private static VerifiablePresentation createVP() {
        final var vp = new VerifiablePresentation();

        vp.context = List.of(
            "https://www.w3.org/2018/credentials/v1",
            "https://www.w3.org/2018/credentials/examples/v1"
        );
        vp.id = "http://example.test/credentialVP/18";
        vp.type = List.of("VerifiableCredential");
        vp.holder = "did:example:123";
        vp.verifiableCredential = List.of(createVC());
        return vp;
    }

    private VCtestData() {
        // Prevent instantiation
    }
}
