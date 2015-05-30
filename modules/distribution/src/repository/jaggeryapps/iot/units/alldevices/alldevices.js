function onRequest(context) {
    context.myDevicePath = "mydevice";

    var constants = require("/modules/constants.js");
    var httpReq = new XMLHttpRequest();
    var user = session.get(constants.USER_SESSION_KEY);
    var endPoint = "http://localhost:9763/iotdevices/DevicesManager/getDevices?username=" + user.username;
    //
    httpReq.open("GET", endPoint, false);
    log.info("%%%%");
    log.info(httpReq.response);

    context.devices = {
        "devices": [
            {"name":"device1",
                "id" : 1234
            },
            {"name":"device2",
                "id" : 5678}
        ]
    };
    return context;
}

//
//{
//    "devices": [
//    {"name":"device1",
//        "id" : 1234
//    },
//    {"name":"device2",
//        "id" : 5678}
//]
//}
