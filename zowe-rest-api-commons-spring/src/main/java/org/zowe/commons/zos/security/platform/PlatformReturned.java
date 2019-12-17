/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.zos.security.platform;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

@Data @Builder
/**
 * More details about the z/OS security call results.
 *
 * See also:
 * https://www.ibm.com/support/knowledgecenter/en/SSYKE2_8.0.0/com.ibm.java.zsecurity.api.80.doc/com.ibm.os390.security/com/ibm/os390/security/PlatformReturned.html
 */
public class PlatformReturned implements Serializable {
    private static final long serialVersionUID = -2699057722238941755L;

    private boolean success;
    private int rc;
    private int errno;
    private int errno2;
    private String errnoMsg;
    private String stringRet;
    private transient Object objectRet;
}
