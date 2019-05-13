# z/OS Security

## Authentication

The REST API service is protected by HTTP Basic authentication that is connected to `ZosAuthenticationProvider`.

If the Spring profile `zos` is active, then this provider uses `SafPlatformUser` class that uses reflection to
call `com.ibm.os390.security.PlatformUser` class. This class is available in IBM® SDK for z/OS®, Java™ Technology Edition in `racf.jar`.
This JAR is not available in public repositories and cannot be added to this repository. These APIs are documented in <https://www.ibm.com/support/knowledgecenter/en/SSYKE2_8.0.0/com.ibm.java.zsecurity.80.doc/zsecurity-component/saf.html>.

When you run it outside of z/OS without `zos` profile, a mock implementation `MockPlatformUser` is used. This accepts user ID `zowe` and password `zowe`. 
