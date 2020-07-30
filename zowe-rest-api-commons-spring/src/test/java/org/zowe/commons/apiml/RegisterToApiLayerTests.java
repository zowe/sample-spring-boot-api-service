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

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.test.util.ReflectionTestUtils;
import org.zowe.apiml.eurekaservice.client.ApiMediationClient;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RegisterToApiLayerTests {

    private static final String IP_ADDRESS = "123.15.0.1";

    @Mock
    private ApiMediationServiceConfigBean config;

    @Mock
    private SslConfigBean ssl;

    @Mock
    private ApiMediationClient apiMediationClient;

    private RegisterToApiLayer getMock(boolean enabled) {
        RegisterToApiLayer registerToApiLayer = mock(RegisterToApiLayer.class);
        ReflectionTestUtils.setField(registerToApiLayer, "ipAddress", IP_ADDRESS);
        ReflectionTestUtils.setField(registerToApiLayer, "enabled", enabled);
        ReflectionTestUtils.setField(registerToApiLayer, "config", config);
        ReflectionTestUtils.setField(registerToApiLayer, "ssl", ssl);
        ReflectionTestUtils.setField(registerToApiLayer, "apiMediationClient", apiMediationClient);
        return registerToApiLayer;
    }

    @Test
    public void givenContextRefreshedEvent_whenApimlEnabled_thenRegister() {
        RegisterToApiLayer registerToApiLayer = getMock(true);

        doCallRealMethod().when(registerToApiLayer).onApplicationEvent(any());
        registerToApiLayer.onApplicationEvent(mock(ContextRefreshedEvent.class));

        verify(registerToApiLayer, times(1)).register(same(config), same(ssl));
    }

    @Test
    public void givenContextRefreshedEvent_whenApimlDisabled_thenDoNothing() {
        RegisterToApiLayer registerToApiLayer = getMock(false);

        doCallRealMethod().when(registerToApiLayer).onApplicationEvent(any());
        registerToApiLayer.onApplicationEvent(mock(ContextRefreshedEvent.class));

        verify(registerToApiLayer, never()).register(same(config), same(ssl));
    }

    @Test
    public void givenAvailableApiml_whenRegister_thenSuccess() {
        RegisterToApiLayer registerToApiLayer = getMock(true);
        doCallRealMethod().when(registerToApiLayer).register(any(), any());
        when(registerToApiLayer.createApiMediationClient()).thenReturn(mock(ApiMediationClient.class));

        registerToApiLayer.register(config, ssl);

        verify(config, times(1)).setSsl(same(ssl));
        verify(config, times(1)).setServiceIpAddress(IP_ADDRESS);

        ApiMediationClient amc2 = (ApiMediationClient) ReflectionTestUtils.getField(registerToApiLayer, "apiMediationClient");
        assertNotSame(apiMediationClient, amc2);
    }

    @Test
    public void givenApiMediationClient_whenGetApiGatewayBaseUrl_thenReturnHomepageUrl() {
        RegisterToApiLayer registerToApiLayer = getMock(true);
        doCallRealMethod().when(registerToApiLayer).getApiGatewayBaseUrl();

        String homepage = "homePage";

        InstanceInfo instanceInfo = mock(InstanceInfo.class);
        doReturn(homepage).when(instanceInfo).getHomePageUrl();
        Application application = mock(Application.class);
        EurekaClient eurekaClient = mock(EurekaClient.class);
        doReturn(eurekaClient).when(apiMediationClient).getEurekaClient();
        doReturn(application).when(eurekaClient).getApplication("gateway");
        doReturn(Collections.singletonList(instanceInfo)).when(application).getInstances();

        assertSame(homepage, registerToApiLayer.getApiGatewayBaseUrl());
        assertSame(homepage, registerToApiLayer.getApiGatewayBaseUrl());

        verify(apiMediationClient, times(1)).getEurekaClient();
        verify(instanceInfo, times(1)).getHomePageUrl();
    }

    @Test
    public void givenApiMediationClientNoGwInstance_whenGetApiGatewayBaseUrl_thenReturnNull() {
        RegisterToApiLayer registerToApiLayer = getMock(true);
        doCallRealMethod().when(registerToApiLayer).getApiGatewayBaseUrl();

        Application application = mock(Application.class);
        EurekaClient eurekaClient = mock(EurekaClient.class);
        doReturn(eurekaClient).when(apiMediationClient).getEurekaClient();
        doReturn(application).when(eurekaClient).getApplication("gateway");
        doReturn(Collections.emptyList()).when(application).getInstances();

        assertNull(registerToApiLayer.getApiGatewayBaseUrl());
    }

    @Test
    public void givenApiMediationClientNoGwApplication_whenGetApiGatewayBaseUrl_thenReturnNull() {
        RegisterToApiLayer registerToApiLayer = getMock(true);
        doCallRealMethod().when(registerToApiLayer).getApiGatewayBaseUrl();

        EurekaClient eurekaClient = mock(EurekaClient.class);
        doReturn(eurekaClient).when(apiMediationClient).getEurekaClient();
        //doReturn(null).when(eurekaClient).getApplication("gateway");

        assertNull(registerToApiLayer.getApiGatewayBaseUrl());
    }

}
