/*
 * This program and the accompanying materials are made available and may be used, at your option, under either:
 * * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
 * * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.commons.spring;

import static org.junit.Assert.assertEquals;

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
