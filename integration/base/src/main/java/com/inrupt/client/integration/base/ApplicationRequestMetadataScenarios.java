package com.inrupt.client.integration.base;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.inrupt.client.Headers;
import com.inrupt.client.auth.Credential;
import com.inrupt.client.auth.Session;
import com.inrupt.client.openid.OpenIdException;
import com.inrupt.client.openid.OpenIdSession;
import com.inrupt.client.solid.SolidClient;
import com.inrupt.client.solid.SolidClientException;
import com.inrupt.client.solid.SolidRDFSource;
import com.inrupt.client.solid.SolidSyncClient;
import com.inrupt.client.spi.RDFFactory;
import com.inrupt.client.util.URIBuilder;
import com.inrupt.client.webid.WebIdProfile;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.apache.commons.rdf.api.Dataset;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Literal;
import org.apache.commons.rdf.api.RDF;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
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

    private static final String APPLICATION_REQUEST_METADATA_FEATURE = config
            .getOptionalValue("inrupt.test.feature.application-request-metadata", String.class)
            .orElse("false");

    private static final String AUTH_METHOD = config
            .getOptionalValue("inrupt.test.auth-method", String.class)
            .orElse("client_secret_basic");
    private static final String CLIENT_ID = config.getValue("inrupt.test.client-id", String.class);
    private static final String CLIENT_SECRET = config.getValue("inrupt.test.client-secret", String.class);

    private static final String FOLDER_SEPARATOR = "/";

    private static URI publicContainerURI;
    private static URI privateContainerURI;

    private static SolidSyncClient authenticatedClient;
    private static Session session;

    @BeforeAll
    static void setup() {
       /* if (!featureIsActive()) {
            LOGGER.info("ApplicationRequestMetadataScenarios are skipped, feature not active");
            return;
        }*/
        LOGGER.info("Setup ApplicationRequestMetadataScenarios test");
        if (config.getOptionalValue("inrupt.test.webid", String.class).isPresent()) {
            LOGGER.info("Running ApplicationRequestMetadataScenarios on live server");
            webidUrl = config.getOptionalValue("inrupt.test.webid", String.class).get();
        }

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
    }

    @AfterAll
    static void teardown() {
       // if (featureIsActive()) {
            //cleanup pod
            Utils.deleteContentsRecursively(authenticatedClient, publicContainerURI);
            Utils.deleteContentsRecursively(authenticatedClient, privateContainerURI);
      //  }

    }

    @ParameterizedTest
    @MethodSource("provideSessions")
    @DisplayName(" " +
            "Request and response headers match for a successful authenticated request")
    void requestResponseMatchOnAuthRequestTest(final Session session) {
       // assumeTrue(featureIsActive());

        LOGGER.info("Integration Test - Request and response headers match for a successful authenticated request");

        final Headers applicationHeaders = Headers.of(
                Map.of("somecid", List.of("a6d87d0e-2454-4501-8110-ecc082aa975f"))
        );

        final SolidClient authClient = SolidClient.getClientBuilder()
                .headers(applicationHeaders).build()
                .session(session);

        final String resourceName = privateContainerURI + "e2e-test-application-metadata1";
        final String predicateName = "https://example.example/predicate";
        final IRI booleanType = rdf.createIRI("http://www.w3.org/2001/XMLSchema#boolean");

        final IRI subject = rdf.createIRI(resourceName);
        final IRI predicate = rdf.createIRI(predicateName);
        final Literal objectTrue = rdf.createLiteral("true", booleanType);

        // Populate data for this resource
        final Dataset dataset = rdf.createDataset();
        dataset.add(null, subject, predicate, objectTrue);

        // Create a new resource and check response
        final URI resourceUri = URI.create(resourceName);
        authClient.create(
                new SolidRDFSource(resourceUri, dataset)).thenAccept(response -> {

            assertEquals("0d1e63a3-b635-4d50-ba7f-34b7176defdf",
                    response.getHeaders().allValues("somecid").get(0));
        });
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

    private static boolean featureIsActive() {
        return APPLICATION_REQUEST_METADATA_FEATURE.equals("true");
    }
}
