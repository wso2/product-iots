function onRequest(context) {
    context.myDevicePath = "/iot/mydevice";

    var constants = require("/modules/constants.js");
    var httpReq = new XMLHttpRequest();
    var user = session.get(constants.USER_SESSION_KEY);
    var endPoint = "http://localhost:9763/iotdevices/DevicesManager/getDevices?username=" + user.username;
    //
    httpReq.open("GET", endPoint, false);
    httpReq.setRequestHeader("Content-type","application/json");
    httpReq.send();
    //
    context.devices = JSON.parse(httpReq.responseText);
    return context;
}

//{
//    "device": [
//    {
//        "dateOfEnrolment": 1432978481443,
//        "dateOfLastUpdate": 1432978481443,
//        "deviceIdentifier": "qhnva32dy2ck",
//        "deviceTypeId": 1,
//        "id": 1,
//        "name": "rasikas_firealarm_qhn",
//        "owner": "rasika",
//        "properties": {
//            "name": "DEVICE_NAME",
//            "value": "rasikas_firealarm_qhn"
//        },
//        "status": "ACTIVE",
//        "type": "firealarm"
//    }
//]
//}
