<%--
  ~ Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

<html>
<head>
    <title>Connected Coffee Cup</title>

    <link rel="stylesheet" href="css/coffee.css">

    <!--[if lt IE 9]>
    <script src="http://html5shiv.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->
</head>

<body>
<%
    String deviceId = request.getParameter("deviceId");
    if (deviceId != null) {
        request.getSession().setAttribute("deviceId", deviceId);
    }

    String deviceOwner = request.getParameter("deviceOwner");
    if (deviceOwner != null) {
        request.getSession().setAttribute("deviceOwner", deviceOwner);
    }

    String token = request.getParameter("token");
    if (token != null) {
        request.getSession().setAttribute("token", token);
    }

%>

<div class="container">
    <h1>Coffee Cup - Demo</h1>
    <div class="left-pane">
        <table style="width:100%">
            <tr>
                <td align="right" style="width: 140px">Temperature:</td>
                <td id="temperature_level" align="left" style="width: 60px">0 C</td>
                <td align="right" style="width: 140px">Coffee Level:</td>
                <td id="coffee_level" align="left" style="width: 60px">0%</td>
            </tr>
            <tr>
                <td colspan="2" align="center">
                    <input id="amount_temp" type="range" orient="vertical" min="0" max="100"
                           value="0"
                           onchange="updateTemperature(this.value)"><br/>
                </td>
                <td colspan="2" align="center">
                    <input id="amount_coffee" type="range" orient="vertical" min="0" max="100"
                           value="0"
                           onchange="updateCoffee(this.value)">
                </td>
            </tr>
            <tr>
                <td colspan="4" align="center">
                    <button id="order-cup">Order Coffee Cup</button>
                </td>
            </tr>
        </table>
    </div>
    <div class="right-pane">
        <div class="coffee-wrapper">
            <div class="coffee">
                <div class="coffee_main">
                    <div class="coffee_inner">
                        <div class="handle"></div>
                        <div class="water" id="water"></div>
                    </div>
                    <div class="highlight"></div>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="js/libs/jquery.min.js"></script>
<script src="js/coffee.js"></script>
<script src="js/libs/htmlpreview.min.js"></script>
<script>HTMLPreview.replaceAssets();</script>
<script>
    $("#order-cup").click(function() {

        var deviceId = '<%=request.getSession().getAttribute("deviceId")%>';
        var deviceOwner = '<%=request.getSession().getAttribute("deviceOwner")%>';
        var token = '<%=request.getSession().getAttribute("token")%>';
        var url = "/connectedcup/controller/ordercoffee?deviceId=" + deviceId +"&deviceOwner=" +
                  deviceOwner;

        $.ajax({
                   type: 'POST',
                   url: url,
                   headers: {
                       "Authorization" : "Bearer " + token

                   }
               });

    });

    function sendData()
    {
        var deviceId = '<%=request.getSession().getAttribute("deviceId")%>';
        var deviceOwner = '<%=request.getSession().getAttribute("deviceOwner")%>';
        var tempPayload = "temperature:" + temperature;
        var levelPayload = "coffeelevel:" + coffee_amount;
        $.post( "/connected-cup-agent/push_temperature?deviceId=" + deviceId +"&deviceOwner=" + deviceOwner +
                "&payload=" + tempPayload);
        $.post( "/connected-cup-agent/push_level?deviceId=" + deviceId +"&deviceOwner=" + deviceOwner +
                "&payload=" + levelPayload);
        setTimeout(sendData, 5000);
    }

    sendData();

</script>
</body>
</html>