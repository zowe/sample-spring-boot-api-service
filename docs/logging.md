# Logging

The default settings of the sample application produce a clean log that contains
only important informational messages from the REST API service user perspective.
Warnings and errors will be displayed. But information for developers are not included by default.

## Debug Profile

In order to see debugging messages that are useful for developers, a debugging profile needs to be enabled.
This profile is called `debug`.

You can pass it as a command line argument `--spring.profiles.include=debug` when you start the JAR. For example:

    java -jar build/libs/*.jar --spring.config.additional-location=file:./config/local/application.yml --spring.profiles.include=debug

## Diagnostics Profile

There is a profile called `diag` that is enabled in `config/local/application.yml`.

This profile enables all Spring Actuator endpoints that you can see at: https://localhost:10080/actuator
