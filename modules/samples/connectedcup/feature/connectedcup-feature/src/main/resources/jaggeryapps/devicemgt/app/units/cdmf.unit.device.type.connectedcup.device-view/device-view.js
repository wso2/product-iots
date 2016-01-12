function onRequest(context) {
    var log = new Log("detail.js");
    var deviceType = context.uriParams.deviceType;
    var deviceId = request.getParameter("id");

    if (deviceType != null && deviceType != undefined && deviceId != null && deviceId != undefined) {
        var deviceModule = require("/modules/device.js").deviceModule;
        var device = deviceModule.viewDevice(deviceType, deviceId);

        if (device && device.status != "error") {
            log.info(device);
            return {"device": device};
        }
    }
}