<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<jsp:include page="../dialog/display_messages.jsp"/>

<link href="../tenant-dashboard/css/dashboard-common.css" rel="stylesheet" type="text/css" media="all"/>
<%
        Object param = session.getAttribute("authenticated");
        String passwordExpires = (String) session.getAttribute(ServerConstants.PASSWORD_EXPIRATION);
        boolean loggedIn = false;
        if (param != null) {
            loggedIn = (Boolean) param;             
        } 
%>
  
<div id="passwordExpire">
         <%
         if (loggedIn && passwordExpires != null) {
         %>
              <div class="info-box"><p>Your password expires at <%=passwordExpires%>. Please change by visiting <a href="../user/change-passwd.jsp?isUserChange=true&returnPath=../admin/index.jsp">here</a></p></div>
         <%
             }
         %>
</div>
<div id="middle">
<div id="workArea">


<style type="text/css">
    .tip-table td.real-time-monitoring {
        background-image: url(../../carbon/tenant-dashboard/images/real-time-monitoring.png);
    }

    .tip-table td.mediation-analytic {
        background-image: url(../../carbon/tenant-dashboard/images/mediation-analytic.png);
    }
    .tip-table td.real-time-service-monitoring {
        background-image: url(../../carbon/tenant-dashboard/images/real-time-service-monitoring.png);
    }
    .tip-table td.summary-genaration {
        background-image: url(../../carbon/tenant-dashboard/images/summary-genaration.png);
    }


    .tip-table td.service-analytic {
        background-image: url(../../carbon/tenant-dashboard/images/service-analytic.png);
    }
    .tip-table td.dashboard {
        background-image: url(../../carbon/tenant-dashboard/images/dashboard.png);
    }
    .tip-table td.activity-correlation{
        background-image: url(../../carbon/tenant-dashboard/images/activity-correlation.png);
    }
    .tip-table td.message-archival{
        background-image: url(../../carbon/tenant-dashboard/images/message-archival.png);
    }
</style>
 <h2 class="dashboard-title">WSO2 DAS quick start dashboard</h2>
        <table class="tip-table">
            <tr>
                <td class="tip-top real-time-monitoring"></td>
                <td class="tip-empty"></td>
                <td class="tip-top mediation-analytic"></td>
                <td class="tip-empty "></td>
                <td class="tip-top real-time-service-monitoring"></td>
                <td class="tip-empty "></td>
                <td class="tip-top summary-genaration"></td>
            </tr>
            <tr>
                <td class="tip-content">
                    <div class="tip-content-lifter">
                        <h3 class="tip-title">Real Time Mediation Monitoring </h3> <br/>


                        <p>Monitor proxy services, endpoints and sequences and their operations in real time. The results gathered from the real-time monitoring are presented in a clear and easy-to-view format.  </p>

                    </div>
                </td>
                <td class="tip-empty"></td>
                <td class="tip-content">
                    <div class="tip-content-lifter">
                         <h3 class="tip-title">Mediation Analysis</h3><br/>

                        <p>Analyze the historical mediation trends over time with the help of visualization framework.</p>

                    </div>
                </td>
                <td class="tip-empty"></td>
                <td class="tip-content">
                    <div class="tip-content-lifter">
                        <h3 class="tip-title">Real time Service Monitoring </h3> <br/>


                        <p>Monitor services and their operations in real time. The results gathered from the real-time monitoring are presented in a clear and easy-to-view format.  </p>

                    </div>
                </td>
                <td class="tip-empty"></td>
                <td class="tip-content">
                    <div class="tip-content-lifter">
                        <h3 class="tip-title">Summary Generation</h3> <br/>


                        <p>Generate a summarized view of collected statistics & write gadgets to display these summarized data</p>

                    </div>
                </td>
            </tr>
            <tr>
                <td class="tip-bottom"></td>
                <td class="tip-empty"></td>
                <td class="tip-bottom"></td>
                <td class="tip-empty"></td>
                <td class="tip-bottom"></td>
                <td class="tip-empty"></td>
                <td class="tip-bottom"></td>
            </tr>
        </table>
	<div class="tip-table-div"></div>
        <table class="tip-table">
            <tr>
                <td class="tip-top service-analytic"></td>
                <td class="tip-empty"></td>
                <td class="tip-top dashboard"></td>
                <td class="tip-empty "></td>
                <td class="tip-top activity-correlation"></td>
                <td class="tip-empty "></td>
                <td class="tip-top message-archival"></td>
            </tr>
            <tr>
                <td class="tip-content">
                    <div class="tip-content-lifter">
                         <h3 class="tip-title">Service Invocation Analysis</h3> <br/>


                        <p>Analyze trends over time to help make both business and IT decisions to fine tune SOA deployment.</p>

                    </div>
                </td>
                <td class="tip-empty"></td>
                <td class="tip-content">
                    <div class="tip-content-lifter">
                         <h3 class="tip-title">Dashboard</h3><br/>


                        <p>Write gadgets to consume service statistics and present to end user. Dashboard support Google Gadget specification.</p>

                    </div>
                </td>
                <td class="tip-empty"></td>
                <td class="tip-content">
                    <div class="tip-content-lifter">
                        <h3 class="tip-title">Activity Correlation & Monitoring</h3> <br/>


                        <p>Monitor and correlate related business activities across multiple server instances. These activities can be used to track and verify the smooth execution of business.</p>

                    </div>
                </td>
                <td class="tip-empty"></td>
                <td class="tip-content">
                    <div class="tip-content-lifter">
                        <h3 class="tip-title">Message Collection & Archival</h3> <br/>


                        <p>Collect messages coming into your SOA, analyze & archive for future retrieval.</p>

                    </div>
                </td>
            </tr>
            <tr>
                <td class="tip-bottom"></td>
                <td class="tip-empty"></td>
                <td class="tip-bottom"></td>
                <td class="tip-empty"></td>
                <td class="tip-bottom"></td>
                <td class="tip-empty"></td>
                <td class="tip-bottom"></td>
            </tr>
        </table>

<p>
    <br/>
</p> </div>
</div>

