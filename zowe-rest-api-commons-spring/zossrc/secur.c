#define _OPEN_SYS 1
#include <errno.h>
#include <stdlib.h>
#include <pthread.h>
#include <string.h>
#include <stdio.h>
#include "secur.h"
#include "jnitools.h"

/*
https://www.ibm.com/support/knowledgecenter/en/SSLTBW_2.3.0/com.ibm.zos.v2r3.bpxbd00/ptsec.htm
*/

JNIEXPORT jint JNICALL Java_org_zowe_commons_zos_security_jni_Secur_createSecurityEnvironmentByDaemon(JNIEnv *env, jobject obj, jstring user, jstring applId)
{
    int rc = EINVAL;
    char *platformUser = jstring_to_ebcdic(env, user);
    char *platformApplId = jstring_to_ebcdic(env, applId);
    if (platformUser != NULL)
    {
        int userLength = strlen(platformUser);
        rc = pthread_security_applid_np(__DAEMON_SECURITY_ENV, __USERID_IDENTITY, userLength, platformUser, NULL, 0, platformApplId);
    }
    free_if_not_null(platformUser);
    free_if_not_null(platformApplId);
    return rc;
}

JNIEXPORT jint JNICALL Java_org_zowe_commons_zos_security_jni_Secur_createSecurityEnvironment(JNIEnv *env, jobject obj, jstring user, jstring password, jstring applId)
{
    int rc = EINVAL;
    char *platformUser = jstring_to_ebcdic(env, user);
    char *platformPassword = jstring_to_ebcdic(env, password);
    char *platformApplId = jstring_to_ebcdic(env, applId);
    if (platformUser != NULL)
    {
        int userLength = strlen(platformUser);
        rc = pthread_security_applid_np(__CREATE_SECURITY_ENV, __USERID_IDENTITY, userLength, platformUser, platformPassword, 0, platformApplId);
    }
    free_if_not_null(platformUser);
    free_if_not_null(platformApplId);
    return rc;
}

JNIEXPORT jint JNICALL Java_org_zowe_commons_zos_security_jni_Secur_removeSecurityEnvironment(JNIEnv *env, jobject obj)
{
    return pthread_security_applid_np(__DELETE_SECURITY_ENV, __USERID_IDENTITY, 0, NULL, NULL, 0, NULL);
}
