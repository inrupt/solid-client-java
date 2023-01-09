# Java Client Libraries for Solid

## Building

The project can be built with Maven and a Java 11+ build environment.

    ./mvnw install

## Documentation

The [project documentation](https://inrupt.github.io/solid-client-java/apidocs/index.html) can be built with the command:

    ./mvnw site

## Using

To add the client libraries to an application, include the following in your project's POM:

    <dependencyManagement>
      <dependencies>
        <dependency>
          <groupId>com.inrupt</groupId>
          <artifactId>inrupt-client-bom</artifactId>
          <version>${inrupt.client.version}</version>
        </dependency>
      </dependencies>
    </dependencyManagement>

    <dependencies>
      <dependency>
        <groupId>com.inrupt</groupId>
        <artifactId>inrupt-client-solid</artifactId>
      </dependency>
      <dependency>
        <groupId>com.inrupt</groupId>
        <artifactId>inrupt-client-openid</artifactId>
      </dependency>
      <dependency>
        <groupId>com.inrupt</groupId>
        <artifactId>inrupt-client-core</artifactId>
      </dependency>
      <dependency>
        <groupId>com.inrupt</groupId>
        <artifactId>inrupt-client-jena</artifactId>
      </dependency>
      <dependency>
        <groupId>com.inrupt</groupId>
        <artifactId>inrupt-client-jackson</artifactId>
      </dependency>
      <dependency>
        <groupId>com.inrupt</groupId>
        <artifactId>inrupt-client-httpclient</artifactId>
      </dependency>
    </dependencies>


