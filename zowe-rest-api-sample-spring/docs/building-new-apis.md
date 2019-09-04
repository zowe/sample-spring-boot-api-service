# Building New APIs

As a Zowe API developer, follow the guidelines in this article to build new mainframe REST API services. These guidelines are demonstrated in the [Zowe Sample API Service](https://github.com/zowe/sample-spring-boot-api-service/blob/master/zowe-rest-api-sample-spring/).

## Where to Start

Begin by familiarizing yourself with the API-subject area so that you are able to answer the following questions:

- What is a REST API?
- What is Swagger?
- What is JSON?

See the following websites for related materials about APIs, Swagger and JSON:

- <http://www.andrewhavens.com/posts/20/beginners-guide-to-creating-a-rest-api/>
- <https://blog.readme.io/what-is-swagger-and-why-it-matters/>

## Define Use Cases

For an API to be useful, it needs to address a specific use case. Both product management and engineers are responsible to identify a use case and design each API accordingly.

Example: File Master Plus delivers an API to manipulate z/OS data sets (<https://docops.ca.com/ca-file-master-plus/11/en/using-the-rest-api>) for use in its Zowe CLI plugin.

First, identify your use cases. Second, design an API (list of URL endpoints, parameters, responses → API specification) that serves this use case. A widely recognized API example is the PetStore: <https://petstore.swagger.io/>

## API Implementation

How to implement and deliver an API service is a key consideration for an engineering team.  Accordingly, there are multiple questions that need to be addressed when implementing and delivering an API service:

- What programming language to choose
- What testing framework is to be used
- Build CI/CD
- How to package and deliver the software
- z/OS security
- HTTPS encryption
- ...

The Zowe Sample API Service is designed to assist developers in addressing these issues.

## Overview of the Zowe Sample API Service

The Zowe Sample API Service helps mainframe development teams create REST APIs for their mainframe products quicker and in a standardized and proven way.

The sample is built in Java with Spring Boot which is the preferred method to create new mainframe REST APIs. This approach enables you to connect an API to legacy mainframe code with proper security and avoids unnecessary complications. Spring Boot provides many ways to create REST APIs which promotes consistency between Zowe APIs and their corresponding APIs.

The SDK also covers the life-cycle of application development. This life-cycle includes unit testing, integration testing on and off of z/OS, deployment of z/OS test systems.
This is provided by the [Zowe API Development CLI Tool](devtool.md).

## Target Users

The primary users of the sample REST API service are Zowe developers who want to develop or enhance a REST API for a mainframe product.

Some teams have developers who are strong in Java and have good knowledge of Spring Boot but may not have the necessary z/OS programming skills. Other teams have experienced mainframe developers who have basic knowledge of Java. All teams have the common goal of spending less time writing boilerplate code for REST APIs, researching how to use JNI or the Zowe JWT token, or inventing a way to deploy non-mainframe build artifacts to an z/OS. This time savings provides developers with more time to focus on adding useful functionality to their product. Following the SDK promotes consistency and enables Zowe users to create new APIs with much less hassle.

## Usage

Use the Sample Service as a template to understand how an API service can be implemented. To familiarize yourself with the SDK, download, build and run the sample project. After you familiarize yourself with the sample project you will need to re-write the specific code to suit the needs of your API service.

Start with following the [README document](../README.md) of the Zowe Sample API Service.
