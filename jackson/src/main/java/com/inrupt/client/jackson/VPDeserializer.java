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
import com.inrupt.client.VerifiableCredential;
import com.inrupt.client.VerifiablePresentation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VPDeserializer extends StdDeserializer<VerifiablePresentation> {

    public VPDeserializer() {
        this((Class<VerifiablePresentation>)null);
    }

    public VPDeserializer(final Class<VerifiablePresentation> vp) {
        super(vp);
    }

    @Override
    public VerifiablePresentation deserialize(final JsonParser jp, final DeserializationContext ctxt)
            throws IOException, JacksonException {

        final JsonNode node = jp.getCodec().readTree(jp);
        final VerifiablePresentation vp = new VerifiablePresentation();

        if (node.get("@context") != null) {
            final ArrayNode contexts = (ArrayNode) node.get("@context");
            final List<String> finalContext = new ArrayList<>();
            contexts.forEach(oneCtx -> finalContext.add(oneCtx.textValue()));
            vp.context = finalContext;
        }

        if (node.get("id") != null) {
            final String id = node.get("id").textValue();
            vp.id = id;
        }

        if (node.get("type") != null) {
            final ArrayNode types = (ArrayNode) node.get("type");
            final List<String> finalType = new ArrayList<>();
            types.forEach(oneType -> finalType.add(oneType.textValue()));
            vp.type = finalType;
        }

        if (node.get("holder") != null) {
            final String holder = node.get("holder").textValue();
            vp.holder = holder;
        }

        if (node.get("verifiableCredential") != null) {
            final ArrayNode verifiableCredential = (ArrayNode) node.get("verifiableCredential");
            final List<VerifiableCredential> finalVerifiableCredential = new ArrayList<>();
            for (final JsonNode oneVc: verifiableCredential) {
                finalVerifiableCredential.add((VerifiableCredential)jp.getCodec()
                    .treeToValue(oneVc, VerifiableCredential.class)
                );
            }
            vp.verifiableCredential = finalVerifiableCredential;
        }

        if (node.get("proof") != null) {
            final JsonNode proof = node.get("proof");
            final Map<String, Object> finalProof = new HashMap<>();
            proof.fields().forEachRemaining(field -> {
                finalProof.put(field.getKey(), field.getValue());
            });
            vp.proof = finalProof;
        }

        return vp;
    }

}
