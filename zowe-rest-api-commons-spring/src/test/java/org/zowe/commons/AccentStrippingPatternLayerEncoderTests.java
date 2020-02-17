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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Layout;

public class AccentStrippingPatternLayerEncoderTests {
    @Test
    public void testWrapRunnableInEnvironmentForAuthenticatedUser() {
        AccentStrippingPatternLayerEncoder encoder = new AccentStrippingPatternLayerEncoder();
        Layout<ILoggingEvent> layout = mock(Layout.class);
        ILoggingEvent event = null;
		when(layout.doLayout(event)).thenReturn("Příliš žluťoučký kůň úpěl ďábelské ódy");
        encoder.overrideLayout(layout);
        encoder.setStripAccents(true);
        byte[] bytes = encoder.encode(event);
        assertEquals("Prilis zlutoucky kun upel dabelske ody", new String(bytes));
        encoder.setStripAccents(false);
        byte[] bytes2 = encoder.encode(event);
        assertEquals("Příliš žluťoučký kůň úpěl ďábelské ódy", new String(bytes2));
    }
}
