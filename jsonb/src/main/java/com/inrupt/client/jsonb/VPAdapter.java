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
import com.inrupt.client.spi.VerifiablePresentation;

import java.util.ArrayList;
import java.util.HashMap;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.bind.adapter.JsonbAdapter;

public class VPAdapter implements JsonbAdapter<VerifiablePresentation, JsonObject> {

    @Override
    public JsonObject adaptToJson(final VerifiablePresentation vp) throws Exception {
        final var builder = Json.createArrayBuilder();

        vp.context.forEach(oneContext -> builder.add(oneContext));
        final JsonArray context = builder.build();

        vp.type.forEach(oneType -> builder.add(oneType));
        final JsonArray type = builder.build();

        return Json.createObjectBuilder()
            .add("@context", context)
            .add("id", vp.id)
            .add("type", type)
            .add("holder", vp.holder)
            .add("verifiableCredential", vp.verifiableCredential.toString())
            .add("proof", vp.proof.toString())
            .build();
    }

    @Override
    public VerifiablePresentation adaptFromJson(final JsonObject adapted) throws Exception {
        final var vp = new VerifiablePresentation();

        if (adapted.containsKey("@context")) {
            vp.context = new ArrayList<String>();
            final JsonArray jsonArrayContext = adapted.getJsonArray("@context");
            jsonArrayContext.forEach(value -> vp.context.add(((JsonString)value).getString()));
        }

        if (adapted.containsKey("id")) {
            vp.id = adapted.getString("id");
        }

        if (adapted.containsKey("type")) {
            final JsonArray jsonArrayType = adapted.getJsonArray("type");
            vp.type = new ArrayList<String>();
            jsonArrayType.forEach(value -> vp.type.add(((JsonString)value).getString()));
        }

        if (adapted.containsKey("holder")) {
            vp.holder = adapted.getString("holder");
        }

        if (adapted.containsKey("verifiableCredential")) {
            vp.verifiableCredential = new ArrayList<VerifiableCredential>();
            final JsonArray jsonArrayContext = adapted.getJsonArray("verifiableCredential");
            for (final var value : jsonArrayContext) {
                vp.verifiableCredential.add(new VCAdapter().adaptFromJson(value.asJsonObject()));
            }
        }

        if (adapted.containsKey("proof")) {
            vp.proof = new HashMap<String,Object>();
            final JsonObject jsonObject = adapted.getJsonObject("proof");
            vp.proof.putAll(jsonObject);
        }

        return vp;
    }

}
