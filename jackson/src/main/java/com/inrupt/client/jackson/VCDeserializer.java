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

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.inrupt.client.spi.VerifiableCredential;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VCDeserializer extends StdDeserializer<VerifiableCredential> {

    public VCDeserializer() {
        this((Class<VerifiableCredential>)null);
    }

    public VCDeserializer(final Class<VerifiableCredential> vc) {
        super(vc);
    }

    @Override
    public VerifiableCredential deserialize(final JsonParser jp, final DeserializationContext ctxt)
            throws IOException, JacksonException {
        final JsonNode node = jp.getCodec().readTree(jp);

        final VerifiableCredential vc = new VerifiableCredential();

        if (node.get("@context") != null) {
            final ArrayNode contexts = (ArrayNode) node.get("@context");
            final List<String> finalContext = new ArrayList<>();
            contexts.forEach(oneCtx -> finalContext.add(oneCtx.textValue()));
            vc.context = finalContext;
        }

        if (node.get("id") != null) {
            final String id = node.get("id").textValue();
            vc.id = id;
        }

        if (node.get("type") != null) {
            final ArrayNode types = (ArrayNode) node.get("type");
            final List<String> finalType = new ArrayList<>();
            types.forEach(oneType -> finalType.add(oneType.textValue()));
            vc.type = finalType;
        }

        if (node.get("issuer") != null) {
            final String issuer = node.get("issuer").textValue();
            vc.issuer = issuer;
        }

        if (node.get("issuanceDate") != null) {
            final Instant issuanceDate = Instant.parse(node.get("issuanceDate").textValue());
            vc.issuanceDate = issuanceDate;
        }

        if (node.get("expirationDate") != null) {
            final Instant expirationDate = Instant.parse(node.get("expirationDate").textValue());
            vc.expirationDate = expirationDate;
        }

        if (node.get("credentialSubject") != null) {
            final ObjectNode credentialSubject = (ObjectNode) node.get("credentialSubject");
            final Map<String, Object> finalCredentialSubject = new HashMap<>();
            credentialSubject.fields().forEachRemaining(field -> {
                finalCredentialSubject.put(field.getKey(), field.getValue());
            });
            vc.credentialSubject = finalCredentialSubject;
        }

        if (node.get("credentialStatus") != null) {
            final ObjectNode credentialStatus = (ObjectNode) node.path("credentialStatus");
            final Map<String, Object> finalCredentialStatus = new HashMap<>();
            credentialStatus.fields().forEachRemaining(field -> {
                finalCredentialStatus.put(field.getKey(), field.getValue());
            });
            vc.credentialStatus = finalCredentialStatus;
        }

        if (node.get("proof") != null) {
            final ObjectNode proof = (ObjectNode) node.get("proof");
            final Map<String, Object> finalProof = new HashMap<>();
            proof.fields().forEachRemaining(field -> {
                finalProof.put(field.getKey(), field.getValue());
            });
            vc.proof = finalProof;
        }

        return vc;
    }

}
