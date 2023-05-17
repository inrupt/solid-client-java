/*
 * Copyright 2023 Inrupt Inc.
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
package com.inrupt.client.accessgrant.accessGrant;

import com.inrupt.client.Response;
import com.inrupt.client.accessgrant.AccessGrantException;
import com.inrupt.client.spi.JsonService;
import com.inrupt.client.spi.ServiceProvider;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class VCBodyHandlers {
    private static final JsonService jsonService = ServiceProvider.getJsonService();

    /**
     * Create a {@link VerifiableCredential} from an HTTP response.
     *
     * @return the body handler
     */
    public static Response.BodyHandler<VerifiableCredential> ofVerifiableCredential() {
        return responseInfo -> {
            final int httpStatus = responseInfo.statusCode();
            if (httpStatus >= 200 && httpStatus < 300) {
                try (final InputStream input = new ByteArrayInputStream(responseInfo.body().array())) {
                    return jsonService.fromJson(input, VerifiableCredential.class);
                } catch (final IOException ex) {
                    throw new AccessGrantException("Error parsing credential", ex);
                }
            }
            throw new AccessGrantException(
                "Unexpected error response when handling a verifiable credential.",
                httpStatus);
        };
    }

    /**
     * Create a {@link VerifiablePresentation} from an HTTP response.
     *
     * @return the body handler
     */
    public static Response.BodyHandler<VerifiablePresentation> ofVerifiablePresentation() {
        return responseInfo -> {
            final int httpStatus = responseInfo.statusCode();
            if (httpStatus >= 200 && httpStatus < 300) {
                try (final InputStream input = new ByteArrayInputStream(responseInfo.body().array())) {
                    return jsonService.fromJson(input, VerifiablePresentation.class);
                } catch (final IOException ex) {
                    throw new AccessGrantException("Error parsing presentation", ex);
                }
            }
            throw new AccessGrantException(
                "Unexpected error response when handling a verifiable presentation.",
                httpStatus);
        };
    }

    private VCBodyHandlers() {
        // Prevent instantiation
    }
}
