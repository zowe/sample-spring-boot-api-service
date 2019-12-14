/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons;

import java.text.Normalizer;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Layout;
import lombok.Setter;

public class AccentStrippingPatternLayerEncoder extends PatternLayoutEncoder {
    private static final String STRIP_ACCENTS_PROPERTY_NAME = "org.zowe.commons.logging.stripAccents";

    @Setter
    private boolean stripAccents = "true".equalsIgnoreCase(System.getProperty(STRIP_ACCENTS_PROPERTY_NAME));

    @Override
    public byte[] encode(ILoggingEvent event) {
        String txt = layout.doLayout(event);
        if (stripAccents) {
            String stripped = stripAccents(txt);
            return (stripped != null) ? stripped.getBytes() : null;
        } else {
            return super.encode(event);
        }
    }

    private String stripAccents(String input) {
        return input == null ? null
                : Normalizer.normalize(input, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    void overrideLayout(Layout layout) {
        this.layout = layout;
    }
}
