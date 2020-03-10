/*
 * This program and the accompanying materials are made available and may be used, at your option, under either:
 * * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
 * * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.rest.response;

/**
 * Represents the severity of a problem. Higher severity has higher value.
 */
public enum MessageType {
    ERROR(40, "ERROR", 'E'), WARNING(30, "WARNING", 'W'), INFO(20, "INFO", 'I'), DEBUG(10, "DEBUG", 'D'),
    TRACE(0, "TRACE", 'T');

    private int levelInt;
    private String levelStr;
    private char levelChar;

    MessageType(int i, String s, char c) {
        levelInt = i;
        levelStr = s;
        levelChar = c;
    }

    public int toInt() {
        return levelInt;
    }

    public char toChar() {
        return levelChar;
    }

    @Override
    public String toString() {
        return levelStr;
    }
}
