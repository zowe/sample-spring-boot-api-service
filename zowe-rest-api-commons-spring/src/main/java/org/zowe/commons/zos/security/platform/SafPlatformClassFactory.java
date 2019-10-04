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

public class SafPlatformClassFactory implements PlatformClassFactory {

    @Override
    public Class<?> getPlatformUserClass() throws ClassNotFoundException {
        return Class.forName("com.ibm.os390.security.PlatformUser");
    }

    @Override
    public Class<?> getPlatformReturnedClass() throws ClassNotFoundException {
        return Class.forName("com.ibm.os390.security.PlatformReturned");
    }

    @Override
    public Object getPlatformUser() {
        return null; // the com.ibm.os390.security.PlatformUser has static methods and no instances
    }

    @Override
    public Class<?> getPlatformAccessControlClass() throws ClassNotFoundException {
        return Class.forName("com.ibm.os390.security.PlatformAccessControl");
    }

    @Override
    public Object getPlatformAccessControl() throws ClassNotFoundException {
        return null; // the com.ibm.os390.security.PlatformAccessControl has static methods and no
                     // instances
    }

    @Override
    public PlatformReturned convertPlatformReturned(Object safReturned) throws ClassNotFoundException,
            IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        Class<?> returnedClass = this.getPlatformReturnedClass();
        if (safReturned == null) {
            return null;
        } else {

            return PlatformReturned.builder().success(returnedClass.getField("success").getBoolean(safReturned))
                    .rc(returnedClass.getField("rc").getInt(safReturned))
                    .errno(returnedClass.getField("errno").getInt(safReturned))
                    .errno2(returnedClass.getField("errno2").getInt(safReturned))
                    .errnoMsg((String) returnedClass.getField("errnoMsg").get(safReturned))
                    .stringRet((String) returnedClass.getField("stringRet").get(safReturned))
                    .objectRet(returnedClass.getField("objectRet").get(safReturned)).build();
        }
    }
}
