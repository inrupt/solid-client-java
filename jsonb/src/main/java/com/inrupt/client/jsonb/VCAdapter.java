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

import com.inrupt.client.api.VerifiableCredential;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.bind.adapter.JsonbAdapter;

public class VCAdapter implements JsonbAdapter<VerifiableCredential, JsonObject> {

    @Override
    public JsonObject adaptToJson(final VerifiableCredential vc) throws Exception {

        final var result = Json.createObjectBuilder();

        if (vc.context != null) {
            final var arrayBuilder = Json.createArrayBuilder();
            vc.context.forEach(oneContext -> arrayBuilder.add(oneContext));
            result.add("@context", arrayBuilder.build());
        }

        if (vc.type != null) {
            final var arrayBuilder = Json.createArrayBuilder();
            vc.type.forEach(oneType -> arrayBuilder.add(oneType));
            result.add("type", arrayBuilder.build());
        }

        if (vc.id != null) {
            result.add("id", vc.id);
        }

        if (vc.issuer != null) {
            result.add("issuer", vc.issuer);
        }

        if (vc.issuanceDate != null) {
            result.add("issuanceDate", vc.issuanceDate.toString());
        }

        if (vc.expirationDate != null) {
            result.add("expirationDate", vc.expirationDate.toString());
        }

        if (vc.credentialSubject != null) {
            final var itCSubject = vc.credentialSubject.entrySet().iterator();
            final var objectBuilder = Json.createObjectBuilder();
            while (itCSubject.hasNext()) {
                addRightJsonType(objectBuilder, itCSubject.next());
            }
            result.add("credentialSubject", objectBuilder.build());
        }

        if (vc.credentialStatus != null) {
            final var itCStatus = vc.credentialStatus.entrySet().iterator();
            final var objectBuilder = Json.createObjectBuilder();
            while (itCStatus.hasNext()) {
                addRightJsonType(objectBuilder, itCStatus.next());
            }
            result.add("credentialStatus", objectBuilder.build());
        }

        if (vc.proof != null) {
            final var itProof = vc.proof.entrySet().iterator();
            final var objectBuilder = Json.createObjectBuilder();
            while (itProof.hasNext()) {
                addRightJsonType(objectBuilder, itProof.next());
            }
            result.add("proof", objectBuilder.build());
        }

        return result.build();
    }

    @Override
    public VerifiableCredential adaptFromJson(final JsonObject adapted) throws Exception {
        final var vc = new VerifiableCredential();

        if (adapted.containsKey("@context")) {
            vc.context = new ArrayList<String>();
            final var jsonArrayContext = adapted.getJsonArray("@context");
            jsonArrayContext.forEach(value -> vc.context.add(((JsonString) value).getString()));
        }

        if (adapted.containsKey("id")) {
            vc.id = adapted.getString("id");
        }

        if (adapted.containsKey("type")) {
            final var jsonArrayType = adapted.getJsonArray("type");
            vc.type = new ArrayList<String>();
            jsonArrayType.forEach(value -> vc.type.add(((JsonString) value).getString()));
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
            vc.credentialSubject = new HashMap<String, Object>();
            final var jsonObject = adapted.getJsonObject("credentialSubject");
            vc.credentialSubject.putAll(jsonObject);
        }

        if (adapted.containsKey("credentialStatus")) {
            vc.credentialStatus = new HashMap<String, Object>();
            final var jsonObject = adapted.getJsonObject("credentialStatus");
            vc.credentialStatus.putAll(jsonObject);
        }

        if (adapted.containsKey("proof")) {
            vc.proof = new HashMap<String, Object>();
            final var jsonObject = adapted.getJsonObject("proof");
            vc.proof.putAll(jsonObject);
        }

        return vc;
    }

    private <T> void addRightJsonType(final JsonObjectBuilder objectBuilder, final Entry<String, T> entry) {
        if (entry.getValue() instanceof JsonString) {
            objectBuilder.add(entry.getKey(), (JsonString) entry.getValue());
        }
        if (entry.getValue() instanceof JsonNumber) {
            objectBuilder.add(entry.getKey(), (JsonNumber) entry.getValue());
        }
        if (entry.getValue() instanceof JsonArray) {
            final var arrayBuilder = Json.createArrayBuilder();
            ((JsonArray) entry.getValue()).forEach(oneValue -> arrayBuilder.add(oneValue));
            final var jsonArray = arrayBuilder.build();
            objectBuilder.add(entry.getKey(), jsonArray);
        }
        if (entry.getValue() instanceof JsonValue) {
            objectBuilder.add(entry.getKey(), (JsonValue) entry.getValue());
        }
        if (entry.getValue() instanceof JsonObject) {
            final var object = Json.createObjectBuilder();
            final var it = ((JsonObject) entry.getValue()).entrySet().iterator();
            while (it.hasNext()) {
                addRightJsonType(object, it.next());
            }
            objectBuilder.add(entry.getKey(), object);
        }
    }

}
