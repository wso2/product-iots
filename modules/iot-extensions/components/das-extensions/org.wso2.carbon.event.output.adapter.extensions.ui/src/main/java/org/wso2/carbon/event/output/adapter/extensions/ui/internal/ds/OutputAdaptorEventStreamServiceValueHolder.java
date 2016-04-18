/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *
 */

package org.wso2.carbon.event.output.adapter.extensions.ui.internal.ds;

import org.wso2.carbon.event.stream.core.EventStreamService;

/**
 * This class holds a reference to the current EventStream OSGI service component "eventStreamService.component" in the
 * OSGI runtime.
 */
public class OutputAdaptorEventStreamServiceValueHolder {
    private static EventStreamService eventStreamService;

    public static void registerEventStreamService(EventStreamService eventBuilderService) {
        OutputAdaptorEventStreamServiceValueHolder.eventStreamService = eventBuilderService;
    }

    public static EventStreamService getEventStreamService() {
        return OutputAdaptorEventStreamServiceValueHolder.eventStreamService;
    }

    public static void unregisterEventStreamService(EventStreamService eventStreamService) {
        OutputAdaptorEventStreamServiceValueHolder.eventStreamService = null;
    }
}
