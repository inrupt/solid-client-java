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
package com.inrupt.client.integration.base;

import static com.inrupt.client.integration.base.Utils.TEXT_TURTLE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import com.inrupt.client.Headers;
import com.inrupt.client.Request;
import com.inrupt.client.Response;
import com.inrupt.client.auth.Credential;
import com.inrupt.client.auth.Session;
import com.inrupt.client.openid.OpenIdException;
import com.inrupt.client.openid.OpenIdSession;
import com.inrupt.client.solid.*;
import com.inrupt.client.spi.RDFFactory;
import com.inrupt.client.util.URIBuilder;
import com.inrupt.client.webid.WebIdProfile;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.*;
import java.util.stream.Stream;

import org.apache.commons.rdf.api.Dataset;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Literal;
import org.apache.commons.rdf.api.RDF;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationRequestMetadataScenarios {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationRequestMetadataScenarios.class);
    private static String podUrl;
    private static String webidUrl;

    private static final Config config = ConfigProvider.getConfig();

    private static String issuer;

    private static final RDF rdf = RDFFactory.getInstance();

    //feature is deactivated by default
    private static final String REQUEST_METADATA_FEATURE = config
            .getOptionalValue("inrupt.test.request-metadata.feature", String.class)
            .orElse("false");

    private static final String[] REQUEST_METADATA_HEADERS_THAT_PROPAGATE = config
            .getOptionalValue("inrupt.test.request-metadata-headers-that-propagate", String[].class)
            .orElse(new String[0]);

    private static final String AUTH_METHOD = config
            .getOptionalValue("inrupt.test.auth-method", String.class)
            .orElse("client_secret_basic");
    private static final String CLIENT_ID = config.getValue("inrupt.test.client-id", String.class);
    private static final String CLIENT_SECRET = config.getValue("inrupt.test.client-secret", String.class);
    private static final Boolean INRUPT_TEST_ERROR_DESCRIPTION_FEATURE =
            config.getValue("inrupt.test.error-description.feature", Boolean.class);


    private static final String FOLDER_SEPARATOR = "/";

    private static URI publicContainerURI;
    private static URI privateContainerURI;

    private static SolidSyncClient authenticatedClient;
    private static Session session;

    @BeforeAll
    static void setup() {
        LOGGER.info("Setup ApplicationRequestMetadataScenarios test");
        if (config.getOptionalValue("inrupt.test.webid", String.class).isPresent()) {
            LOGGER.info("Running ApplicationRequestMetadataScenarios on live server");
            webidUrl = config.getOptionalValue("inrupt.test.webid", String.class).get();

            State.WEBID = URI.create(webidUrl);
            //find storage from WebID using domain-specific webID solid concept
            final SolidSyncClient client = SolidSyncClient.getClient().session(Session.anonymous());
            try (final WebIdProfile sameProfile = client.read(URI.create(webidUrl), WebIdProfile.class)) {
                final var storages = sameProfile.getStorages();
                issuer = sameProfile.getOidcIssuers().iterator().next().toString();
                if (!storages.isEmpty()) {
                    podUrl = storages.iterator().next().toString();
                }

                session = OpenIdSession.ofClientCredentials(
                        URI.create(issuer), //Client credentials
                        CLIENT_ID,
                        CLIENT_SECRET,
                        AUTH_METHOD);

                authenticatedClient = client.session(session);
            } catch (SolidClientException ex) {
                LOGGER.error("problems reading the webId");
            }

            publicContainerURI = URIBuilder.newBuilder(URI.create(podUrl))
                .path("public-domain-test-" + UUID.randomUUID() + FOLDER_SEPARATOR)
                .build();

            Utils.createPublicContainer(authenticatedClient, publicContainerURI);

            privateContainerURI = URIBuilder.newBuilder(URI.create(podUrl))
                .path(State.PRIVATE_RESOURCE_PATH + "-auth-test-" + UUID.randomUUID() + "/")
                .build();

            Utils.createContainer(authenticatedClient, privateContainerURI);

            LOGGER.info("Integration Test Pod Host: [{}]", URI.create(podUrl).getHost());
        } else {
            LOGGER.info("ApplicationRequestMetadataScenarios are skipped, feature not active");
        }
    }

    @AfterAll
    static void teardown() {
        //cleanup pod
        if (publicContainerURI != null) {
            Utils.deleteContentsRecursively(authenticatedClient, publicContainerURI);
        }
        if (privateContainerURI != null) {
            Utils.deleteContentsRecursively(authenticatedClient, privateContainerURI);
        }
    }

    @ParameterizedTest
    @EnabledIf("featureIsActive")
    @MethodSource("provideSessions")
    @DisplayName("https://w3id.org/inrupt/qa/manifest/solid-client-java/authClientCreateResourceWithHeaders " +
            "Request and response headers match for a successful authenticated request")
    void matchOnAuthRequestSyncLowLevelClientTest(final Session session) {

        LOGGER.info("Integration Test - Low level sync client - " +
                "Request and response headers match for a successful authenticated request");

        final SolidSyncClient authClient = SolidSyncClient.getClient().session(session);

        final String resourceName = privateContainerURI + "e2e-test-application-metadata-" + UUID.randomUUID();
        final String predicateName = "https://example.example/predicate";
        final IRI booleanType = rdf.createIRI("http://www.w3.org/2001/XMLSchema#boolean");

        final IRI subject = rdf.createIRI(resourceName);
        final IRI predicate = rdf.createIRI(predicateName);
        final Literal objectTrue = rdf.createLiteral("true", booleanType);

        // Populate data for this resource
        final Dataset dataset = rdf.createDataset();
        dataset.add(null, subject, predicate, objectTrue);

        final URI resourceUri = URI.create(resourceName);

        final var headers = fillHeaders("aaaaaa");

        // Create a new resource and check response headers
        final Request.Builder reqBuilder = Request.newBuilder(resourceUri)
                .header(Utils.CONTENT_TYPE, Utils.TEXT_TURTLE)
                .PUT(Request.BodyPublishers.noBody());

        //fill the configured headers
        headers.forEach((key,value) -> {
            reqBuilder.header(key, value.get(0));
        });

        final var req = reqBuilder.build();

        final var res = authClient.send(req, Response.BodyHandlers.discarding());

        assertTrue(Utils.isSuccessful(res.statusCode()));

        for (String value: REQUEST_METADATA_HEADERS_THAT_PROPAGATE) {
            assertEquals(headers.get(value), res.headers().allValues(value));
        }
    }

    @ParameterizedTest
    @EnabledIf("featureIsActive")
    @MethodSource("provideSessions")
    @DisplayName("https://w3id.org/inrupt/qa/manifest/solid-client-java/authClientCreateResourceWithHeaders " +
            "Request and response headers match for a successful authenticated request")
    void matchOnAuthRequestAsyncHighLevelClientTest(final Session session) {

        LOGGER.info("Integration Test - High level async client -" +
                " Request and response headers match for a successful authenticated request");

        final var headers = fillHeaders("bbbbbb");
        headers.put("someheader", List.of("bbbbbb-2454-4501-8110-ecc082aa975f"));

        final SolidClient authClient = SolidClient.getClientBuilder()
                        .headers(Headers.of(headers)).build()
                        .session(session);

        final String resourceName = privateContainerURI + "e2e-test-application-metadata-" + UUID.randomUUID();
        final String predicateName = "https://example.example/predicate";
        final IRI booleanType = rdf.createIRI("http://www.w3.org/2001/XMLSchema#boolean");

        final IRI subject = rdf.createIRI(resourceName);
        final IRI predicate = rdf.createIRI(predicateName);
        final Literal objectTrue = rdf.createLiteral("true", booleanType);

        // Populate data for this resource
        final Dataset dataset = rdf.createDataset();
        dataset.add(null, subject, predicate, objectTrue);

        // Create a new resource and check response headers
        final URI resourceUri = URI.create(resourceName);
        authClient.create(new SolidRDFSource(resourceUri, dataset)).thenAccept(response -> {
            for (String value: REQUEST_METADATA_HEADERS_THAT_PROPAGATE) {
                assertEquals(headers.get(value), response.getHeaders().allValues(value));
            }
            assertTrue(response.getHeaders().allValues("someheader").isEmpty());
        });
    }

    @ParameterizedTest
    @EnabledIf("featureIsActive")
    @MethodSource("provideSessions")
    @DisplayName("https://w3id.org/inrupt/qa/manifest/solid-client-java/authClientCreateResourceWithHeaders " +
            "Request and response headers match for a successful authenticated request")
    void matchOnAuthRequestSyncHighLevelClientTest(final Session session) {

        LOGGER.info("Integration Test - High level sync client -" +
                " Request and response headers match for a successful authenticated request");

        final var headers = fillHeaders("ccccc");
        headers.put("someheader", List.of("ccccc-2454-4501-8110-ecc082aa975f"));

        final SolidSyncClient authClient = SolidSyncClient.getClientBuilder()
                .headers(Headers.of(headers)).build()
                .session(session);

        final String resourceName = privateContainerURI + "e2e-test-application-metadata-" + UUID.randomUUID();
        final String predicateName = "https://example.example/predicate";
        final IRI booleanType = rdf.createIRI("http://www.w3.org/2001/XMLSchema#boolean");

        final IRI subject = rdf.createIRI(resourceName);
        final IRI predicate = rdf.createIRI(predicateName);
        final Literal objectTrue = rdf.createLiteral("true", booleanType);

        // Populate data for this resource
        final Dataset dataset = rdf.createDataset();
        dataset.add(null, subject, predicate, objectTrue);

        // Create a new resource and check response headers
        final URI resourceUri = URI.create(resourceName);
        try ( var resource = authClient.create(new SolidRDFSource(resourceUri, dataset))) {
            for (String header: REQUEST_METADATA_HEADERS_THAT_PROPAGATE) {
                assertEquals(headers.get(header), resource.getHeaders().allValues(header));
            }
            assertTrue(resource.getHeaders().allValues("someheader").isEmpty());
        }
    }

    @ParameterizedTest
    @EnabledIf("featureIsActive")
    @MethodSource("provideSessions")
    @DisplayName("https://w3id.org/inrupt/qa/manifest/solid-client-java/unAuthClientCreateResourceWithHeaders " +
            "Request and response headers match for a successful unauthenticated request")
    void matchOnUnAuthRequestSyncHighLevelClientTest(final Session session) {

        LOGGER.info("Integration Test - High level sync client -" +
                " Request and response headers match for a successful unauthenticated request");

        final var resourceUri = URI.create(publicContainerURI + UUID.randomUUID().toString());

        try (final var testResource = new SolidRDFSource(resourceUri)) {
            final var requestHeaders = fillHeaders("unAuth");
            final var client = SolidSyncClient.getClientBuilder()
                    .headers(Headers.of(requestHeaders)).build();

            try (var response = client.create(testResource)) {
                matchHeaders(requestHeaders, response.getHeaders());
            }
        }
    }

    @ParameterizedTest
    @EnabledIf("featureIsActive")
    @MethodSource("provideSessions")
    @DisplayName("https://w3id.org/inrupt/qa/manifest/solid-client-java/authClientCannotCreateResourceWithHeaders " +
            "Request and response headers match for a failed authenticated request")
    void matchOnAuthFailedRequestSyncHighLevelClientTest(final Session session) {

        LOGGER.info("Integration Test - High level sync client -" +
                " Request and response headers match for a failed authenticated request");

        final var resourceUri = URI.create(privateContainerURI + UUID.randomUUID().toString());
        final var invalidPayload = new ByteArrayInputStream("invalid".getBytes(UTF_8));

        try (final var testResource = new SolidNonRDFSource(resourceUri, TEXT_TURTLE, invalidPayload)) {
            final var requestHeaders = fillHeaders("badRequest");
            final var client = SolidSyncClient.getClientBuilder()
                    .headers(Headers.of(requestHeaders)).build()
                    .session(session);

            final var exception = assertThrows(BadRequestException.class, ()-> client.create(testResource));

            matchHeaders(requestHeaders, exception.getHeaders());
            assertEquals(INRUPT_TEST_ERROR_DESCRIPTION_FEATURE, Utils.checkProblemDetails(exception).isPresent());
            Utils.checkProblemDetails(exception).ifPresent(problemDetails -> {
                assertEquals("Bad Request", problemDetails.getTitle());
                assertNotNull(problemDetails.getInstance());
                assertNotNull(problemDetails.getDetail());
            });
        }
    }

    @ParameterizedTest
    @EnabledIf("featureIsActive")
    @MethodSource("provideSessions")
    @DisplayName("https://w3id.org/inrupt/qa/manifest/solid-client-java/unClientCannotCreateResourceWithHeaders " +
            "Request and response headers match for a failed unauthenticated request")
    void matchOnUnAuthFailedRequestSyncHighLevelClientTest(final Session session) {

        LOGGER.info("Integration Test - High level sync client -" +
                " Request and response headers match for a failed unauthenticated request");

        final var resourceUri = URI.create(privateContainerURI + UUID.randomUUID().toString());

        try (final var testResource = new SolidRDFSource(resourceUri)) {
            final var requestHeaders = fillHeaders("authFailed");
            final var client = SolidSyncClient.getClientBuilder()
                    .headers(Headers.of(requestHeaders)).build();

            final var exception = assertThrows(UnauthorizedException.class, ()-> client.create(testResource));

            matchHeaders(requestHeaders, exception.getHeaders());
            assertEquals(INRUPT_TEST_ERROR_DESCRIPTION_FEATURE, Utils.checkProblemDetails(exception).isPresent());
            Utils.checkProblemDetails(exception).ifPresent(problemDetails -> {
                assertEquals("Unauthorized", problemDetails.getTitle());
                assertNotNull(problemDetails.getInstance());
                assertNotNull(problemDetails.getDetail());
            });
        }
    }

    @SuppressWarnings("unchecked")
    private void matchHeaders(final Map<String, List<String>> requestHeaders, final Headers responseHeaders) {
        final var receivedHeaders = Arrays.asList(responseHeaders
                .asMap()
                .entrySet()
                .toArray(Map.Entry[]::new));
        final var expectedHeaders = requestHeaders.entrySet().stream()
                .map(kv -> hasItem(both(
                        hasProperty("key", equalTo(kv.getKey()))).and(
                        hasProperty("value", equalTo(kv.getValue())))))
                .toArray(Matcher[]::new);

        assertThat(receivedHeaders, is(allOf(expectedHeaders)));
    }

    private static Stream<Arguments> provideSessions() throws SolidClientException {
        final Session session = OpenIdSession.ofClientCredentials(
                URI.create(issuer), //Client credentials
                CLIENT_ID,
                CLIENT_SECRET,
                AUTH_METHOD);
        final Optional<Credential> credential = session.getCredential(OpenIdSession.ID_TOKEN, null);
        final var token = credential.map(Credential::getToken)
                .orElseThrow(() -> new OpenIdException("We could not get a token"));
        return Stream.of(
                        Arguments.of(OpenIdSession.ofIdToken(token), //OpenId token
                        Arguments.of(session)));
    }

    static boolean featureIsActive() {
        return REQUEST_METADATA_FEATURE.equals("true");
    }

    private Map<String, List<String>> fillHeaders(final String headerValue) {
        final var headers = new HashMap<String, List<String>>();
        for (String header: REQUEST_METADATA_HEADERS_THAT_PROPAGATE) {
            headers.put(header, List.of(headerValue + "-" + UUID.randomUUID()));
        }
        return headers;
    }
}
