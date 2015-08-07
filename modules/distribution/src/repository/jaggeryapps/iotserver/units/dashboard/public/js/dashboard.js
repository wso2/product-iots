var updateStats = function (serviceURL, id) {
    invokerUtil.get(
        serviceURL,
        function (result) {
            $(id).html(JSON.parse(result));
        }, function (message) {
            console.log(message);
        }
    );
};

$(document).ready(function(){
    updateStats("/iotserver/api/devices/count", "#device-count");
    updateStats("/iotserver/api/group/all/count", "#group-count");
    updateStats("/iotserver/api/policies/count", "#policy-count");
    updateStats("/iotserver/api/users/count", "#user-count");


});