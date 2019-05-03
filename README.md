# [Zowe](https://zowe.org/) Sample API Service &middot; [![GitHub license](https://img.shields.io/badge/license-EPL2.0-blue.svg)](https://github.com/plavjanik/sample-spring-boot-api-service/blob/master/LICENSE) [![CircleCI](https://circleci.com/gh/plavjanik/sample-spring-boot-api-service.svg?style=shield&circle-token=53a413d5029b55efba2b0b8273aa7fe1be7f6b02)](https://circleci.com/gh/plavjanik/sample-spring-boot-api-service) [![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](ttps://github.com/plavjanik/sample-spring-boot-api-service/blob/master/README.md)

## Prerequisites

Following platform is required to run the application:

* Java JDK 8
  * For example: <https://adoptopenjdk.net/releases.html?variant=openjdk8&jvmVariant=openj9>
  * Set `JAVA_HOME` system variable with a valid JDK path and add `${JAVA_HOME}/bin` to the PATH variable

## Quick Start

Build you application:

    ./gradlew build

**Note:** On Windows use `gradlew` instead of `./gradlew`

Start your application:

    ./gradlew bootRun

## Use the application

Open <http://localhost:10080/api/v1/greeting> in your browser or favorite REST API client (for example [HTTPie](https://httpie.org/), [REST Client for VSCode](https://marketplace.visualstudio.com/items?itemName=humao.rest-client), or [Insomnia](https://insomnia.rest/).

## Learn More about Gradle and Spring Boot

### Reference Documentation

For further reference, please consider the following sections:

* [Official Gradle documentation](https://docs.gradle.org)

### Guides

The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/bookmarks/)
* [Building a RESTful Web Service with Spring Boot Actuator](https://spring.io/guides/gs/actuator-service/)

### Additional Links

These additional references should also help you:

* [Gradle Build Scans – insights for your project's build](https://scans.gradle.com#gradle)
