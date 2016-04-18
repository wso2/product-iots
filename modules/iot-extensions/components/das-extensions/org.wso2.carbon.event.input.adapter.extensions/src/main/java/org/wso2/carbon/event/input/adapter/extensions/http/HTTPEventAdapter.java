/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.wso2.carbon.event.input.adapter.extensions.http;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapter;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterConfiguration;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterListener;
import org.wso2.carbon.event.input.adapter.core.exception.InputEventAdapterException;
import org.wso2.carbon.event.input.adapter.core.exception.InputEventAdapterRuntimeException;
import org.wso2.carbon.event.input.adapter.core.exception.TestConnectionNotSupportedException;
import org.wso2.carbon.event.input.adapter.extensions.http.util.HTTPEventAdapterConstants;
import org.wso2.carbon.event.input.adapter.extensions.internal.EventAdapterServiceDataHolder;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.servlet.ServletException;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class HTTPEventAdapter implements InputEventAdapter {

    private final InputEventAdapterConfiguration eventAdapterConfiguration;
    private final Map<String, String> globalProperties;
    private InputEventAdapterListener eventAdaptorListener;
    private final String id = UUID.randomUUID().toString();
    public static ExecutorService executorService;
    private static final Log log = LogFactory.getLog(HTTPEventAdapter.class);
    private boolean isConnected = false;

    public HTTPEventAdapter(InputEventAdapterConfiguration eventAdapterConfiguration,
            Map<String, String> globalProperties) {
        this.eventAdapterConfiguration = eventAdapterConfiguration;
        this.globalProperties = globalProperties;
    }

    @Override
    public void init(InputEventAdapterListener eventAdaptorListener) throws InputEventAdapterException {
        this.eventAdaptorListener = eventAdaptorListener;

        //ThreadPoolExecutor will be assigned  if it is null
        if (executorService == null) {
            int minThread;
            int maxThread;
            long defaultKeepAliveTime;
            int jobQueueSize;

            //If global properties are available those will be assigned else constant values will be assigned
            if (globalProperties.get(HTTPEventAdapterConstants.ADAPTER_MIN_THREAD_POOL_SIZE_NAME) != null) {
                minThread = Integer
                        .parseInt(globalProperties.get(HTTPEventAdapterConstants.ADAPTER_MIN_THREAD_POOL_SIZE_NAME));
            } else {
                minThread = HTTPEventAdapterConstants.ADAPTER_MIN_THREAD_POOL_SIZE;
            }

            if (globalProperties.get(HTTPEventAdapterConstants.ADAPTER_MAX_THREAD_POOL_SIZE_NAME) != null) {
                maxThread = Integer
                        .parseInt(globalProperties.get(HTTPEventAdapterConstants.ADAPTER_MAX_THREAD_POOL_SIZE_NAME));
            } else {
                maxThread = HTTPEventAdapterConstants.ADAPTER_MAX_THREAD_POOL_SIZE;
            }

            if (globalProperties.get(HTTPEventAdapterConstants.ADAPTER_KEEP_ALIVE_TIME_NAME) != null) {
                defaultKeepAliveTime = Integer
                        .parseInt(globalProperties.get(HTTPEventAdapterConstants.ADAPTER_KEEP_ALIVE_TIME_NAME));
            } else {
                defaultKeepAliveTime = HTTPEventAdapterConstants.DEFAULT_KEEP_ALIVE_TIME_IN_MILLS;
            }

            if (globalProperties.get(HTTPEventAdapterConstants.ADAPTER_EXECUTOR_JOB_QUEUE_SIZE_NAME) != null) {
                jobQueueSize = Integer
                        .parseInt(globalProperties.get(HTTPEventAdapterConstants.ADAPTER_EXECUTOR_JOB_QUEUE_SIZE_NAME));
            } else {
                jobQueueSize = HTTPEventAdapterConstants.ADAPTER_EXECUTOR_JOB_QUEUE_SIZE;
            }

            RejectedExecutionHandler rejectedExecutionHandler = new RejectedExecutionHandler() {
                @Override
                public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                    try {
                        executor.getQueue().put(r);
                    } catch (InterruptedException e) {
                        log.error("Exception while adding event to executor queue : " + e.getMessage(), e);
                    }
                }

            };

            executorService = new ThreadPoolExecutor(minThread, maxThread, defaultKeepAliveTime, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(jobQueueSize), rejectedExecutionHandler);

        }
    }

    @Override
    public void testConnect() throws TestConnectionNotSupportedException {
        throw new TestConnectionNotSupportedException("not-supported");
    }

    @Override
    public void connect() {
        registerDynamicEndpoint(eventAdapterConfiguration.getName());
        isConnected = true;
    }

    @Override
    public void disconnect() {
        if (isConnected){
            isConnected = false;
            unregisterDynamicEndpoint(eventAdapterConfiguration.getName());
        }
    }

    @Override
    public void destroy() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof HTTPEventAdapter))
            return false;

        HTTPEventAdapter that = (HTTPEventAdapter) o;

        return id.equals(that.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean isEventDuplicatedInCluster() {
        return false;
    }

    @Override
    public boolean isPolling() {
        return false;
    }

    private void registerDynamicEndpoint(String adapterName) {

        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        String endpoint;
        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            endpoint = HTTPEventAdapterConstants.ENDPOINT_PREFIX + adapterName;
        } else {
            endpoint = HTTPEventAdapterConstants.ENDPOINT_PREFIX + HTTPEventAdapterConstants.ENDPOINT_TENANT_KEY
                    + HTTPEventAdapterConstants.ENDPOINT_URL_SEPARATOR + tenantDomain
                    + HTTPEventAdapterConstants.ENDPOINT_URL_SEPARATOR + adapterName;
        }

        try {
            HttpService httpService = EventAdapterServiceDataHolder.getHTTPService();
            if (httpService == null) {
                throw new InputEventAdapterRuntimeException(
                        "HttpService not available, Error in registering endpoint " + endpoint);
            }
            httpService.registerServlet(endpoint, new HTTPMessageServlet(eventAdaptorListener, tenantId,
                    eventAdapterConfiguration),
                    new Hashtable(), httpService.createDefaultHttpContext());
        } catch (ServletException | NamespaceException e) {
            throw new InputEventAdapterRuntimeException("Error in registering endpoint " + endpoint, e);
        }

    }

    private void unregisterDynamicEndpoint(String adapterName) {
        HttpService httpService = EventAdapterServiceDataHolder.getHTTPService();
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String endpoint;
        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            endpoint = HTTPEventAdapterConstants.ENDPOINT_PREFIX + adapterName;
        } else {
            endpoint = HTTPEventAdapterConstants.ENDPOINT_PREFIX + HTTPEventAdapterConstants.ENDPOINT_TENANT_KEY
                    + HTTPEventAdapterConstants.ENDPOINT_URL_SEPARATOR + tenantDomain
                    + HTTPEventAdapterConstants.ENDPOINT_URL_SEPARATOR + adapterName;
        }
        if (httpService != null) {
            httpService.unregister(endpoint);
        }

    }
}