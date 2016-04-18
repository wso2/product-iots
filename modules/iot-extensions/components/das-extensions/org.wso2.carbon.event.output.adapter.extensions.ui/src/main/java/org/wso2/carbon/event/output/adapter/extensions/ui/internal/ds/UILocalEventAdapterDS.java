/*
 *
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.event.output.adapter.extensions.ui.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterFactory;
import org.wso2.carbon.event.output.adapter.extensions.ui.UIEventAdapterFactory;
import org.wso2.carbon.event.output.adapter.extensions.ui.UIOutputCallbackControllerService;
import org.wso2.carbon.event.output.adapter.extensions.ui.internal.UIOutputCallbackControllerServiceImpl;
import org.wso2.carbon.event.stream.core.EventStreamService;

/**
 * @scr.component component.name="output.extensions.Ui.AdapterService.component" immediate="true"
 * @scr.reference name="eventStreamService.service"
 * interface="org.wso2.carbon.event.stream.core.EventStreamService" cardinality="1..1"
 * policy="dynamic" bind="setEventStreamService" unbind="unsetEventStreamService"
 */
public class UILocalEventAdapterDS {

    private static final Log log = LogFactory.getLog(UILocalEventAdapterDS.class);

    /**
     * initialize the ui adapter service here service here.
     *
     * @param context
     */
    protected void activate(ComponentContext context) {

        try {
            OutputEventAdapterFactory uiEventAdapterFactory = new UIEventAdapterFactory();
            context.getBundleContext().registerService(OutputEventAdapterFactory.class.getName(), uiEventAdapterFactory, null);

            UIOutputCallbackControllerServiceImpl UIOutputCallbackRegisterServiceImpl = new UIOutputCallbackControllerServiceImpl();
            context.getBundleContext().registerService(UIOutputCallbackControllerService.class.getName(),
                    UIOutputCallbackRegisterServiceImpl, null);

            UIEventAdaptorServiceInternalValueHolder.registerUIOutputCallbackRegisterServiceInternal(
                    UIOutputCallbackRegisterServiceImpl);

            if (log.isDebugEnabled()) {
                log.debug("Successfully deployed the output ui adapter service");
            }
        } catch (RuntimeException e) {
            log.error("Can not create the output ui adapter service ", e);
        }
    }

    protected void setEventStreamService(EventStreamService eventStreamService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the EventStreamService reference for the UILocalEventAdaptor Service");
        }
        OutputAdaptorEventStreamServiceValueHolder.registerEventStreamService(eventStreamService);
    }

    protected void unsetEventStreamService(EventStreamService eventStreamService) {
        if (log.isDebugEnabled()) {
            log.debug("Un-Setting the EventStreamService reference for the UILocalEventAdaptor Service");
        }
        OutputAdaptorEventStreamServiceValueHolder.unregisterEventStreamService(eventStreamService);
    }
}
