# z/OS Security

## Types of API Services

The are two possible types of API services for z/OS:

1. Running on z/OS

    - Such service can work directly with z/OS resources (data sets, zFS file systems, spool, call other z/OS services via native code - HLASM, Metal C, or XL C/C++ via JNI or [IBM JZOS](https://www.ibm.com/support/knowledgecenter/en/SSYKE2_8.0.0/com.ibm.java.zsecurity.api.80.doc/com.ibm.jzos/index.html) or other [z/OS specific Java APIs provided by IBM](https://www.ibm.com/support/knowledgecenter/en/SSYKE2_8.0.0/com.ibm.java.zsecurity.api.80.doc/zsecurity-overview.html)
    - Runs under **service user ID**
      - This user ID should have only the necessary permissions for the operation of the service (access to the working directory - e.g. instance directory that can contain temporary or persisted data of the service)
      - This user ID should not be given access to mainframe resources of the user. If the service needs to access such data in need to switch the security context of the current thread
    - The access to mainframe resources that belong to z/OS users is done under the security context of the user that is using the REST API

2. Running off z/OS

    - Such services can run off z/OS (but they can run on z/OS too)
    - They cannot use any specific z/OS APIs in Java or use JNI
    - They can access z/OS resources only by calling API services running on z/OS (from the group #1 or provided by IBM - for example: [z/OSMF REST API Services](https://www.ibm.com/support/knowledgecenter/en/SSLTBW_2.3.0/com.ibm.zos.v2r3.izua700/IZUHPINFO_RESTServices.htm))
    - They can be based on z/OS security (and should in case when they access z/OS resources) by calling REST API of the [Zowe Authentication and Authorization Service](https://github.com/zowe/api-layer/wiki/Zowe-Authentication-and-Authorization-Service)

## Authentication

Application need to support **HTTP Basic** authentication. While this is not supporting MFA, it provides the most simple way how to use the APIs. The access to the API service is done via HTTPS so the credentials are transferred securely. This level of support is enough for early versions of the REST API service that are being validation by customer before GA.

The REST API service is protected by HTTP Basic authentication that is connected to `ZosAuthenticationProvider`.

If the Spring profile `zos` is active, then this provider uses `SafPlatformUser` class that uses reflection to
call `com.ibm.os390.security.PlatformUser` class. This class is available in IBM® SDK for z/OS®, Java™ Technology Edition in `racf.jar`.
This JAR is not available in public repositories and cannot be added to this repository. These APIs are documented in <https://www.ibm.com/support/knowledgecenter/en/SSYKE2_8.0.0/com.ibm.java.zsecurity.80.doc/zsecurity-component/saf.html>.

When you run it outside of z/OS without `zos` profile, a mock implementation `MockPlatformUser` is used. This accepts user ID `zowe` and password `zowe`.

Applications should support stateless token-based authentication provided by [Zowe Authentication and Authorization Service](https://github.com/zowe/api-layer/wiki/Zowe-Authentication-and-Authorization-Service). This is not implemented yet.

## Security Context

HTTP requests in the API services are processed by thread that are started under the security context of the server. If your code needs to access mainframe resources under the security context of the user that is issuing the REST API request in need to do following:

1. Enforce authentication for the API request
2. Switch security context for the code that needs it

Both tasks are easy with the REST API SDK.

Let's assume that we have REST API endpoint `/api/v1/my/endpoint` implemented in `MyController` class implemented by method `endpoint`:

```java
@RestController
@RequestMapping("/api/v1/my")
public class MyController {
    @GetMapping("/endpoint")
    public String endpoint() {
        // Your code is running under the service user ID, not the client user ID
    }
}
```

We would like to access it only when user has provided valid credentials. In the sample service, it can be done
by adding following lines to the `SecurityConfiguration` class and its `configure(HttpSecurity http)` method:

```java
// endpoint protection
.and()
.authorizeRequests()
.antMatchers("/api/v1/my/endpoint").authenticated()
```

Once the the endpoint is protected, user needs to provide authentication. The way how the authentication can be provided is configured in the same class `SecurityConfiguration` in `configure(AuthenticationManagerBuilder auth)`. This class is a subclass of `WebSecurityConfigurerAdapter`.

If you need you need to have the information about the authenticated user then you can add `org.springframework.security.core.Authentication` as a parameter to your endpoint method signature:

```java
import org.springframework.security.core.Authentication;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/api/v1/my")
public class MyController {
    @GetMapping("/endpoint")
    public String endpoint(@ApiIgnore Authentication authentication) {
        // New parameter:  <--------------------------->
        String userId = authentication.getName();
        // Your code is running under the service user ID, not the client user ID
    }
}
```

If you want to run some code under the user ID of the client, you have to several options:

 1. Wrap the code into `Runnable` interface using methods in `PlatformThreadLevelSecurity` run it as you need
 2. Wrap the code into `Callable` interface using `PlatformThreadLevelSecurity` and run it as you need

> **Tip** `Runnable` is good when you just need to run the code but you are not interested in its return value (you can return the result by changing some data structures that you pass as parameters). `Callable` is better when you need to return a single value to the caller. More details are at <https://www.baeldung.com/java-runnable-callable>. Since REST API usually return a response, a `Callable` will be used more.

The term **as you need** means that you can:

 1. Run it synchronously in the current thread
 2. Submit it as a new thread
 3. Use a thread pool

```java
import org.zowe.sdk.zos.security.PlatformThreadLevelSecurity;
import com.ibm.jzos.ZUtil;

    @GetMapping("/endpoint")
    public String endpoint(Authentication a) throws Exception {
        String outputValue = (String) platformThreadLevelSecurity
                .wrapCallableInEnvironmentForAuthenticatedUser(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        // This code is running under the security context (thread-level environment) of the user that has authenticated to the REST API request
                        return ZUtil.getCurrentUser();
                    }
                }).call();
        // This code is running under service user ID
    }
```

**Notes:**

- If you want to get the result of the `Callable` as a part of the REST API request, then calling the `Callabe` synchronously is going to be the typical option. It means to call the `call()` method as in the example. Tomcat (which is used as the embedded web server) has already a thread pool so there is no need to create a new one for processing of REST responses.

- Using a thread pool makes sense when you want to start some activities in parallel or without waiting for the response (e.g. process multiple things in parallel, start a long-running task that can take minutes to complete, background processing...).

- Starting an unlimited number of new threads is not a good idea since you will hit some limit sooner or later and there is an overhead connected to each thread (both in MVS and in Java), so it makes sense only for "service threads" that live with the application. But you do not want to create a new thread for each request that starts some long-running activity since it is very easy for users to do thousands of such requests and start thousands of such threads.

- It makes sense to create a "thread pool" by using <https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Executors.html#newFixedThreadPool-int-> and submit/invoke the Runnables/Callables that are not meant to be done as a part of the REST API request in it. The size of the thread pools (in Tomcat, or for background tasks) will vary for each REST API service and expected number of users. In some cases, it will make sense to make it configurable by the user.

### Security Context Requirements

The change of the security environment/context of a Java thread can be done in multiple ways:

1. Use [pthread_security_applid_np — Create or delete thread-level security](https://www.ibm.com/support/knowledgecenter/en/SSLTBW_2.3.0/com.ibm.zos.v2r3.bpxbd00/ptsec.htm) in XL C via JNI
2. Use [BPX4TLS callable service](https://www.ibm.com/support/knowledgecenter/en/SSLTBW_2.3.0/com.ibm.zos.v2r3.bpxb100/tls.htm) in Metal C and change the APPLID of the current TCB
3. Use `ThreadSubject.doAsPrivileged` and `LoginContext` (<https://www.ibm.com/support/knowledgecenter/en/SSYKE2_8.0.0/com.ibm.java.zsecurity.api.80.doc/com.ibm.security.auth/com/ibm/security/auth/module/JAASLoginModule.html>)

Changing a Java thread security environment affects the actions that are done in that thread no matter if it is Java, XL C/C++, Metal C or HLASM code. The security context is set for the underlying TCB.

We recommend to use #1 since it has no dependencies (but #2 has) and the shared libraries can be distributed with JARs. It allows the usage of PassTickets (but #3 does not). It allows you to add additional native C and C++ code in a way that is preferred by most of the developers.

In case of #1 and #2 there are two methods with different security requirements:

- a) The service user ID has UPDATE access to the BPX.DAEMON resource in the FACILITY class
  - This allows to use `pthread_security_applid_np` without password
  - The service needs to validate the authentication (e.g. Zowe JWT token) so there is not need to obtain the PassTicket or remember the password

- b) The service user ID has READ access to the BPX.SERVER resource in the FACILITY class
  - The `pthread_security_applid_np` needs to be called with a password or a valid PassTicket
  - This requires a REST API that generates a PassTicket if you have a valid JWT token

Currenly **#1 a)** - `pthread_security_applid_np` via JNI without password and `BPX.DAEMON` requirement is implemented.

**#1 b)** is defined in the backlog as <https://github.com/zowe/sample-spring-boot-api-service/issues/17>.

#### Implementation

TODO: Update links to classes

The flow of the execution from the top layer to bottom:

1. [`SecurityContextController.authenticatedUser()`](/src/main/java/org/zowe/sample/apiservice/security/SecurityContextController.java#47) - example of an REST API controller that is using security context switching

2. [`WebSecurityConfig.configure(HttpSecurity)`](/src/main/java/org/zowe/sample/apiservice/config/WebSecurityConfig.java#29) - configures the HTTP endpoint security and requires authentication for the HTTP requests that is necessary for changing the security context:

    ```java
    .antMatchers("/api/v1/my/endpoint").authenticated()
    ```

3. `platformThreadLevelSecurity` - a bean that implements [`PlatformThreadLevelSecurity`](/src/main/java/com/ca/mfaas/sampleservice/security/PlatformThreadLevelSecurity.java) interface to to thread-level security context change functions such as `wrapCallableInEnvironmentForAuthenticatedUser` or `wrapRunnableInEnvironmentForAuthenticatedUser` that allows you to wrap any code to the z/OS security environment of the user that has authenticated to the REST API

4. [`CallInThreadLevelSecurityEnvironmentByDaemon`](src/main/java/org/zowe/sdk/zos/security/thread/PlatformThreadLevelSecurity.java) and [`RunInThreadLevelSecurityEnvironmentByDaemon`](src/main/java/org/zowe/sdk/zos/security/thread/RunInThreadLevelSecurityEnvironmentByDaemon.java) - classes that implement standard Java interfaces `Callable` and `Runnable` that can wrap any existing code in `Callable` and `Runnable` into a code that will switch the security context just for the code in them

5. `PlatformSecurityService` - interfaces for low-level security functions that are z/OS platform dependent
   - `ZosJniPlatformSecurityService` - implementation of the previous interface for z/OS that is using JNI to call the native code that provides security functionality on z/OS - this is annotated by `@Profile("zos")` so it is used only when the application is running on z/OS
   - `DummyPlatformSecurityService` - dummy implementation that can run anywhere but it is not integrated with z/OS security

6. [`Secur.java`](/src/main/java/org/zowe/sdk/zos/security/jni/Secur.java) - defines the native methods to Java and load the shared library to Java

7. [`secur.c`](/zossrc/Secur.c) implements the native code in XL C, it is linked to `libsecur.so` library

> **Note** The files above are a part of the sample application so you need to copy them to your application. There is a future work defined to move them to an SDK library (<https://github.com/zowe/sample-spring-boot-api-service/issues/5>).

At this point, we leave our code and we are calling services provided by z/OS:

1. [`pthread_security_applid_np()`](https://www.ibm.com/support/knowledgecenter/en/SSLTBW_2.3.0/com.ibm.zos.v2r3.bpxbd00/ptsec.htm) is a function from XL C runtime library that is called to switch the security context

2. [`BPX4TLS`](https://www.ibm.com/support/knowledgecenter/en/SSLTBW_2.3.0/com.ibm.zos.v2r3.bpxb100/tls.htm) - a callable service that is used by the previous function

3. `RACROUTE REQUEST=AUTH` - Assembler Callable Service to call functionality provided by *System authorization facility (SAF)*

4. [System authorization facility (SAF)](https://www.ibm.com/support/knowledgecenter/en/SSLTBW_2.3.0/com.ibm.zos.v2r3.ichc600/safa.htm#safa)

5. Security product (CA Top Secret, CA ACF2, IBM RACF)

#### Building

See [z/OS Native OS Linkage](zos-native-os-linkage.md) for instructions how to build z/OS native modules, namely `libsecur.so`.

#### Packaging

The shared library `libsecur.so` is expected to be in zFS filesystem in the a directory that is in the `LIBPATH`. `LIBPATH` in an environment variable that contains directories that search for the native load libraries.

#### Links

- <https://www.ibm.com/support/knowledgecenter/en/SSLTBW_2.3.0/com.ibm.zos.v2r3.bpxb100/tls.htm>
