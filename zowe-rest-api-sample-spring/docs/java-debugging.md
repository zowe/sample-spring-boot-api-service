# Java Debugging

Option `-Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=10081,suspend=n` will cause that the service will open a port for remote debugging. The server will start and work as usual. You can connect to the port 10081 from the remote debugger in your IDE.

Example how to start the sample service with the debugging port:

```bash
java -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=10081,suspend=n -jar build/libs/zowe-rest-api-sample-spring-*.jar --spring.config.additional-location=file:config/local/application.yml
```
