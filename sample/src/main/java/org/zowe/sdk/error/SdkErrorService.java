/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.sdk.error;

import com.ca.mfaas.error.ErrorService;
import com.ca.mfaas.error.impl.ErrorServiceImpl;
import com.ca.mfaas.rest.response.ApiMessage;

public final class SdkErrorService {
    private static ErrorService errorService = new ErrorServiceImpl("/sdk-messages.yml");

    private SdkErrorService() {}

    public static ErrorService get() {
        return errorService;
    }

    public static String getReadableMessage(String key, Object... parameters) {
        return SdkErrorService.get().createApiMessage(key, parameters).getMessages().get(0).toReadableText();
    }

    public static String getReadableMessage(ApiMessage apiMessage) {
        return apiMessage.getMessages().get(0).toReadableText();
    }
}
