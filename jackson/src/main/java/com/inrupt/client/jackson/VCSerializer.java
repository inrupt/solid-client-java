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

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;

public class VCSerializer extends StdSerializer<VerifiableCredential> {

    public VCSerializer() {
        this((Class<VerifiableCredential>)null);
    }

    public VCSerializer(final Class<VerifiableCredential> vc) {
        super(vc);
    }

    @Override
    public void serialize(final VerifiableCredential vc, final JsonGenerator jgen,
            final SerializerProvider provider) throws IOException {

        jgen.writeStartObject();

        if (vc.type != null) {
            jgen.writeFieldName("@context");
            jgen.writeStartArray();
            for (final String context : vc.context) {
                jgen.writeString(context);
            }
            jgen.writeEndArray();
        }

        jgen.writeStringField("id", vc.id);

        if (vc.type != null) {
            jgen.writeFieldName("type");
            jgen.writeStartArray();
            for (final String type : vc.type) {
                jgen.writeString(type);
            }
            jgen.writeEndArray();
        }

        jgen.writeStringField("issuer", vc.issuer);

        jgen.writeStringField("issuanceDate", vc.issuanceDate.toString());

        if (vc.credentialSubject != null) {
            jgen.writeFieldName("credentialSubject");
            jgen.writeStartObject();
            final Iterator<Entry<String, Object>> itCSubject = vc.credentialSubject.entrySet().iterator();
            while (itCSubject.hasNext()) {
                final Entry<String, Object> entry = itCSubject.next();
                jgen.writeObjectField(entry.getKey(), entry.getValue());
            }
            jgen.writeEndObject();
        }

        if (vc.credentialStatus != null) {
            jgen.writeFieldName("credentialStatus");
            jgen.writeStartObject();
            final Iterator<Entry<String, Object>> itCStatus = vc.credentialStatus.entrySet().iterator();
            while (itCStatus.hasNext()) {
                final Entry<String, Object> entry = itCStatus.next();
                jgen.writeObjectField(entry.getKey(), entry.getValue());
            }
            jgen.writeEndObject();
        }

        if (vc.proof != null) {
            jgen.writeFieldName("proof");
            jgen.writeStartObject();
            final Iterator<Entry<String, Object>> itProof = vc.proof.entrySet().iterator();
            while (itProof.hasNext()) {
                final Entry<String, Object> entry = itProof.next();
                jgen.writeObjectField(entry.getKey(), entry.getValue());
            }
            jgen.writeEndObject();
        }

        jgen.writeEndObject();

    }

}
