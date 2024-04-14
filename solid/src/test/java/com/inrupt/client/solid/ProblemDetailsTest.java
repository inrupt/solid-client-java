package com.inrupt.client.solid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.inrupt.client.Headers;
import com.inrupt.client.HttpStatus;
import com.inrupt.client.ProblemDetails;
import com.inrupt.client.spi.JsonService;
import com.inrupt.client.spi.ServiceProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

// Ideally, this class should be in the api module, but it creates a circular dependency with the JSON module implementation.
public class ProblemDetailsTest {
    Headers mockProblemDetailsHeader() {
        final List<String> headerValues = new ArrayList<>();
        headerValues.add("application/problem+json");
        final Map<String, List<String>> headerMap = new HashMap<>();
        headerMap.put("Content-Type", headerValues);
        return Headers.of(headerMap);
    }

    final JsonService jsonService = ServiceProvider.getJsonService();
    @Test
    void testEmptyProblemDetails() {
        final int statusCode = 400;
        final ProblemDetails pd = ProblemDetails.fromErrorResponse(
                statusCode,
                mockProblemDetailsHeader(),
                "{}".getBytes(),
                jsonService
        );
        assertEquals(ProblemDetails.DEFAULT_TYPE, pd.getType().toString());
        assertEquals(statusCode, pd.getStatus());
        Assertions.assertEquals("Bad Request", pd.getTitle());
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
                "}").getBytes(),
                jsonService
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
                "}").getBytes(),
                jsonService
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
                    "\"status\":\"Some invalid status\"," +
                "}").getBytes(),
                jsonService
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
                        "\"status\":500," +
                "}").getBytes(),
                jsonService
        );
        assertEquals(statusCode, pd.getStatus());
    }

    @Test
    void testInvalidTypeProblemDetails() {
        final ProblemDetails pd = ProblemDetails.fromErrorResponse(
                400,
                mockProblemDetailsHeader(),
                ("{" +
                    "\"type\":\"Some invalid type\"," +
                "}").getBytes(),
                jsonService
        );
        assertEquals(ProblemDetails.DEFAULT_TYPE, pd.getType().toString());
    }

    @Test
    void testInvalidInstanceProblemDetails() {
        final ProblemDetails pd = ProblemDetails.fromErrorResponse(
                400,
                mockProblemDetailsHeader(),
                ("{" +
                    "\"instance\":\"Some invalid instance\"," +
                "}").getBytes(),
                jsonService
        );
        assertNull(pd.getInstance());
    }
}
