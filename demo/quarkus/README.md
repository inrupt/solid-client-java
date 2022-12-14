# Quarkus-based Demo Application

This application uses Quarkus to look up a user's WebID profile and then
display the contents of a user's storage.

The following configuration values need to be added for this to work properly:

```
quarkus.oidc.auth-server-url=<URL of Solid-OIDC server>
quarkus.oidc.client-id=<URL of Client Identifier>
```

_**Note:** client-id is a [Client ID Document](https://solid.github.io/solid-oidc/#clientids-document). You can use the [Client Identifier Helper tool](https://client-identifiers.tools.inrupt.com/generator) to generate or validate one.

These configuration values are typically added to a `./config/application.properties` file in the directory where the application is run.

To run the application, use this command:

```
$ java -jar demo/quarkus/target/quarkus-app/quarkus-run.jar
```

The application will be available at `http://localhost:8080`

