//{{{jobcard.name}}} JOB {{{jobcard.account}}},
//             '{{{jobcard.description}}}',
//             MSGCLASS={{{jobcard.messageClass}}},
//             CLASS={{{jobcard.jobClass}}},
//             MSGLEVEL=(1,1),
//             REGION=0M
/*JOBPARM SYSAFF=*
//*
//********************************************************************
//* Custom JVM procedure                                             *
//********************************************************************
//JVMPRC86 PROC JAVACLS=,                < Fully Qfied Java class..RQD
//   ARGS=,                              < Args to Java class
//   VERSION='86',                       < JVMLDM version: 86
//   LOGLVL='',                          < Debug LVL: +I(info) +T(trc)
//   REGSIZE='0M',                       < Execution region size
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
export PWD={{{deployment.rootDir}}}

export JAVA_HOME=/sys/java64bt/v8r0m0/usr/lpp/java/J8.0_64

CLASSPATH={{{deployment.rootDir}}}/jars/*
export CLASSPATH=$CLASSPATH

LIBPATH=/lib:/usr/lib:$JAVA_HOME/bin
LIBPATH=$LIBPATH:$JAVA_HOME/lib/s390x
LIBPATH=$LIBPATH:$JAVA_HOME/lib/s390x/j9vm
LIBPATH=$LIBPATH:$JAVA_HOME/bin/classic
LIBPATH=$LIBPATH:{{{deployment.rootDir}}}/lib
export LIBPATH=$LIBPATH

IJO="-Xms16m -Xmx128m"
export IBM_JAVA_OPTIONS="${IJO}"

export PATH=$PATH:$JAVA_HOME:$LIBPATH:$CLASSPATH

echo $PATH
/*
//MAINARGS DD *
-jar jars/zowe-rest-api-sample-spring-0.0.1-SNAPSHOT.jar
--spring.config.additional-location=\
file:{{{deployment.rootDir}}}/config/local/application.yml
/*
//STDOUT   DD SYSOUT=*
//STDERR   DD SYSOUT=*
//SYSUDUMP DD SYSOUT=*
