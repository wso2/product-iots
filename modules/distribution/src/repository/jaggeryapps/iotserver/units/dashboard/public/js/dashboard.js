var updateStats = function (serviceURL, id) {
    invokerUtil.get(
        serviceURL,
        function (data) {
            $(id).html(data);
        }, function (message) {
            console.log(message);
        }
    );
};

$(document).ready(function(){
    //updateStats("/mdm-admin/devices/count", "#device-count");
    //updateStats("/mdm-admin/policies/count", "#policy-count");
    //updateStats("/mdm-admin/users/count/" + "carbon.super", "#user-count");
    //TODO: get this value from devicecloud webservice
    $("#device-count").html("0");
    $("#policy-count").html("0");
    $("#user-count").html("0");
});