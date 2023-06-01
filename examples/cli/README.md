# One way to run the cli example

The `AppConfig` class depends on some properties. Make sure to first add the `src/main/resources/application.properties` the following properties:

```
inrupt.examples.cli.issuer=<URL of Solid-OIDC server> // mandatory, example https://login.inrupt.com
inrupt.examples.cli.client-id=<your registered client_id> // mandatory
inrupt.examples.cli.client-secret=<your registered client_secret> // mandatory
```

_**Note:** client-id & client_secret you receive when you register your client at the Solid-OIDC server registration endpoint, example for PodSpaces [https://login.inrupt.com/registration.html](https://login.inrupt.com/registration.html)._

Also make sure you have install cleaned the entire project beforehand, to make sure all dependencies are loaded, with  `./mvnw clean install`.
And then you can run the example with:

```
cd examples/cli
#make sure you are using java 17
../../mvnw clean compile
../../mvnw quarkus:dev -pl .
```

And follow the instructions from quarkus to add command line options.