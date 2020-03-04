/*
 * This program and the accompanying materials are made available and may be used, at your option, under either:
 * * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
 * * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
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
