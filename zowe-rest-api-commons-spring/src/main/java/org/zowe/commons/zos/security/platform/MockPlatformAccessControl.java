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

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.List;

import org.yaml.snakeyaml.Yaml;

public class MockPlatformAccessControl implements PlatformAccessControl {
    public static final String VALID_USERID = "ZOWE";
    public static final String FAILING_LEVEL = "FAILURE";
    public static final String NONE = "NONE";

    private Map<String, String> safAccess = new HashMap<>();
    private Set<String> validUserid = new HashSet<>();
    private Set<String> definedResource = new HashSet<>();

    public MockPlatformAccessControl() {
        this("test-saf.yml");
    }

    public MockPlatformAccessControl(String resourceName) {
        loadSafAccess(resourceName);
    }

    private AccessLevel toAccessLevel(String levelName) {
        try {
            return AccessLevel.valueOf(levelName);
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void loadSafAccess(String resourcePath) {
        Yaml yaml = new Yaml();
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(resourcePath);
        Map<String, Object> data = yaml.load(inputStream);
        Map<String, Map<String, Map<String, List<String>>>> classes = (Map<String, Map<String, Map<String, List<String>>>>) data
                .get("safAccess");
        for (Entry<String, Map<String, Map<String, List<String>>>> clazz : classes.entrySet()) {
            String resourceClass = clazz.getKey();
            for (Entry<String, Map<String, List<String>>> resource : clazz.getValue().entrySet()) {
                String resourceName = resource.getKey();
                for (Entry<String, List<String>> level : resource.getValue().entrySet()) {
                    String levelName = level.getKey().toUpperCase();
                    List<String> users = level.getValue();
                    defineAccessToResourceForUsers(resourceClass, resourceName, levelName, users);
                }
            }
        }
    }

    private void defineAccessToResourceForUsers(String resourceClass, String resourceName, String levelName, List<String> users) {
        if (!levelName.equalsIgnoreCase(FAILING_LEVEL) && !levelName.equalsIgnoreCase(NONE)
                && (toAccessLevel(levelName) == null)) {
            throw new IllegalArgumentException("Invalid level: " + levelName);
        }

        definedResource.add(resourceKey(resourceClass, resourceName));

        if (!levelName.equalsIgnoreCase(NONE)) {
            for (String userid : users) {
                validUserid.add((userid.toUpperCase()));
                safAccess.put(safAccessKey(userid, resourceClass, resourceName), levelName);
            }
        }
    }

    private static String resourceKey(String resourceClass, String resourceName) {
        return (resourceClass + "-" + resourceName).toUpperCase();
    }

    private static String safAccessKey(String userid, String resourceClass, String resourceName) {
        return (userid + "-" + resourceClass + "-" + resourceName).toUpperCase();
    }

    @Override
    public PlatformReturned checkPermission(String userid, String resourceClass, String resourceName, int accessLevel) {
        PlatformReturned.PlatformReturnedBuilder builder = PlatformReturned.builder().success(false);

        if (!validUserid.contains(userid.toUpperCase())) {
            return builder.errno(PlatformAckErrno.ESRCH.errno).errno2(PlatformErrno2.JRSAFNoUser.errno2).build();
        }

        if (!definedResource.contains(resourceKey(resourceClass, resourceName))) {
            return builder.errno(PlatformAckErrno.ESRCH.errno).errno2(PlatformErrno2.JRSAFResourceUndefined.errno2)
                    .build();
        }

        String savedLevel = safAccess.get(safAccessKey(userid, resourceClass, resourceName));

        if (savedLevel == null) {
            return builder.errno(PlatformAckErrno.EPERM.errno).errno2(PlatformErrno2.JRNoResourceAccess.errno2).build();
        }

        if (savedLevel.equalsIgnoreCase(FAILING_LEVEL)) {
            return builder.errno(PlatformAckErrno.EPERM.errno).errno2(PlatformErrno2.JREnvDirty.errno2).build();
        }

        AccessLevel savedAccessLevel = AccessLevel.valueOf(savedLevel);
        if (savedAccessLevel.getValue() < accessLevel) {
            return builder.errno(PlatformAckErrno.EPERM.errno).errno2(PlatformErrno2.JRNoResourceAccess.errno2).build();
        }

        return null;
    }

    @Override
    public PlatformReturned checkPermission(String resourceClass, String resourceName, int accessLevel) {
        return checkPermission(VALID_USERID, resourceClass, resourceName, accessLevel);
    }
}
