<%--
  ~ Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ WSO2 Inc. licenses this file to you under the Apache License,
  ~ Version 2.0 (the "License"); you may not use this file except
  ~ in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied. See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  --%>

<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<link href="../tenant-dashboard/css/dashboard-common.css" rel="stylesheet" type="text/css"
      media="all"/>
<%
    Object param = session.getAttribute("authenticated");
    String passwordExpires = (String) session.getAttribute(ServerConstants.PASSWORD_EXPIRATION);
    boolean hasModMgtPermission = CarbonUIUtil.isUserAuthorized(request,
                                                                "/permission/admin/manage/add/module");
    boolean hasServiceMgtPermission = CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/add/service");
    boolean hasWebAppMgtPermission = CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/manage/webapp");
    boolean loggedIn = false;
    if (param != null) {
        loggedIn = (Boolean) param;
    }
%>

<div id="passwordExpire">
    <%
        if (loggedIn && passwordExpires != null) {
    %>
    <div class="info-box"><p>Your password expires at <%=passwordExpires%>. Please change by
                             visiting <a
                href="../user/change-passwd.jsp?isUserChange=true&returnPath=../admin/index.jsp">here</a>
    </p></div>
    <%
        }
    %>
</div>
<div id="middle">
    <div id="workArea">
        <style type="text/css">
            .tip-table td.service-hosting {
                background-image: url(../../carbon/tenant-dashboard/images/service-hosting.png);
            }

            .tip-table td.web-applications {
                background-image: url(../../carbon/tenant-dashboard/images/web-applications.png);
            }

            .tip-table td.service-testing {
                background-image: url(../../carbon/tenant-dashboard/images/service-testing.png);
            }

            .tip-table td.message-tracing {
                background-image: url(../../carbon/tenant-dashboard/images/message-tracing.png);
            }

            .tip-table td.wsdl2java {
                background-image: url(../../carbon/tenant-dashboard/images/wsdl2java.png);
            }

            .tip-table td.java2wsdl {
                background-image: url(../../carbon/tenant-dashboard/images/java2wsdl.png);
            }

            .tip-table td.wsdl-validator {
                background-image: url(../../carbon/tenant-dashboard/images/wsdl-validator.png);
            }

            .tip-table td.modules {
                background-image: url(../../carbon/tenant-dashboard/images/modules.png);
            }
        </style>
        <h2 class="dashboard-title">WSO2 Enterprise Mobility Server quick start dashboard</h2>
        <table class="tip-table">
            <tr>
                <td class="tip-top service-hosting"></td>
                <td class="tip-top"></td>
                <td class="tip-top"></td>
                <td class="tip-top"></td>
                <td class="tip-top"></td>
            </tr>
            <tr>

                <td class="tip-content">
                    <div class="tip-content-lifter">
                        <%
                            if (hasWebAppMgtPermission) {
                        %>
                        <a class="tip-title"
                           href="../webapp-list/index.jsp?region=region1&item=webapps_list_menu">Web
                                                                                                 Applications</a>
                        <br/>
                        <%
                        } else {
                        %>
                        <h3>Web Applications</h3> <br/>
                        <%
                            }
                        %>
                        <p>Web Application hosting features in AppServer supports deployment of
                           Tomcat compliant Webapps. Deployed Webapps can be easily managed using
                           the Webapp management facilities available in the management console.</p>

                    </div>
                </td>
                <td class="tip-empty"></td>

            </tr>
            <tr>
            </tr>
        </table>
        <div class="tip-table-div"></div>


        <p>
            <br/>
        </p></div>
</div>
