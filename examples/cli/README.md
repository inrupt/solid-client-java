# ONe way to run the cli example

The `AppConfig` class depends on some properties. Make sure to first add the `src/main/resources/application.properties` the following properties:

* inrupt.examples.cli.issuer= // mandatory -> is the URL where you login to your WebID provider
* inrupt.examples.cli.client-id= // mandatory -> make sure to first register your cli at the provider registration endpoint
* inrupt.examples.cli.client-secret= // mandatory -> make sure to first register your cli at the provider registration endpoint

Also make sure you have install cleaned the entire project beforehand, to make sure all dependencies are loaded, with  `./mvnw clean install`.
And then you can run the example with `./mvnw quarkus:dev -pl example/cli`.