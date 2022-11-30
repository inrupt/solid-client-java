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
package com.inrupt.client.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.inrupt.client.VerifiableCredential;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class VCDeserializer extends StdDeserializer<VerifiableCredential> {

    private static final String CONTEXT = "@context";
    private static final String TYPE = "type";
    private static final String ID = "id";
    private static final String ISSUER = "issuer";
    private static final String ISSUANCE_DATE = "issuanceDate";
    private static final String EXPIRATION_DATE = "expirationDate";
    private static final String CREDENTIAL_SUBJECT = "credentialSubject";
    private static final String CREDENTIAL_STATUS = "credentialStatus";
    private static final String PROOF = "proof";
    private static final String NOT_NULL_MESSAGE = "Verifiable credential {} cannot be empty!";
    private static final String TO_REPLACE = "{}";

    public VCDeserializer() {
        this((Class<VerifiableCredential>)null);
    }

    public VCDeserializer(final Class<VerifiableCredential> vc) {
        super(vc);
    }

    @Override
    public VerifiableCredential deserialize(final JsonParser jp, final DeserializationContext ctxt)
            throws IOException {
        final JsonNode node = jp.getCodec().readTree(jp);

        final VerifiableCredential vc = new VerifiableCredential();

        if (!node.path(CONTEXT).isMissingNode()) {
            vc.context = new ArrayList<>();
            node.get(CONTEXT).elements()
                    .forEachRemaining(element -> vc.context.add(element.textValue()));
        }

        if (!node.path(ID).isMissingNode()) {
            vc.id = node.get(ID).textValue();
        }

        if (!node.path(TYPE).isMissingNode()) {
            vc.type = new ArrayList<>();
            node.get(TYPE).elements().forEachRemaining(element -> vc.type.add(element.textValue()));
        }

        if (!node.path(ISSUER).isMissingNode()) {
            Objects.requireNonNull(node.get(ISSUER), NOT_NULL_MESSAGE.replace(TO_REPLACE, ISSUER));
            vc.issuer = node.get(ISSUER).textValue();
        }

        if (!node.path(ISSUANCE_DATE).isMissingNode()) {
            vc.issuanceDate = Instant.parse(node.get(ISSUANCE_DATE).textValue());
        }

        if (!node.path(EXPIRATION_DATE).isMissingNode()) {
            vc.expirationDate = Instant.parse(node.get(EXPIRATION_DATE).textValue());
        }

        if (!node.path(CREDENTIAL_SUBJECT).isMissingNode()) {
            vc.credentialSubject = new HashMap<>();
            node.get(CREDENTIAL_SUBJECT).fields().forEachRemaining(
                    field -> vc.credentialSubject.put(field.getKey(), field.getValue()));
        }

        if (!node.path(CREDENTIAL_STATUS).isMissingNode()) {
            vc.credentialStatus = new HashMap<>();
            node.get(CREDENTIAL_STATUS).fields().forEachRemaining(
                    field -> vc.credentialStatus.put(field.getKey(), field.getValue()));
        }

        if (!node.path(PROOF).isMissingNode()) {
            vc.proof = new HashMap<>();
            node.get(PROOF).fields()
                    .forEachRemaining(field -> vc.proof.put(field.getKey(), field.getValue()));
        }

        return vc;
    }

}
