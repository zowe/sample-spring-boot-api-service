#define _OPEN_SYS 1
#include <stdlib.h>
#include <jni.h>
#include "jnitools.h"

char *jstring_to_ebcdic(JNIEnv *env, jstring jstr)
{
    if (jstr == NULL)
    {
        return NULL;
    }

    int len;
    jint rc = GetStringPlatformLength(env, jstr, &len, "IBM-1047");
    if (rc != 0)
    {
        return NULL;
    }

    char *str = __malloc31(len);
    rc = GetStringPlatform(env, jstr, str, len, "IBM-1047");
    if (rc != 0)
    {
        free(str);
        return NULL;
    }

    return str;
}

void free_if_not_null(void *ptr)
{
    if (ptr != NULL)
    {
        free(ptr);
    }
}
