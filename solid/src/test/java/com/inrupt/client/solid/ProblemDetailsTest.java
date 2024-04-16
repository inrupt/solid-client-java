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
package com.inrupt.client.solid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.inrupt.client.Headers;
import com.inrupt.client.ProblemDetails;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

// Ideally, this class should be in the api module, but it creates
// a circular dependency with the JSON module implementation.
public class ProblemDetailsTest {
    Headers mockProblemDetailsHeader() {
        final List<String> headerValues = new ArrayList<>();
        headerValues.add("application/problem+json");
        final Map<String, List<String>> headerMap = new HashMap<>();
        headerMap.put("Content-Type", headerValues);
        return Headers.of(headerMap);
    }

    @Test
    void testEmptyProblemDetails() {
        final int statusCode = 400;
        final ProblemDetails pd = ProblemDetails.fromErrorResponse(
                statusCode,
                mockProblemDetailsHeader(),
                "{}".getBytes()
        );
        assertEquals(ProblemDetails.DEFAULT_TYPE, pd.getType().toString());
        assertEquals(statusCode, pd.getStatus());
        assertNull(pd.getTitle());
        assertNull(pd.getDetails());
        assertNull(pd.getInstance());
    }
    @Test
    void testCompleteProblemDetails() {
        final int statusCode = 400;
        final ProblemDetails pd = ProblemDetails.fromErrorResponse(
                statusCode,
                mockProblemDetailsHeader(),
                ("{" +
                    "\"title\":\"Some title\"," +
                    "\"status\":400," +
                    "\"details\":\"Some details\"," +
                    "\"instance\":\"https://example.org/instance\"," +
                    "\"type\":\"https://example.org/type\"" +
                "}").getBytes()
        );
        assertEquals("https://example.org/type", pd.getType().toString());
        assertEquals(statusCode, pd.getStatus());
        Assertions.assertEquals("Some title", pd.getTitle());
        assertEquals("Some details", pd.getDetails());
        assertEquals("https://example.org/instance", pd.getInstance().toString());
    }

    @Test
    void testIgnoreUnknownProblemDetails() {
        final int statusCode = 400;
        final ProblemDetails pd = ProblemDetails.fromErrorResponse(
                statusCode,
                mockProblemDetailsHeader(),
                ("{" +
                    "\"title\":\"Some title\"," +
                    "\"status\":400," +
                    "\"details\":\"Some details\"," +
                    "\"instance\":\"https://example.org/instance\"," +
                    "\"type\":\"https://example.org/type\"," +
                    "\"unknown\":\"Some unknown property\"" +
                "}").getBytes()
        );
        assertEquals("https://example.org/type", pd.getType().toString());
        assertEquals(statusCode, pd.getStatus());
        Assertions.assertEquals("Some title", pd.getTitle());
        assertEquals("Some details", pd.getDetails());
        assertEquals("https://example.org/instance", pd.getInstance().toString());
    }

    @Test
    void testInvalidStatusProblemDetails() {
        final int statusCode = 400;
        final ProblemDetails pd = ProblemDetails.fromErrorResponse(
                statusCode,
                mockProblemDetailsHeader(),
                ("{" +
                    "\"status\":\"Some invalid status\"" +
                "}").getBytes()
        );
        assertEquals(statusCode, pd.getStatus());
    }

    @Test
    void testMismatchingStatusProblemDetails() {
        final int statusCode = 400;
        final ProblemDetails pd = ProblemDetails.fromErrorResponse(
                statusCode,
                mockProblemDetailsHeader(),
                ("{" +
                        "\"status\":500" +
                "}").getBytes()
        );
        assertEquals(500, pd.getStatus());
    }

    @Test
    void testInvalidTypeProblemDetails() {
        final ProblemDetails pd = ProblemDetails.fromErrorResponse(
                400,
                mockProblemDetailsHeader(),
                ("{" +
                    "\"type\":\"Some invalid type\"" +
                "}").getBytes()
        );
        assertEquals(ProblemDetails.DEFAULT_TYPE, pd.getType().toString());
    }

    @Test
    void testInvalidInstanceProblemDetails() {
        final ProblemDetails pd = ProblemDetails.fromErrorResponse(
                400,
                mockProblemDetailsHeader(),
                ("{" +
                    "\"instance\":\"Some invalid instance\"" +
                "}").getBytes()
        );
        assertNull(pd.getInstance());
    }
}
