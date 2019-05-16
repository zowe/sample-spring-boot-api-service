/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.sdk.spring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

public class SpringContextTests {
    SpringContext context = new SpringContext();

    @Test
    public void getApplicationContextReturnsSetContext() {
        try {
            ApplicationContext newContext = new GenericApplicationContext();
            context.setApplicationContext(newContext);
            assertEquals(newContext, SpringContext.getApplicationContext());
        } finally {
            context.setApplicationContext(null);
        }
    }

    @Test(expected = NoSuchBeanDefinitionException.class)
    public void getBeanFailsForNonExistentBean() {
        try {
            GenericApplicationContext newContext = new GenericApplicationContext();
            newContext.refresh();
            context.setApplicationContext(newContext);
            SpringContext.getBean(SpringContext.class);
        } finally {
            context.setApplicationContext(null);
        }
    }

}
