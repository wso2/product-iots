function onRequest(context) {
    var uri = request.getRequestURI();
    var uriMatcher = new URIMatcher(String(uri));
    var isMatched = uriMatcher.match("/{context}/device/{deviceType}/{+deviceId}");
    if (isMatched) {
        var matchedElements = uriMatcher.elements();
        var deviceType = matchedElements.deviceType;
        var deviceId = matchedElements.deviceId;
        context.deviceType = deviceType;
        context.deviceId = deviceId;
        var deviceModule = require("/modules/device.js").deviceModule;
        var device = deviceModule.viewDevice(deviceType, deviceId);
        if (device){
            var viewModel = {};
            var deviceInfo = device.properties.DEVICE_INFO;
            if (deviceInfo != undefined && String(deviceInfo.toString()).length > 0){
                deviceInfo = JSON.parse(deviceInfo);
                if (device.type == "ios"){
                    viewModel.imei = device.properties.IMEI;
                    viewModel.phoneNumber = deviceInfo.PhoneNumber;
                    viewModel.udid = deviceInfo.UDID;
                    viewModel.BatteryLevel = Math.round(deviceInfo.BatteryLevel * 100);
                    viewModel.DeviceCapacity = Math.round(deviceInfo.DeviceCapacity * 100) / 100;
                    viewModel.AvailableDeviceCapacity = Math.round(deviceInfo.AvailableDeviceCapacity * 100) / 100;
                    viewModel.DeviceCapacityUsed = Math.round((viewModel.DeviceCapacity
                        - viewModel.AvailableDeviceCapacity) * 100) / 100;
                    viewModel.DeviceCapacityPercentage = Math.round(viewModel.DeviceCapacityUsed
                        / viewModel.DeviceCapacity * 10000) /100;
                }else if(device.type == "android"){
                    viewModel.imei = device.properties.IMEI;
                    viewModel.model = device.properties.DEVICE_MODEL;
                    viewModel.vendor = device.properties.VENDOR;
                    viewModel.internal_memory = {};
                    viewModel.external_memory = {};
                    viewModel.location = {
                        latitude: device.properties.LATITUDE,
                        longitude: device.properties.LONGITUDE
                    };
                    viewModel.BatteryLevel = deviceInfo.BATTERY_LEVEL;
                    viewModel.internal_memory.FreeCapacity = Math.round((deviceInfo.INTERNAL_TOTAL_MEMORY -
                    deviceInfo.INTERNAL_AVAILABLE_MEMORY) * 100) / 100;
                    viewModel.internal_memory.DeviceCapacityPercentage = Math.round(deviceInfo.INTERNAL_AVAILABLE_MEMORY
                        / deviceInfo.INTERNAL_TOTAL_MEMORY * 10000) / 100;
                    viewModel.external_memory.FreeCapacity = Math.round((deviceInfo.EXTERNAL_TOTAL_MEMORY -
                        deviceInfo.EXTERNAL_AVAILABLE_MEMORY) * 100) / 100;
                    viewModel.external_memory.DeviceCapacityPercentage = Math.round(deviceInfo.EXTERNAL_AVAILABLE_MEMORY
                        /deviceInfo.EXTERNAL_TOTAL_MEMORY * 10000) /100;
                }
                viewModel.enrollment = device.enrollment;
                device.viewModel = viewModel;
            }
        }
        context.device = device;
    } else {
        response.sendError(404);
    }
    return context;
}