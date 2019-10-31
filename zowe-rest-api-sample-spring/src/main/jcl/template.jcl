{{#each user.jobcard}}
{{{this}}}
{{/each}}
//********************************************************************
//* JVM procedure
//********************************************************************
//JVMPRC86 PROC JAVACLS=,                < Fully Qfied Java class..RQD
//   ARGS=,                              < Args to Java class
//   VERSION='86',                       < JVMLDM version: 8.0 64-bit
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
//JAVA EXEC PROC=JVMPRC86,PARM='+T'
{{#if user.javaLoadlib}}
//STEPLIB  DD DSN={{{user.javaLoadlib}}},DISP=SHR
{{/if}}
//MAINARGS DD *
-jar bin/zowe-rest-api-sample-spring.jar
--spring.config.additional-location=file:etc/application.yml
/*
//STDOUT   DD SYSOUT=*
//STDERR   DD SYSOUT=*
//SYSUDUMP DD SYSOUT=*
//STDENV DD *
export PWD={{{user.zosTargetDir}}}
cd $PWD

export JAVA_HOME={{{user.javaHome}}}

CLASSPATH="${JAVA_HOME}/lib/tools.jar"
CLASSPATH="${CLASSPATH}":/usr/include/java_classes/IRRRacf.jar
export CLASSPATH=${CLASSPATH}
echo CLASSPATH=${CLASSPATH}

LIBPATH=/lib:/usr/lib:"${JAVA_HOME}"/bin
LIBPATH="$LIBPATH":"${JAVA_HOME}"/bin/classic
LIBPATH="$LIBPATH":"${JAVA_HOME}"/bin/j9vm
LIBPATH="$LIBPATH":"${JAVA_HOME}"/lib/s390/classic
LIBPATH="$LIBPATH":"${JAVA_HOME}"/lib/s390/default
LIBPATH="$LIBPATH":"${JAVA_HOME}"/lib/s390/j9vm
LIBPATH="$LIBPATH":"${PWD}/lib"
export LIBPATH=$LIBPATH

IJO="-Xms16m -Xmx128m"
IJO="$IJO -Duser.language=en -Duser.country=US"
IJO="$IJO -Dibm.serversocket.recover=true"
IJO="$IJO -Dfile.encoding=UTF-8"
IJO="$IJO -Djava.io.tmpdir=/tmp"
{{#if debugPort}}
IJO="$IJO -Xdebug"
_DEBUG_OPTIONS="suspend=n,server=y,address={{debugPort}}"
IJO="$IJO -Xrunjdwp:transport=dt_socket,$_DEBUG_OPTIONS"
{{/if}}
#IJO="$IJO -Xhealthcenter:port=39083,transport=jrmp"
IJO="$IJO -Xquickstart"

export IBM_JAVA_OPTIONS="${IJO}"
export PATH=$JAVA_HOME/bin:$PATH

# Timezone
TIMEZONE=$(grep `echo "\0137TZ=*"` /etc/profile)
# If you want to override timezone defined in /etc/profile
# uncomment following line and replace 'GMT'
# with the valid identifier of the time zone you wish to use
#TIMEZONE="TZ=GMT"
echo --------- extracted timezone from /etc/profile
echo TIMEZONE=$TIMEZONE
echo ---------
export ${TIMEZONE%%#*}
#export TZ=Etc/GMT-9

export JAVA_DUMP_HEAP=false
export JAVA_PROPAGATE=NO
export IBM_JAVA_ZOS_TDUMP=NO

echo "Working directory: `pwd`"
/*
