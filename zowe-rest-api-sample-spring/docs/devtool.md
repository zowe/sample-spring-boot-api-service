# zowe-api-dev - Zowe API Development CLI Tool

`zowe-api-dev` is a command-line tool to automate some tasks during API development on z/OS.
This tool is experimental.

**Warning!** This tool is in experimental state. It will be improved a lot in future and some
things may change and break before a table version is released.

It uses the project-specific definitions in `zowe-api.json`.
This file is check in to the version control system and it does not contain user specific information.

## Installation

## From NPM

```bash
npm -g install @zowedev/zowe-api-dev
```

**Note:** The organization will be changed to `@zowe` when the tool is stable.

### From sources

```bash
https://github.com/zowe/sample-spring-boot-api-service
cd sample-spring-boot-api-service
git checkout sdk-split
cd zowe-api-dev
npm link
```

### Configuration

Install Zowe CLI from NPM [@zowe/cli](https://www.npmjs.com/package/@zowe/cli). Version 6.0.0 or above is required.

```bash
npm install -g @zowe/cli
```

You need to setup `zosmf` and `ssh` profiles:

```bash
zowe profiles create zosmf-profile ca32 --host ca32.lvn.broadcom.net --port 1443 --user <userid> --pass "<password>" --reject-unauthorized false
zowe profiles create ssh-profile ca32 --host ca32.lvn.broadcom.net --user <userid> --password "<password>"
```

## Using

Change directory to the project that you want to work with.

```bash
cd zowe-rest-api-sample-spring
```

If you are using the project for the first time, you need to provide user-specific values in `user-zowe-api.json`.
This file should not be check in to the version control system. You can initialize it quickly using:

```bash
zowe-api-dev init --account=129300000 --zosHlq=PLAPE03.ZOWE.SAMPLE --zosTargetDir=/a/plape03/sample
```

The account number is required on systems that require it to be set correctly. All parameters have default values that are derived from your profile but you can specify them explicitly.

It creates a file `user-zowe-api.json` in the current working directory with following content:

```json
{
    "zosTargetDir": "/a/plape03/sample",
    "zosHlq": "PLAPE03.ZOWE.SAMPLE",
    "jobcard": [
        "//PLAPE03Z JOB 129300000,'ZOWE API',MSGCLASS=A,CLASS=A,",
        "//  MSGLEVEL=(1,1),REGION=0M",
        "/*JOBPARM SYSAFF=*"
    ]
}
```

Next step is to allocate a ZFS filesystem with enought space. You can skip this step if your directory has enough space.

```bash
zowe-api-dev zfs
```

You should get message `PLAPE03.ZOWE.SAMPLE.ZFS mounted at /a/plape03/sample` at the end.

You can use the same command to mount your filesystem again if it is not mounted. You can specify additional parameters for `zfsadm define` in this way `zowe-api-dev zfs --defineParams="-storageclass TSO"`. The amount of space is defined in `zowe-api.json` as `zfsMegabytes` property.

The sample application contains native code. It needs to be build on z/OS. Following command uploads source for to z/OS and builds it. It downloads the build artifacts so they can be integrated to your jar file.

```bash
zowe-api-dev zosbuild
```

Then you need to build the Java application:

```bash
./gradlew build
```

The result of the build is a jar file. You need to deploy it to z/OS using:

```bash
zowe-api-dev deploy
```

That will deploy the jar file to the `bin` directory and extract `.so` file from it into the `lib` directory.

All the binaries are there but you need to configure the application too. The current sample application require `port` to be configured.
The following command takes configuration file and templates from `config/zos` and sets the port to `10180`.
You a port number that is available for you.

```bash
zowe-api-dev config --name zos --parameter port=10180
```

The last step is to start the applicaiton.

```bash
zowe-api-dev start
```

`zowe-api-dev start` is the fastest option that starts the Java application on z/OS in SSH session. You can see the output immediatelly:

```txt
2019-09-02 05:12:28.039 <ZWEASA1:main:17171692> PLAPE03 (org.zowe.sample.apiservice.ZoweApiServiceApplication:50) INFO Starting ZoweApiServiceApplication on USILCA32 with PID 17171692 (/a/plape03/sample/bin/zowe-rest-api-sample-spring.jar started by PLAPE03 in /a/plape03/sample)
2019-09-02 05:12:28.043 <ZWEASA1:main:17171692> PLAPE03 (org.zowe.sample.apiservice.ZoweApiServiceApplication:679) INFO The following profiles are active: https,diag,zos
2019-09-02 05:12:30.715 <ZWEASA1:main:17171692> PLAPE03 (org.springframework.boot.web.embedded.tomcat.TomcatWebServer:90) INFO Tomcat initialized with port(s): 10180 (https)
2019-09-02 05:12:34.730 <ZWEASA1:main:17171692> PLAPE03 (org.springframework.boot.web.embedded.tomcat.TomcatWebServer:204) INFO Tomcat started on port(s): 10180 (https) with context path ''
2019-09-02 05:12:34.732 <ZWEASA1:main:17171692> PLAPE03 (org.zowe.sample.apiservice.ZoweApiServiceApplication:59) INFO Started ZoweApiServiceApplication in 8.648 seconds (JVM running for 9.504)
2019-09-02 05:12:34.743 <ZWEASA1:main:17171692> PLAPE03 (org.zowe.sdk.spring.ServiceStartupEventHandler:25) INFO Zowe Sample API Service has been started in 9.515 seconds
```

But the application stops when your session is terminated.

The application is available at URL: `https://<hostname>:10180`. For example: `https://ca32.lvn.broadcom.net:10180`

You can start you application in a job:

```bash
zowe-api-dev start --job
```

Then check its status:

```bash
zowe-api-dev status
```

And stop it:

```bash
zowe-api-dev stop
```

If you want to completely delete the zFS then you can use this command:

```bash
zowe-api-dev zfs --unmount --delete
```
