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
import com.inrupt.client.api.VerifiablePresentation;

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

public class VPAdapter implements JsonbAdapter<VerifiablePresentation, JsonObject> {

    @Override
    public JsonObject adaptToJson(final VerifiablePresentation vp) throws Exception {
        final var result = Json.createObjectBuilder();

        if (vp.context != null) {
            final var arrayBuilder = Json.createArrayBuilder();
            vp.context.forEach(oneContext -> arrayBuilder.add(oneContext));
            result.add("@context", arrayBuilder.build());
        }

        if (vp.type != null) {
            final var arrayBuilder = Json.createArrayBuilder();
            vp.type.forEach(oneType -> arrayBuilder.add(oneType));
            result.add("type", arrayBuilder.build());
        }

        if (vp.id != null) {
            result.add("id", vp.id);
        }

        if (vp.holder != null) {
            result.add("holder", vp.holder);
        }

        if (vp.verifiableCredential != null) {
            final var arrayBuilder = Json.createArrayBuilder();
            final var itVC = vp.verifiableCredential.iterator();
            while (itVC.hasNext()) {
                arrayBuilder.add(new VCAdapter().adaptToJson(itVC.next()));
            }
            result.add("verifiableCredential", arrayBuilder.build());
        }

        if (vp.proof != null) {
            final var itProof = vp.proof.entrySet().iterator();
            final var objectBuilder = Json.createObjectBuilder();
            while (itProof.hasNext()) {
                addRightJsonType(objectBuilder, itProof.next());
            }
            result.add("proof", objectBuilder.build());
        }

        return result.build();
    }

    @Override
    public VerifiablePresentation adaptFromJson(final JsonObject adapted) throws Exception {
        final var vp = new VerifiablePresentation();

        if (adapted.containsKey("@context")) {
            vp.context = new ArrayList<String>();
            final var jsonArrayContext = adapted.getJsonArray("@context");
            jsonArrayContext.forEach(value -> vp.context.add(((JsonString) value).getString()));
        }

        if (adapted.containsKey("id")) {
            vp.id = adapted.getString("id");
        }

        if (adapted.containsKey("type")) {
            final var jsonArrayType = adapted.getJsonArray("type");
            vp.type = new ArrayList<String>();
            jsonArrayType.forEach(value -> vp.type.add(((JsonString) value).getString()));
        }

        if (adapted.containsKey("holder")) {
            vp.holder = adapted.getString("holder");
        }

        if (adapted.containsKey("verifiableCredential")) {
            vp.verifiableCredential = new ArrayList<VerifiableCredential>();
            final var jsonArrayContext = adapted.getJsonArray("verifiableCredential");
            for (final var value : jsonArrayContext) {
                vp.verifiableCredential.add(new VCAdapter().adaptFromJson(value.asJsonObject()));
            }
        }

        if (adapted.containsKey("proof")) {
            vp.proof = new HashMap<String, Object>();
            final var jsonObject = adapted.getJsonObject("proof");
            vp.proof.putAll(jsonObject);
        }

        return vp;
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
