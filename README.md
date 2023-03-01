# Java Client Libraries for Solid

[![Contributor Covenant](https://img.shields.io/badge/Contributor%20Covenant-2.1-4baaaa.svg)](CODE-OF-CONDUCT.md)

This project adheres to the Contributor Covenant [code of conduct](CODE-OF-CONDUCT.md). By participating, you are expected to uphold this code. Please report unacceptable behavior to [engineering@inrupt.com](mailto:engineering@inrupt.com).

The Inrupt Java Client Libraries provide highly flexible, simple components that can be used in any Java application for [Solid](https://solidproject.org/). The libraries are composed of different modules that follow a layered architectural approach.

## Using the libraries as a dependency in your own projects

To use the Inrupt Java Client Libraries in your projects make sure to visit the [Set Up page](https://inrupt.github.io/solid-client-java/setUp.html) of the documentation.

Next we will document how to use this repository locally.

## Using this repository locally

After cloning the repository locally you can work with the code as follows:

### Code build

The project can be built with Maven and a Java 11+ build environment.

```bash
    ./mvnw install
```

### Documentation build

The [project documentation](https://inrupt.github.io/solid-client-java/apidocs/index.html) can be built with the command:

```bash
    ./mvnw site
```

### Running tests

The repository contains multiple tests. Each module has dedicated unit tests. And there is a module dedicated to integration testing in the [integration module](https://github.com/inrupt/solid-client-java/tree/main/integration).
By running the following command all tests (including integration tests) are run:

```bash
    ./mvnw test
```

The integration tests come with a dedicated Mocked Solid Server. Their configuration can be setup in such a way that the integration tests can be run also on love Solid Servers. More about their setup and configuration on the [integration test README](https://github.com/inrupt/solid-client-java/blob/main/integration/README.md).

#### Code coverage

This project uses JaCoCo for generating the code coverage metric that measures how many lines of code are executed during automated tests. To generate the reports (in different formats) run:


```bash
    ./mvnw verify
```

The reports are then place in the `report/target/site` folder on the project root.

## Issues & Help

### Solid Community Forum

If you have questions about working with Solid or just want to share what you’re
working on, visit the [Solid forum](https://forum.solidproject.org/). The Solid
forum is a good place to meet the rest of the community.

### Bugs and Feature Requests

- For public feedback, bug reports, and feature requests please file an issue
  via [Github](https://github.com/inrupt/solid-client-java/issues/).
- For non-public feedback or support inquiries please use the [Inrupt Service
  Desk](https://inrupt.atlassian.net/servicedesk).

## Documentation

- [Inrupt Java Client Libraries getting started](https://inrupt.github.io/solid-client-java/index.html/)
- [Inrupt Java Client Libraries javadocs](https://inrupt.github.io/solid-client-java/apidocs/)

## Changelog

## License

MIT © [Inrupt](https://inrupt.com)