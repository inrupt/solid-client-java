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
import com.inrupt.client.spi.VerifiableCredential;
import com.inrupt.client.spi.VerifiablePresentation;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;

public class VPSerializer extends StdSerializer<VerifiablePresentation> {

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
            jgen.writeFieldName("@context");
            jgen.writeStartArray();
            for (final String context : vp.context) {
                jgen.writeString(context);
            }
            jgen.writeEndArray();
        }

        jgen.writeStringField("id", vp.id);

        if (vp.type != null) {
            jgen.writeFieldName("type");
            jgen.writeStartArray();
            for (final String type : vp.type) {
                jgen.writeString(type);
            }
            jgen.writeEndArray();
        }

        jgen.writeStringField("holder", vp.holder);

        if (vp.verifiableCredential != null) {
            jgen.writeFieldName("verifiableCredential");
            jgen.writeStartArray();
            for (final VerifiableCredential vc : vp.verifiableCredential) {
                jgen.writePOJO(vc);
            }
            jgen.writeEndArray();
        }

        if (vp.proof != null) {
            jgen.writeFieldName("proof");
            jgen.writeStartObject();
            final Iterator<Entry<String, Object>> itProof = vp.proof.entrySet().iterator();
            while (itProof.hasNext()) {
                final Entry<String, Object> entry = itProof.next();
                jgen.writeObjectField(entry.getKey(), entry.getValue());
            }
            jgen.writeEndObject();
        }

        jgen.writeEndObject();

    }

}
