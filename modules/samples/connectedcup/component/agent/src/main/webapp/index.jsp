<html>
    <head>

        <link rel="stylesheet" href="bootstrap.min.css">
        <link rel="stylesheet" href="jquery-ui.css">
        <script src="jquery-1.12.0.min.js"></script>
        <script src="jquery-ui.js"></script>

        <style>

            .filler {
                height:200px;
                background-color:black;
            }
        </style>

    </head>
    <body>
    <%
        String deviceId = request.getParameter("deviceId");
        if(deviceId != null)request.getSession().setAttribute("deviceId",deviceId);

        String deviceOwner = request.getParameter("deviceOwner");
        if(deviceOwner != null)request.getSession().setAttribute("deviceOwner",deviceOwner);

        String token = request.getParameter("token");
        if(token != null)request.getSession().setAttribute("token",token);

    %>
    <div class="row col-sm-offset-5">
        <div class="col-md-4" ><h3>Connected Cup</h3></div>
    </div>
    <br>
    </br>

    <div class="row col-sm-offset-2">
        <div class="col-md-2" >
            <p>
                <label for="amount_temp">Temperature: </label>
                <input type="text" id="amount_temp" readonly style="border:0; color:#f6931f; font-weight:bold;">
            </p>
            <div id="temperature_level" style="height:200px; margin: 10%;"></div>
        </div>
        <div class="col-md-2" id="objectHolder">
            <p>
                <label for="amount">Coffee Level: </label>
                <input type="text" id="amount" readonly style="border:0; color:#f6931f; font-weight:bold;">
            </p>
            <div id="slider-vertical" style="height:200px; margin: 10%;"></div>

        </div>
        <div class="col-md-4" style="height:200px; ">
            <div id="filler" class="filler" style="height: 0%; bottom: 0px; position: absolute; width: 100%; ">
                <img src="coffeecup.png" style="height: 200px; width: 100%; position: absolute; bottom:0px;">
            </div>
        </div>
        <div class="col-md-2" >
            <button id="order-cup">Order Coffee Cup</button>
        </div>
    </div>

        <script type="application/javascript">
            function processCoffeeLevel(min, max){
                var filler = document.getElementById("filler");
                $( "#slider-vertical" ).slider({
                    orientation: "vertical",
                    range: "min",
                    min: min,
                    max: max,
                    value: 0,
                    slide: function( event, ui ) {
                        $( "#amount" ).val( ui.value );
                        filler.style.height = map(min, max, 0, 100, ui.value) + "%";
                    }
                });


                $( "#amount" ).val( $( "#slider-vertical" ).slider( "value" ) );


            }

            function processTemperature(min, max){
                $( "#temperature_level" ).slider({
                    orientation: "vertical",
                    range: "min",
                    min: min,
                    max: max,
                    value: 0,
                    slide: function( event, ui ) {
                        $( "#amount_temp" ).val( ui.value );
                    }
                });
            }

            function map(a,b,c,d,N)
            {
                return c+((N-a)*(d-c))/(b-a);
            }

            processCoffeeLevel(0, 100);
            processTemperature(0,100);

            $("#order-cup").click(function() {

                var deviceId = '<%=request.getSession().getAttribute("deviceId")%>';
                var deviceOwner = '<%=request.getSession().getAttribute("deviceOwner")%>';
                var token = '<%=request.getSession().getAttribute("token")%>';
                var url = "https://localhost:9443/connectedcup/controller/ordercoffee?deviceId=" + deviceId +"&deviceOwner=" +
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
                var tempPayload = "temperature:" + $( "#temperature_level" ).slider( "value" );
                var levelPayload = "coffeelevel:" + $( "#slider-vertical" ).slider( "value" );
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
