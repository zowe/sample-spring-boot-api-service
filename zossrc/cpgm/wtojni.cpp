/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
#include <jni.h>
#include <iostream>
#include <string>
#include <unistd.h>
#include "wtojni.h"
#include "wtoexec.h"

using namespace std;

/**                                                                                                                                         
 * The fields between the pragmas below need to be in ASCII.                                                                                
 *                                                                                                                                          
 * Runtime translations can be done via:                                                                                                    
 *   __etoa() EBCDIC to ASCII                                                                                                               
 *   __atoe() ASCII to EBCDIC                                                                                                               
 */
#if defined(__IBMC__) || defined(__IBMCPP__)
#pragma convert(819)
#endif

const char *MESSAGE_FIELD = "message";
const char *FROM_C = "Message set from JNI";
const char *JNI_FIELD_STRING = "Ljava/lang/String;";

#if defined(__IBMC__) || defined(__IBMCPP__)
#pragma convert(0)
#endif

JNIEXPORT jint JNICALL Java_org_zowe_sample_apiservice_wto_ZosWto_wto(JNIEnv *env, jobject thisObj, jint inId, jstring inContent)
{
    int rc = 0;

    // input values from Java
    const char *content = env->GetStringUTFChars(inContent, NULL);

    char contentCopy[100] = {0};

    // copy, covert, and print input data
    strcpy(contentCopy, content);
    __atoe(contentCopy);
    cout << "[DEBUG] Input - id: " << (int)inId << " content: " << contentCopy << endl;

    // call native z/OS service routine
    rc = WTOEXE((int *)&inId, contentCopy);

    // demonstrate setting a String class variable from JNI (simulating returning a value
    jclass clazz = env->GetObjectClass(thisObj);
    jfieldID dataField = env->GetFieldID(clazz, MESSAGE_FIELD, JNI_FIELD_STRING);
    jstring cData = env->NewStringUTF(FROM_C);
    env->SetObjectField(thisObj, dataField, cData);

    // directly return an integer return type
    return rc;
}