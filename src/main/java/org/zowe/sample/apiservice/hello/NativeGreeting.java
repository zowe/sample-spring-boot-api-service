/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.sample.apiservice.hello;

public class NativeGreeting {
    private static String JNI_SHARED_LIBRARY = "wtojni";

    static {
        System.loadLibrary(NativeGreeting.JNI_SHARED_LIBRARY);
    }

    private native int wto(int id, String content);

    private final int nativeCode; // integer value returned from native code
    private String nativeMessage; // String class variable set in native code

    private final int id;
    private final String content;

    public NativeGreeting(int id, String content) {
        this.id = id;
        this.content = content;
        this.nativeCode = wto(id, content);
    }

    public int getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public int getNativeCode() {
        return nativeCode;
    }

    public String getNativeMessage() {
        return nativeMessage;
    }
}