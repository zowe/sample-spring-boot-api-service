# z/OS Deployment Instructions

There are multiple approaches to deploying the sample API service to z/OS; the following
serves as a reference to deploy the sample API as-is.  For a production instance of an API, your deployment process may differ.

**&ast;Note:&ast;** Substitute values below for your site configuration

## Manual Deployment

First, create a space to deploy the API artifacts.

Login to [z/OS Unix Shell](https://www.ibm.com/support/knowledgecenter/zosbasics/com.ibm.zos.zconcepts/zconcepts_146.htm).

**&ast;Note:&ast;** You may need privileged authority to issue the command examples below

1. Allocate and format a [z/OS File System](https://www.ibm.com/support/knowledgecenter/en/SSLTBW_2.3.0/com.ibm.zos.v2r3.bpxb200/zfspref.htm) (zFS):

    - `zfsadm define -aggregate IBMUSER.SAMPLAPI.ZFS -cyls 100 -volumes WRKD23`
      - (response): `IOEZ00248I VSAM linear dataset IBMUSER.SAMPLAPI.ZFS successfully created.`
    - `zfsadm format -aggregate IBMUSER.SAMPLAPI.ZFS`
      - (response): `IOEZ00077I HFS-compatibility aggregate IBMUSER.SAMPLAPI.ZFS has been successfully created`

2. Create directory and mount the file system:

    - `mkdir /u/ibmuser/samplapi`
    - `/usr/sbin/mount -v -f IBMUSER.SAMPLAPI.ZFS /u/ibmuser/samplapi`
      - response: `FOMF0502I Mount complete for IBMUSER.SAMPLAPI.ZFS`

### Deploy Artifacts

Next, deploy artifacts from your workstation to the zFS.

You can upload artifacts via `ftp`, `sftp`, `scp` or [`Zowe CLI`](https://github.com/zowe/zowe-cli). `zowe` commands will be used in the remaining examples.

#### Deploy the Sample Service API Jar

To obtain the sample service jar, run `gradlew build`.  The default artifact will be `build/libs/zowe-apiservice-0.0.1-SNAPSHOT.jar`.

1. Create a directory for the `sample-service.jar`
   - `mkdir /u/ibmuser/samplapi/jars`

2. Upload the `sample-service.jar` as a binary artifact:

    - `zowe files upload ftu "<path_to_local_file>/sample-service.jar" "/u/ibmuser/samplapi/jars/sample-service.jar" --binary`

#### Deploy the Sample Service Configuration YAML

1. Create a directory for the `application.yml`
   - `mkdir /u/ibmuser/samplapi/config`

2. Upload the `config/local/application.yml` as a binary artifact:

`zowe files upload ftu "config/local/application.yml" "/u/ibmuser/samplapi/config/application.yml" --binary`

**&ast;Note:&ast;** If this file is edited on z/OS, it must remain in ASCII format

When the server is started, options will be provided to specify `--spring.config.additional-location` to refer to `config/application.yml`.  Settings in this file will override values found in the same-named `src/main/resources/application.yml`.  For example:

Add `zos` profile, change the port number, and change the paths to keystore and truststore

```yaml
spring.profiles.active: https,diag,zos

server:
    ssl:
        keyStore: config/keystore.p12
        trustStore: config/truststore.p12
    address: 127.0.0.1
    port: 10080
```

**&ast;Note:&ast;** This uses the existing keystore and truststore for localhost without integration to API ML. If you want to integrate to Zowe API ML, you need to follow the instructions in [Generate a keystore and truststore for a new service on z/OS](https://zowe.github.io/docs-site/latest/extend/extend-apiml/api-mediation-security.html#zowe-runtime-on-z-os) and modify the `application.yml`.

#### Deploy `keystore` and `truststore`

`zowe files upload ftu "config/local/keystore.p12" "/u/ibmuser/samplapi/config/keystore.p12" --binary`

`zowe files upload ftu "config/local/truststore.p12" "/u/ibmuser/samplapi/config/truststore.p12" --binary`

### Run

Lastly, you can run the sample server from the z/OS Unix Shell, started task, or batch job.

#### Start via Java Commands

Start the server via:
`java -Xquickstart -jar /u/ibmuser/samplapi/jars/zowe-apiservice-0.0.1-SNAPSHOT.jar --spring.config.additional-location=file:/u/ibmuser/samplapi/config/application.yml`

Here is a snippet of the messages seen after startup on z/OS (using git bash terminal and ssh):

![Java Started Service](images/java-started-service.png)

Stop the server via:
`Ctrl+C`

#### Start via z/OS Batch Job

Customize the JCL below to run the server as a batch job:

```
//SAMPLAPI JOB ACCT#,'SAMPLE API',MSGCLASS=A,CLASS=B,
//             MSGLEVEL=(1,1),REGION=0M
/*JOBPARM SYSAFF=*
//*
//********************************************************************
//* Custom JVM procedure                                             *
//********************************************************************
//JVMPRC86 PROC JAVACLS=,                < Fully Qfied Java class..RQD
//   ARGS=,                              < Args to Java class
//   VERSION='86',                       < JVMLDM version: 86
//   LOGLVL='',                          < Debug LVL: +I(info) +T(trc)
//   REGSIZE='0M',                       < EXECUTION REGION SIZE
//   LEPARM=''
//JAVAJVM  EXEC PGM=JVMLDM&VERSION,REGION=&REGSIZE,
//   PARM='&LEPARM/&LOGLVL &JAVACLS &ARGS'
//SYSPRINT DD SYSOUT=*          < System stdout
//SYSOUT   DD SYSOUT=*          < System stderr
//STDOUT   DD SYSOUT=*          < Java System.out
//STDERR   DD SYSOUT=*          < Java System.err
//CEEDUMP  DD SYSOUT=*
//CEEOPTS DD *
TRAP(ON,NOSPIE)
/*
//ABNLIGNR DD DUMMY
// PEND
//********************************************************************
//* End Custom JVM procedure                                         *
//********************************************************************
//*
//JAVA EXEC PROC=JVMPRC86,
// PARM='+T'
//STDENV DD *
export PWD=/u/ibmuser/samplapi

export JAVA_HOME=/sys/java64bt/v8r0m0/usr/lpp/java/J8.0_64

CLASSPATH=/u/ibmuser/sampleapi/jars/*
export CLASSPATH=$CLASSPATH

LIBPATH=/lib:/usr/lib:$JAVA_HOME/bin
LIBPATH=$LIBPATH:$JAVA_HOME/lib/s390x
LIBPATH=$LIBPATH:$JAVA_HOME/lib/s390x/j9vm
LIBPATH=$LIBPATH:$JAVA_HOME/bin/classic
export LIBPATH=$LIBPATH

IJO="-Xms16m -Xmx128m"
export IBM_JAVA_OPTIONS="${IJO}"

export PATH=$PATH:$JAVA_HOME:$LIBPATH
/*
//MAINARGS DD *
-jar -Xquickstart jars/zowe-apiservice-0.0.1-SNAPSHOT.jar
--spring.config.additional-location=\
file:/u/ibmuser/samplapi/config/application.yml
/*
//STDOUT   DD SYSOUT=*
//STDERR   DD SYSOUT=*
//SYSUDUMP DD SYSOUT=*
```

Stop via: `STOP SAMPLE`.

### Running Example

When the server is started (either through a java command or JCL), you can test the sample `api/v1/greeting` API through your web browser (as you could if they API were running on your workstation).

#### Landing Page

Navigate to the host and port configured in your `config/application.yml`:

![Landing](images/landing-page.png)

#### Sign In

After bypassing the security exception, sign in with `zowe` as the user name and password:

![Sign in](images/sign-in.png)

#### Sample API Response

View the sample API response:

![Greeting API](images/greeting-api.png)
