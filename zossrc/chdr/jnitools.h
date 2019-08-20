#include <jni.h>

char *jstring_to_ebcdic(JNIEnv *env, jstring jstr);

void free_if_not_null(void *ptr);
