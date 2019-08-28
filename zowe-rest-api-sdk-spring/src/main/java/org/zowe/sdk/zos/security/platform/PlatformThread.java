/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.sdk.zos.security.platform;

/**
 * Interface to z/OS thread-level information.
 *
 * It is inspired by
 * https://www.ibm.com/support/knowledgecenter/SSYKE2_8.0.0/com.ibm.java.zsecurity.api.80.doc/com.ibm.os390.security/com/ibm/os390/security/PlatformThread.html
 * class. But it provides interface instead of class with static methods so
 * non-z/OS implementation of this inteface can be created.
 */
public interface PlatformThread {
    /**
     * @return User ID associated with the current thread.
     */
    String getUserName();
}
