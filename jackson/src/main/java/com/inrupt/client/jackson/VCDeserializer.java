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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.inrupt.client.VerifiableCredential;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        if (node.get(CONTEXT) != null) {
            final ArrayNode contexts = (ArrayNode) node.get(CONTEXT);
            final List<String> finalContext = new ArrayList<>();
            contexts.forEach(oneCtx -> finalContext.add(oneCtx.textValue()));
            vc.context = finalContext;
        }

        if (node.get(ID) != null) {
            final String id = node.get(ID).textValue();
            vc.id = id;
        }

        if (node.get(TYPE) != null) {
            final ArrayNode types = (ArrayNode) node.get(TYPE);
            final List<String> finalType = new ArrayList<>();
            types.forEach(oneType -> finalType.add(oneType.textValue()));
            vc.type = finalType;
        }

        if (node.get(ISSUER) != null) {
            final String issuer = node.get(ISSUER).textValue();
            vc.issuer = issuer;
        }

        if (node.get(ISSUANCE_DATE) != null) {
            final Instant issuanceDate = Instant.parse(node.get(ISSUANCE_DATE).textValue());
            vc.issuanceDate = issuanceDate;
        }

        if (node.get(EXPIRATION_DATE) != null) {
            final Instant expirationDate = Instant.parse(node.get(EXPIRATION_DATE).textValue());
            vc.expirationDate = expirationDate;
        }

        if (node.get(CREDENTIAL_SUBJECT) != null) {
            final JsonNode credentialSubject = node.get(CREDENTIAL_SUBJECT);
            final Map<String, Object> finalCredentialSubject = new HashMap<>();
            credentialSubject.fields().forEachRemaining(field ->
                finalCredentialSubject.put(field.getKey(), field.getValue())
            );
            vc.credentialSubject = finalCredentialSubject;
        }

        if (node.get(CREDENTIAL_STATUS) != null) {
            final JsonNode credentialStatus = node.path(CREDENTIAL_STATUS);
            final Map<String, Object> finalCredentialStatus = new HashMap<>();
            credentialStatus.fields().forEachRemaining(field ->
                finalCredentialStatus.put(field.getKey(), field.getValue())
            );
            vc.credentialStatus = finalCredentialStatus;
        }

        if (node.get(PROOF) != null) {
            final JsonNode proof = node.get(PROOF);
            final Map<String, Object> finalProof = new HashMap<>();
            proof.fields().forEachRemaining(field ->
                finalProof.put(field.getKey(), field.getValue())
            );
            vc.proof = finalProof;
        }

        return vc;
    }

}
