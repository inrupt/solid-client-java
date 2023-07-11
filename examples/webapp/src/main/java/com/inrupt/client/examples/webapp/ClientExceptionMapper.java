/*
 * Copyright Inrupt Inc.
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
package com.inrupt.client.examples.webapp;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.slf4j.LoggerFactory.getLogger;

import com.inrupt.client.solid.DataMappingException;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.io.ByteArrayInputStream;
import java.util.Collection;

import org.slf4j.Logger;

@Provider
public class ClientExceptionMapper implements ExceptionMapper<DataMappingException> {

    private static final Logger LOGGER = getLogger(ClientExceptionMapper.class);

    @CheckedTemplate
    static class Templates {
        private static native TemplateInstance error(
                String message,
                Collection<String> errors);
    }

    @Override
    public Response toResponse(final DataMappingException err) {
        LOGGER.debug("Web Application Error: {}", err.getMessage());

        final var errorHtml = Templates.error("Invalid container", err.getValidationResults()).render();

        final var response = new ByteArrayInputStream(errorHtml.getBytes(UTF_8));

        return Response.status(400)
                       .entity(response)
                       .type(MediaType.TEXT_HTML)
                       .build();
    }
}
