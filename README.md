# Solid Client libraries for Solid

## Building

The project can be built with Maven and a Java 11+ build environment.

    ./mvnw install

## Documentation

The [project documentation](https://inrupt.github.io/solid-client-java/apidocs/index.html) can be built with the command:

    ./mvnw site

## Using

To add the client libraries to an application, include the following in your project's POM:

    <dependency>
      <groupId>com.inrupt</groupId>
      <artifactId>inrupt-client-core</artifactId>
      <version>${inrupt.client.version}</version>
    </dependency>
    <dependency>
      <groupId>com.inrupt</groupId>
      <artifactId>inrupt-client-solid</artifactId>
      <version>${inrupt.client.version}</version>
    </dependency>
    <dependency>
      <groupId>com.inrupt</groupId>
      <artifactId>inrupt-client-openid</artifactId>
      <version>${inrupt.client.version}</version>
    </dependency>
    <dependency>
      <groupId>com.inrupt</groupId>
      <artifactId>inrupt-client-jena</artifactId>
      <version>${inrupt.client.version}</version>
    </dependency>
    <dependency>
      <groupId>com.inrupt</groupId>
      <artifactId>inrupt-client-jackson</artifactId>
      <version>${inrupt.client.version}</version>
    </dependency>
    <dependency>
      <groupId>com.inrupt</groupId>
      <artifactId>inrupt-client-httpclient</artifactId>
      <version>${inrupt.client.version}</version>
    </dependency>


