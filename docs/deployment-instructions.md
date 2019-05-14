# z/OS Deployment Instructions

There are multiple approaches to deploying the sample API service to z/OS; the following
serves as a reference.

## Manual Deployment

Login to [z/OS Unix Shell](https://www.ibm.com/support/knowledgecenter/zosbasics/com.ibm.zos.zconcepts/zconcepts_146.htm).

**Note:** You may need privileged authority to issue the commands below.  The following were
run as `superuser`.

**Note:** substitute values for your site configuration (e.g. your mainframe user ID for IBMUSER, valid volume names, and JCL standards)

1. Allocate and format [z/OS File System](https://www.ibm.com/support/knowledgecenter/en/SSLTBW_2.3.0/com.ibm.zos.v2r3.bpxb200/zfspref.htm) (zFS), example:

    - `zfsadm define -aggregate IBMUSER.SAMPLAPI.ZFS -cyls 100 -volumes WRKD23`
      - response: `IOEZ00248I VSAM linear dataset IBMUSER.SAMPLAPI.ZFS successfully created.`
    - `zfsadm format -aggregate IBMUSER.SAMPLAPI.ZFS`
      - response: `IOEZ00077I HFS-compatibility aggregate IBMUSER.SAMPLAPI.ZFS has been successfully created`

1. Create directory and mount the file system, example:

    - `mkdir /u/users/samplapi`
    - `/usr/sbin/mount -v -f IBMUSER.SAMPLAPI.ZFS /u/users/samplapi`
      - response: `FOMF0502I Mount complete for IBMUSER.SAMPLAPI.ZFS`

### Deploy Artifacts

You can upload artifacts via something like `ftp`, `sftp`, `scp` or [`Zowe CLI`](https://github.com/zowe/zowe-cli).  For this document, `zowe` commands will be used.

#### Deploy the Sample Service API Jar

To obtain the sample service jar, run `gradlew build`.  The default artifact will be `build/libs/zowe-apiservice-0.0.1-SNAPSHOT.jar`.

1. Create a directory for the `sample-service.jar`
   - `mkdir /u/users/samplapi/jars`

2. Upload the `sample-service.jar` as a binary artifact:

    - `zowe files upload ftu "<path_to_local_file>/sample-service.jar" "/u/users/samplapi/jars/sample-service.jar" --binary`

#### Deploy the Sample Service Configuration YAML

1. Create a directory for the `application.yml`
   - `mkdir /u/users/samplapi/config`

2. Upload the `config/local/application.yml` as a binary artifact:

`zowe files upload ftu "config/local/application.yml" "/u/users/samplapi/config/local/application.yml" --binary`

**Note:** if this file is edited on z/OS, it must remain in ASCII format.

When the server is started, options will be provided to specify `--spring.config.additional-location` to refer to `config/local/application.yml`.  Settings in this file will override values found in the same-named `src/main/resources/application.yml`.  For example:

```yaml
server:
    address: 127.0.0.1
    port: 10080
```
### Run

You can run the sample server from the z/OS Unix Shell, started task, or batch job.

#### Start via Java Commands

Start the server via:
`java -jar /u/users/samplapi/jars/zowe-apiservice-0.0.1-SNAPSHOT.jar --spring.config.additional-location=file:/u/users/samplapi/config/local/application.yml`

Stop the server via:
`Ctrl+C`

#### Start via z/OS Batch Job

```
{{#jclUseDeployment}}
//{{jcl.srv.jobname}} JOB {{deploy.tso.account}},
{{/jclUseDeployment}}
{{^jclUseDeployment}}
//{{jcl.srv.jobname}} JOB {{build.tso.account}},
{{/jclUseDeployment}}
//             'Sample API',
//             MSGCLASS=A,CLASS={{jcl.srv.jobclass}},
//             MSGLEVEL=(1,1),REGION=0M
/*JOBPARM SYSAFF=*
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
//JAVA EXEC PROC=JVMPRC86,
// PARM='+T'
//SYSPROC DD DISP=SHR,DSN=USER.PROCLIB
//STDENV DD *
export JAVA_HOME={{java.home}}
{{#jclUseDeployment}}
export PWD={{deploy.uss.workDir}}
{{/jclUseDeployment}}
{{^jclUseDeployment}}
export PWD={{build.uss.workDir}}
{{/jclUseDeployment}}
CLASSPATH={{jclJarLib}}/*
export CLASSPATH=$CLASSPATH

LIBPATH=/lib:/usr/lib:$JAVA_HOME/bin
LIBPATH=$LIBPATH:$JAVA_HOME/lib/s390x
LIBPATH=$LIBPATH:$JAVA_HOME/lib/s390x/j9vm
LIBPATH=$LIBPATH:$JAVA_HOME/bin/classic
LIBPATH=$LIBPATH:{{jclDeployUssLib}}
export LIBPATH=$LIBPATH

IJO="{{java.ijo}}"
export IBM_JAVA_OPTIONS="${IJO}"

export PATH=$PATH:$JAVA_HOME:$LIBPATH

/*
//MAINARGS DD *
-jar /u/users/samplapi/jars/zowe-apiservice-0.0.1-SNAPSHOT.jar
--spring.config.additional-location=\
file:/u/users/samplapi/config/local/application.yml
/*
//STDOUT   DD SYSOUT=*
//STDERR   DD SYSOUT=*
//SYSUDUMP DD SYSOUT=*
```

Stop via: `STOP SAMPLE`.
