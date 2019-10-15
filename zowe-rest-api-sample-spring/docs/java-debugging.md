# Java Debugging

Option `-Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=10081,suspend=n` will cause that the service will open a port for remote debugging. The server will start and work as usual. You can connect to the port 10081 from the remote debugger in your IDE.

Example how to start the sample service with the debugging port:

```bash
java -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=10081,suspend=n -jar build/libs/zowe-rest-api-sample-spring-*.jar --spring.config.additional-location=file:config/local/application.yml
```

## Debugging on z/OS

The `zowe-api-dev start` command can insert the right options to the Java on z/OS.

You need to use option `-d` or `--debugPort` on the `start` command. For example:

```bash
zowe-api-dev start --debugPort 10181
```

It can be used with the `--job` option as well:

```bash
zowe-api-dev start --job --debugPort 10181
```

Then you can attach to the Java process on z/OS with your debugger.

If you are using VS Code then you need to change the `hostName` and `port` to your z/OS host and the selected port.

For example:

```json
{
    "type": "java",
    "name": "Debug (Attach) - Remote",
    "request": "attach",
    "hostName": "ca32.lvn.broadcom.net",
    "port": 10181
}
```

## Resources

* [VS Code -Running and Debugging Java](https://code.visualstudio.com/docs/java/java-debugging)
