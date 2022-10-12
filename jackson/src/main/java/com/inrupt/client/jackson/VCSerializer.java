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

        jgen.writeFieldName("@context");
        jgen.writeStartArray();
        for (final String context: vc.context) {
            jgen.writeString(context);
        }
        jgen.writeEndArray();

        jgen.writeStringField("id", vc.id);

        jgen.writeFieldName("type");
        jgen.writeStartArray();
        for (final String type: vc.type) {
            jgen.writeString(type);
        }
        jgen.writeEndArray();

        jgen.writeStringField("issuer", vc.issuer);

        jgen.writeStringField("issuanceDate", vc.issuanceDate.toString());

        jgen.writeStringField("credentialSubject", vc.credentialSubject.toString());

        jgen.writeStringField("credentialStatus", vc.credentialStatus.toString());

        jgen.writeStringField("proof", vc.proof.toString());

        jgen.writeEndObject();

    }

}
