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
package com.inrupt.client;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;

import org.junit.jupiter.api.Test;

class URIBuilderTest {

    @Test
    void testPassThroughDid() {
        final URI uri = URI.create("did:solid:pod.example:container/resource");
        assertEquals(uri, URIBuilder.newBuilder(uri).build());
    }

    @Test
    void testBuildDidPath() {
        final URI uri = URI.create("did:solid:pod.example:data/");
        final URI expected = URI.create("did:solid:pod.example:data/container/resource");

        assertEquals(expected, URIBuilder.newBuilder(uri).path("container").path("resource").build());
        assertEquals(expected, URIBuilder.newBuilder(uri).path("/container/").path("/resource").build());
    }

    @Test
    void testEscapedCharacters() {
        final URI uri = URI.create("did:solid:location");
        final URI expected = URI.create("did:solid:location/un%7Cwise%7Bcharacters%7D");

        assertEquals(expected, URIBuilder.newBuilder(uri).path("/un|wise{characters}").build());
    }

    @Test
    void testPassThrough() {
        final URI uri = URI.create("https://solid.example/pod/container/resource");
        assertEquals(uri, URIBuilder.newBuilder(uri).build());
    }

    @Test
    void testBuildPath() {
        final URI uri = URI.create("https://solid.example/pod/");
        final URI expected = URI.create("https://solid.example/pod/container/resource");

        assertEquals(expected, URIBuilder.newBuilder(uri).path("container").path("resource").build());
        assertEquals(expected, URIBuilder.newBuilder(uri).path("/container/").path("/resource").build());
    }

    @Test
    void testExistingParams() {
        final URI uri = URI.create("https://solid.example/container/?a=one&b=two");
        final URI expected = URI.create("https://solid.example/container/resource?a=one&b=two&c=three#fragment");

        assertEquals(expected, URIBuilder.newBuilder(uri).path("resource").queryParam("c", "three")
                .fragment("fragment").build());
    }

    @Test
    void testBuildUrlWithFragment() {
        final URI uri = URI.create("https://pod.example/");
        final URI expected = URI.create("https://pod.example/container/resource#fragment");

        assertEquals(expected, URIBuilder.newBuilder(uri).path("/container").path("resource")
                .fragment("fragment").build());
    }

    @Test
    void testBuildUrlWithParams() {
        final URI uri = URI.create("https://pod.example");
        final URI expected = URI.create("https://pod.example/resource?param1=first&param2=second");

        assertEquals(expected, URIBuilder.newBuilder(uri).path("resource")
                .queryParam("param1", "first")
                .queryParam("param2", "second")
                .build());
    }

    @Test
    void testBuildWithEmptyParams() {
        final URI uri = URI.create("https://data.example/resource?a=&b=&c=");
        final URI expected = URI.create("https://data.example/resource?a=&b=&c=&d=&e=");

        assertEquals(expected, URIBuilder.newBuilder(uri).queryParam("d", null).queryParam("e", "").build());
    }

    @Test
    void testBuildWithNullQueryParam() {
        final URI uri = URI.create("https://pod.example/container/");
        final URI expected = URI.create("https://pod.example/container/?key=");

        assertEquals(expected, URIBuilder.newBuilder(uri).queryParam("key", null).build());
    }

    @Test
    void testBuildWithEncodedPaths() {
        final URI uri = URI.create("https://pod.example/cont%3Aainer/");
        final URI expected = URI.create("https://pod.example/cont:ainer/");

        assertEquals(expected, URIBuilder.newBuilder(uri).build());
    }

    @Test
    void testBuildWithEncodedQueryParams() {
        final URI uri = URI.create("did:web:solid.example:data");
        final URI expected = URI.create("did:web:solid.example:data?key=data%20point");

        assertEquals(expected, URIBuilder.newBuilder(uri).queryParam("key", "data point").build());
    }

    @Test
    void testBuildWithNoPath() {
        final URI uri = URI.create("https://pod.example");
        final URI expected = URI.create("https://pod.example#fragment");

        assertEquals(expected, URIBuilder.newBuilder(uri).fragment("fragment").build());
    }

    @Test
    void testBuildNullBlankPath() {
        final URI uri = URI.create("https://pod.example/");
        final URI expected = URI.create("https://pod.example/data/");

        assertEquals(expected, URIBuilder.newBuilder(uri).path(null).path("").path("/data/").build());
    }
}
