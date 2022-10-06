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
package com.inrupt.client.jsonb;

import com.inrupt.client.spi.VerifiableCredential;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.bind.adapter.JsonbAdapter;

public class VCAdapter implements JsonbAdapter<VerifiableCredential, JsonObject> {

    @Override
    public JsonObject adaptToJson(final VerifiableCredential vc) throws Exception {
        final var builder = Json.createArrayBuilder();

        vc.context.forEach(oneContext -> builder.add(oneContext));
        final JsonArray context = builder.build();

        vc.type.forEach(oneType -> builder.add(oneType));
        final JsonArray type = builder.build();

        return Json.createObjectBuilder()
            .add("@context", context)
            .add("id", vc.id)
            .add("type", type)
            .add("issuer", vc.issuer)
            .add("issuanceDate", vc.issuanceDate.toString())
            .add("expirationDate", vc.expirationDate.toString())
            .add("credentialSubject", vc.credentialSubject.toString())
            .add("credentialStatus", vc.credentialStatus.toString())
            .add("proof", vc.proof.toString())
            .build();
    }

    @Override
    public VerifiableCredential adaptFromJson(final JsonObject adapted) throws Exception {
        final var vc = new VerifiableCredential();

        if (adapted.containsKey("@context")) {
            vc.context = new ArrayList<String>();
            final JsonArray jsonArrayContext = adapted.getJsonArray("@context");
            jsonArrayContext.forEach(value -> vc.context.add(((JsonString)value).getString()));
        }

        if (adapted.containsKey("id")) {
            vc.id = adapted.getString("id");
        }

        if (adapted.containsKey("type")) {
            final JsonArray jsonArrayType = adapted.getJsonArray("type");
            vc.type = new ArrayList<String>();
            jsonArrayType.forEach(value -> vc.type.add(((JsonString)value).getString()));
        }

        if (adapted.containsKey("issuer")) {
            vc.issuer = adapted.getString("issuer");
        }

        if (adapted.containsKey("issuanceDate")) {
            vc.issuanceDate = Instant.parse(adapted.getString("issuanceDate"));
        }

        if (adapted.containsKey("expirationDate")) {
            vc.expirationDate = Instant.parse(adapted.getString("expirationDate"));
        }

        if (adapted.containsKey("credentialSubject")) {
            vc.credentialSubject = new HashMap<String,Object>();
            final JsonObject jsonObject = adapted.getJsonObject("credentialSubject");
            vc.credentialSubject.putAll(jsonObject);
        }

        if (adapted.containsKey("credentialStatus")) {
            vc.credentialStatus = new HashMap<String,Object>();
            final JsonObject jsonObject = adapted.getJsonObject("credentialStatus");
            vc.credentialStatus.putAll(jsonObject);
        }

        if (adapted.containsKey("proof")) {
            vc.proof = new HashMap<String,Object>();
            final JsonObject jsonObject = adapted.getJsonObject("proof");
            vc.proof.putAll(jsonObject);
        }

        return vc;
    }

}
