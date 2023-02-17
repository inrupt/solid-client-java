# Quarkus-based Example Application

This application uses Quarkus to look up a user's WebID profile and then
display the contents of a user's storage.

The following configuration values need to be added for this to work properly:

```
quarkus.oidc.auth-server-url=<URL of Solid-OIDC server> // mandatory, example https://login.inrupt.com
quarkus.oidc.client-id=<URL of Client Identifier> // mandatory, example https://xxxx/clientid.jsonld
```

_**Note:** client-id is a [Client ID Document](https://solidproject.org/TR/oidc#clientids-document)._

## Running the webapp in development mode

Add the above mentioned properties in the `./src/main/resources/application.properties`.
And then run `./mvnw quarkus:dev -pl examples/webapp`.

The application will be available at `http://localhost:8080`

## Running the webapp in production

These configuration values are typically added to a `./config/application.properties` file in the directory where the application is run.

To run the application, use this command:

```
$ java -jar examples/webapp/target/quarkus-app/quarkus-run.jar
```

The application will be available at `http://localhost:8080`