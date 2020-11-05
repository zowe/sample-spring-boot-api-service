/*
 * This program and the accompanying materials are made available and may be used, at your option, under either:
 * * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
 * * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.apiml;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Marker;
import org.zowe.commons.error.CommonsErrorService;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;

@Slf4j
public class ApimlIntegrationFailureDetector extends TurboFilter {

    @Override
    public FilterReply decide(Marker marker, Logger logger, Level level, String format, Object[] params, Throwable t) {
        if (reportFatalError(level, t)) {
            return FilterReply.DENY;
        }

        if ((logger != null) && logger.getName().contains("com.netflix")
                && (logger.getName().contains("DiscoveryClient")
                || logger.getName().contains("RedirectingEurekaHttpClient"))) {
            if (logger.getLevel() == Level.ERROR) {
                String message = ExceptionUtils.getMessage(t);
                if (!message.isEmpty()) {
                    log.error(CommonsErrorService.get().getReadableMessage("org.zowe.commons.apiml.unableToRegister",
                            message));
                    logOriginalError(t);
                }
            }
            return FilterReply.DENY;
        }

        return FilterReply.NEUTRAL;
    }

    private boolean isErrorFromDiscoveryClient(String stackFrame) {
        return ((stackFrame.contains(".ApiMediationClient"))
                || (stackFrame.contains("com.netflix.discovery.DiscoveryClient")));
    }

    private boolean isErrorFromDiscoveryClient(Throwable throwable) {
        for (String s : ExceptionUtils.getStackFrames(throwable)) {
            if (isErrorFromDiscoveryClient(s)) {
                return true;
            }
        }
        return false;
    }

    boolean reportFatalError(Level level, Throwable t) {
        if (!level.isGreaterOrEqual(Level.ERROR) || !isErrorFromDiscoveryClient(t)) {
            return false;
        }

        String errorMessageKey;
        if (ExceptionUtils.indexOfType(t, SSLPeerUnverifiedException.class) >= 0) {
            errorMessageKey = "org.zowe.commons.apiml.apimlCertificateNotTrusted";
        } else if (ExceptionUtils.indexOfType(t, SSLHandshakeException.class) >= 0) {
            errorMessageKey = "org.zowe.commons.apiml.serviceCertificateNotTrusted";
        } else {
            return false;
        }

        log.error(CommonsErrorService.get().getReadableMessage(errorMessageKey, t.getMessage()));
        logOriginalError(t);

        return true;
    }

    private void logOriginalError(Throwable t) {
        log.debug("Original error: {}: {}", t.getClass().getName(), t.getMessage(), t);
    }

}
