# Configuration

Java applications that are using Spring Boot have configuration their configuration in the [`application.yml`](/src/main/resources/application.yml) and/or `bootstrap.yml` files. These files are packaged in the JAR file so the user cannot change them.

There are various ways how these values can be overridden in Spring Boot (external YML file, environment variables, Java System properties).

The Java System properties are a common way how to provide external configuration options. They are defined using `-D` options for Java. Java System properties can override any configuration.

The values for Java System properties can be defined in STDENV member of JZOS Batch Launcher for Java started tasks on z/OS.

When your run the sample application on your computer using `gradlew bootRun` it is using external configuration file [`config/local/application.yml`](/config/local/application.yml). This file contains the value for running on your computer in development mode.

## Overriding Properties using CLI

Let's say that we want to use port `10081` instead of default `10080`. This is set by property `server.port`.

Running from JAR:

    java -Dserver.port=10081 -jar build/libs/*.jar --spring.config.additional-location=file:./config/local/application.yml

Running by Gradle:

    ./gradlew bootRun --args=='--spring.config.additional-location=file:./config/local/application.yml --server.port=10081'

## YAML Files Conventions

1. Extension is `.yml`
2. Indentation is 4 spaces
3. Property names are using `camelCase` 
4. The `application.yml` bundled into the JAR should contain only valid or typical values for any deployment (ie. it should not contain values that are correct only on your computer)

## Resources

- Intro to Spring YML configuration - http://www.baeldung.com/spring-yaml
- Full reference - https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html
- For advanced use case of sharing a common YML file - http://roufid.com/load-multiple-configuration-files-different-directories-spring-boot/
