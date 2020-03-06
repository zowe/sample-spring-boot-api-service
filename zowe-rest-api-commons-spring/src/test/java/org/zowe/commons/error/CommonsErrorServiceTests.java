/*
 * This program and the accompanying materials are made available and may be used, at your option, under either:
 * * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
 * * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.error;

import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.junit.Test;

public class CommonsErrorServiceTests {
    @Test
    public void returnsReadableMessage() {
        assertTrue(CommonsErrorService.get()
                .getReadableMessage("org.zowe.commons.apiml.serviceCertificateNotTrusted", "param").contains("param"));
    }

    @Test
    public void returnsLocalizedMessage() {
        assertTrue(CommonsErrorService.get()
                .createApiMessage(Locale.forLanguageTag("cs"), "org.zowe.commons.rest.notFound").getMessages().get(0)
                .getMessageContent().contains("Služba"));
    }

}
