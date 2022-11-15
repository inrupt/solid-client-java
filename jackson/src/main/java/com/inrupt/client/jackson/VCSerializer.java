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
import com.inrupt.client.VerifiableCredential;

import java.io.IOException;

public class VCSerializer extends StdSerializer<VerifiableCredential> {

    private static final String CONTEXT = "@context";
    private static final String TYPE = "type";
    private static final String ID = "id";
    private static final String ISSUER = "issuer";
    private static final String ISSUANCE_DATE = "issuanceDate";
    private static final String CREDENTIAL_SUBJECT = "credentialSubject";
    private static final String CREDENTIAL_STATUS = "credentialStatus";
    private static final String PROOF = "proof";

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

        if (vc.context != null) {
            jgen.writeFieldName(CONTEXT);
            jgen.writeStartArray();
            for (final var context : vc.context) {
                jgen.writeString(context);
            }
            jgen.writeEndArray();
        }

        if (vc.id != null) {
            jgen.writeStringField(ID, vc.id);
        }

        if (vc.type != null) {
            jgen.writeFieldName(TYPE);
            jgen.writeStartArray();
            for (final var type : vc.type) {
                jgen.writeString(type);
            }
            jgen.writeEndArray();
        }

        if (vc.issuer != null) {
            jgen.writeStringField(ISSUER, vc.issuer);
        }

        if (vc.issuanceDate != null) {
            jgen.writeStringField(ISSUANCE_DATE, vc.issuanceDate.toString());
        }

        if (vc.credentialSubject != null) {
            jgen.writeFieldName(CREDENTIAL_SUBJECT);
            jgen.writeStartObject();
            final var itCSubject = vc.credentialSubject.entrySet().iterator();
            while (itCSubject.hasNext()) {
                final var entry = itCSubject.next();
                jgen.writePOJOField(entry.getKey(), entry.getValue());
            }
            jgen.writeEndObject();
        }

        if (vc.credentialStatus != null) {
            jgen.writeFieldName(CREDENTIAL_STATUS);
            jgen.writeStartObject();
            final var itCStatus = vc.credentialStatus.entrySet().iterator();
            while (itCStatus.hasNext()) {
                final var entry = itCStatus.next();
                jgen.writePOJOField(entry.getKey(), entry.getValue());
            }
            jgen.writeEndObject();
        }

        if (vc.proof != null) {
            jgen.writeFieldName(PROOF);
            jgen.writeStartObject();
            final var itProof = vc.proof.entrySet().iterator();
            while (itProof.hasNext()) {
                final var entry = itProof.next();
                jgen.writePOJOField(entry.getKey(), entry.getValue());
            }
            jgen.writeEndObject();
        }

        jgen.writeEndObject();

    }

}
