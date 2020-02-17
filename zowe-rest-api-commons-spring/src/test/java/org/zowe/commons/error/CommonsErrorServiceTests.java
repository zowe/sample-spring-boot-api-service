/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
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
