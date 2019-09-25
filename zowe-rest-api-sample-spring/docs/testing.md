# Testing REST API Services

## Terminology

The terminology around testing is often vague and different terms can have slightly different meaning to people. We will try to use the commonly used terminology in the Java and agile development world.

## Types of Test

- **Unit tests** - The majority of the code should be tested by unit tests. A unit test tests a unit of the code. The unit should should be reasonably small - a function, a method, or a class, and should be tested in isolation without other units. The sample uses [JUnit 4](https://junit.org/junit4/) testing framework which is a standard for Java development. While you can use it to drive tests of multiple units together (integration tests), it is recommended to keep the unit tests small so your tests are fast and reliable. If your class uses other classes, you should use [dependency injection in Spring](https://www.baeldung.com/inversion-control-and-dependency-injection-in-spring) to provide a mock implementation of other classs for your unit test. You can use a mocking framework [Mockito](https://site.mockito.org/) to mock classes. The sample has these tests in `src/test/java` directory.

- **REST controller tests** - The REST endpoints are developed using Spring MVC controllers. It makes sense to the logic of the controllers using Spring's [MockMvc](https://spring.io/guides/gs/testing-web/) or [RestAssuredMockMvc](https://www.baeldung.com/spring-mock-mvc-rest-assured). These tests the controller without starting the web service and you should provide mocks for classes that used by your controller so your tests can run fast and outside of z/OS as a part of the test suite that is executed by `./gradlew test` or `./gradlew build`. The sample has these tests in `src/test/java` directory in the same packages as the tested controllers. For example: [GreetingControllerTests.java](../src/test/java/org/zowe/sample/apiservice/greeting/GreetingControllerTests.java). You should test every functionality of the controller using these tests.

- **REST API integration tests** - Unit tests are good but you need to be sure that the API service works when all units are used together and on z/OS platform. You should have a smaller set of tests (e.g. one per controller) that make sure that all things can be started on z/OS, loaded, and work well together. The integration tests in `src/integrationTest/java` allow to test REST APIs against a running service on your workstation or on z/OS using the HTTP protocol as the API clients will do. These tests are slower and require full build and deployment of the API service so their number should be small and should not replace the testing done by unit tests. Example: [GreetingControllerIntegrationTests.java](../src/test/java/org/zowe/sample/apiservice/greeting/GreetingControllerIntegrationTests.java). The sample tests were developed in Java using [REST-assured](http://rest-assured.io/). There are many other possibilities (different frameworks and programming languages). The integration tests are not executed as a part of regular build. You should start in your CI pipeline. Follow these steps to start them manually during development:

    1. Start an instance of the service:

        - `./gradlew bootRun` (localhost)
        - `zowe-api-dev start` (z/OS)

    2. Run the tests:

        - `./gradlew integrationTest` (localhost with default port)
        - `TEST_BASE_URI=https://<hostname> TEST_PORT=<port> TEST_USERID=<userid> TEST_PASSWORD=<password> TEST_WAIT_MINUTES=5 ./gradlew integrationTest` (z/OS or other host)

- **z/OS native code testing** - TODO (will be addressed in a different user story)

## Resources

- Technical resources:
  - [Intro to Inversion of Control and Dependency Injection with Spring](https://www.baeldung.com/inversion-control-and-dependency-injection-in-spring)
  - [REST-assured](http://rest-assured.io/)
  - [REST-assured Support for Spring MockMvc](https://www.baeldung.com/spring-mock-mvc-rest-assured)
  - [Mockito](https://static.javadoc.io/org.mockito/mockito-core/3.0.0/org/mockito/Mockito.html)

- Testing resources:
  - <https://martinfowler.com/bliki/UnitTest.html>
  - <https://martinfowler.com/bliki/IntegrationTest.html>

- Scaled Agile Framework (SAFe) resources:
  - [Built-In Quality](https://www.scaledagileframework.com/built-in-quality/)
  - [Test Driven Development](https://www.scaledagileframework.com/test-driven-development/)
  - [Agile Testing](https://www.scaledagileframework.com/agile-testing/)
