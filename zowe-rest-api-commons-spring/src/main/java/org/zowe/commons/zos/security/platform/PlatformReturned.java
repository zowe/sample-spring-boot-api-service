/*
 * This program and the accompanying materials are made available and may be used, at your option, under either:
 * * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
 * * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.zos.security.platform;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

/**
 * More details about the z/OS security call results.
 * <p>
 * See also:
 * https://www.ibm.com/support/knowledgecenter/en/SSYKE2_8.0.0/com.ibm.java.zsecurity.api.80.doc/com.ibm.os390.security/com/ibm/os390/security/PlatformReturned.html
 */
@Data
@Builder
public class PlatformReturned implements Serializable {

    private static final long serialVersionUID = -2699057722238941755L;

    private boolean success;
    private int rc;
    private int errno;
    private int errno2;
    private String errnoMsg;
    private String stringRet;
    private transient Object objectRet;

    private void readObject(ObjectInputStream ois) throws IOException {
        success = ois.readBoolean();
        rc = ois.readInt();
        errno = ois.readInt();
        errno2 = ois.readInt();
        errnoMsg = ois.readUTF();
        stringRet = ois.readUTF();
        objectRet = this;
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.writeBoolean(success);
        oos.writeInt(rc);
        oos.writeInt(errno);
        oos.writeInt(errno2);
        oos.writeUTF(errnoMsg);
        oos.writeUTF(stringRet);
    }

}
