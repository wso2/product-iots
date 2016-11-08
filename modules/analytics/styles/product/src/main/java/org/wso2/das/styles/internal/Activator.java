/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.das.styles.internal;

import org.eclipse.equinox.http.helper.ContextPathServletAdaptor;
import org.eclipse.equinox.jsp.jasper.JspServlet;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.wso2.carbon.ui.DefaultComponentEntryHttpContext;

import javax.servlet.Servlet;

/**
 *
 */
public class Activator implements BundleActivator {

    private static String CONTEXT = "/styles";

    private static String INTERNAL_CONTEXT = "/web";

    public void start(BundleContext bundleContext) throws Exception {

        ServiceReference reference = bundleContext.getServiceReference(HttpService.class.getName());
        if (reference != null) {
            final HttpService httpService = (HttpService) bundleContext.getService(reference);
            try {
                HttpContext commonContext =
                        new DefaultComponentEntryHttpContext(bundleContext.getBundle(), INTERNAL_CONTEXT);

                //register our .jsp files at the httpService
                Servlet servlet = new ContextPathServletAdaptor(
                        new JspServlet(bundleContext.getBundle(), "/web"), CONTEXT);
                httpService.registerResources(CONTEXT, "/", commonContext);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            throw new Exception("HttpService is not found.");
        }
    }

    public void stop(BundleContext context) throws Exception {

    }
}
