# Integration tests

The integration tests contain two scenarios of tests:

* resource CRUD related scenarios;
* authentication & authorization scenarios.

All scenarios are detailed in the `./base/src/main/resources/scenarios.ttl` file.

These scenarios can be run against different Solid servers.

## Running the integration tests locally

The default setup (which comes with the project) uses an internal Mock Solid Server. One can simply run the tests locally and they will run on the internal Mock Solid Server. The tests only need a dummy client-id and client-secret as found in the `./base/src/main/resources/META-INF/microprofile-config.properties` file. Navigate to one of the concrete openid or uma tests found in this module and hit `run`.

## Set up a live server

To set up a server one needs to add more configurations to the `./base/src/main/resources/META-INF/microprofile-config.properties` file.

But first let's take a look at the possible configuration values in the properties file.
All the possible value are listed next:

* `inrupt.test.client-id` // mandatory
* `inrupt.test.client-secret` // mandatory
* `inrupt.test.auth-method` // default is `client_secret_basic`
* `inrupt.test.webid`
* `inrupt.test.public-resource-path` // default is no dedicated container, everything gets created on the storage root
* `inrupt.test.private-resource-path` // default is a container named `private`

`inrupt.test.client-id` & `inrupt.test.client-secret` are used to signal the server that this is a registered client. These two fields are mandatory and are the only configuration needed to run the tests on the integrated Mock Solid Server.

`inrupt.test.auth-method` refers to the [client authentication](https://openid.net/specs/openid-connect-core-1_0.html#ClientAuthentication) method and has a default value of `client_secret_basic`. This value is used when this property is not provided.

`inrupt.test.webid` is needed only if we want to run the integration tests on a live service. Otherwise, this property needs to be left out because it will be populated by the Mocked services with a mock username called `someuser`.

`inrupt.test.public-resource-path` & `inrupt.test.private-resource-path` are properties used to fine grain the containers we use for testing.

## The embedded Mock Solid Server

The Mock Solid Server is actually a collection of services which try to mock, as true as possible, a live setup of a Solid Server (which also complies to specifications). For these purpose we list the following services involved in the mocking of a Solid Server:

* `MockWebIdService` - mocks the basics of a WebId provider service which returns a WebId Profile Document. In our tests, the WebId Profile Document is presented as a Turtle file which contains a bare minimum information about the storage location linked to this WebID and the location of the Solid-OIDC issuer.

* `MockOpenIDProvider` - mocks the Identity Provider (IdP) service which is linked in the WebId Profile Document (solid:oidcissuer). The basic features of the mocked IdP are:
  * to provide information on its discovery endpoint (found under `/.well-known/openid-configuration`);
  * provide a token on its token endpoint (found under `oauth/oauth20/token`);
  * provide a jwks on its jwks endpoint (found under `oauth/jwks`).

* `MockSolidServer` - mocks the storage service of a Pod provider. It mocks the behavior of private and public resources by looking if the resource path contains the `inrupt.test.private-resource-path`. And it mocks, according to Solid Protocol methods like GET, PUT, POST and PATCH.

* `MockUMAAuthorizationServer` - mocks the authorization service, in our case a UMA service. UMA authorization is the default, hard-coded, in the Mocked services. This can be seen in any request on a private resource in the MockSolidServer. When not authorized, the Solid Server will respond with a WWW-Authenticate header which contains a UMA ticket.
