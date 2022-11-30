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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.inrupt.client.VerifiablePresentation;

import java.io.IOException;

public class VPSerializer extends StdSerializer<VerifiablePresentation> {

    private static final String CONTEXT = "@context";
    private static final String TYPE = "type";
    private static final String ID = "id";
    private static final String VERIFIABLE_CREDENTIAL = "verifiableCredential";
    private static final String PROOF = "proof";
    private static final String HOLDER = "holder";

    public VPSerializer() {
        this((Class<VerifiablePresentation>)null);
    }

    public VPSerializer(final Class<VerifiablePresentation> vp) {
        super(vp);
    }

    @Override
    public void serialize(final VerifiablePresentation vp, final JsonGenerator jgen,
            final SerializerProvider provider) throws IOException {

        jgen.writeStartObject();

        if (vp.context != null) {
            jgen.writeFieldName(CONTEXT);
            jgen.writeStartArray();
            for (final var context : vp.context) {
                jgen.writeString(context);
            }
            jgen.writeEndArray();
        }

        if (vp.id != null) {
            jgen.writeStringField(ID, vp.id);
        }

        if (vp.type != null) {
            jgen.writeFieldName(TYPE);
            jgen.writeStartArray();
            for (final var type : vp.type) {
                jgen.writeString(type);
            }
            jgen.writeEndArray();
        }

        if (vp.holder != null) {
            jgen.writeStringField(HOLDER, vp.holder);
        }

        if (vp.verifiableCredential != null) {
            jgen.writeFieldName(VERIFIABLE_CREDENTIAL);
            jgen.writeStartArray();
            for (final var oneVc : vp.verifiableCredential) {
                jgen.writePOJO(oneVc);
            }
            jgen.writeEndArray();
        }

        if (vp.proof != null) {
            jgen.writeFieldName(PROOF);
            jgen.writeStartObject();
            for (final var oneProof : vp.proof.entrySet()) {
                jgen.writeObjectField(oneProof.getKey(), oneProof.getValue());
            }
            jgen.writeEndObject();
        }

        jgen.writeEndObject();

    }

}
