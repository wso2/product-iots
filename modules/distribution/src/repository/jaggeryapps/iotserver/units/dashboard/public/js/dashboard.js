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

$(document).ready(function () {
    updateStats("/iotserver/api/devices/count", "#device-count");
    updateStats("/iotserver/api/group/all/count", "#group-count");
    updateStats("/iotserver/api/policies/count", "#policy-count");
    updateStats("/iotserver/api/users/count", "#user-count");

    displayEvents();
});

var displayEvents = function () {
    var eventListing = $("#event-stream");
    var eventListingSrc = eventListing.attr("src");
    $.template("event-stream", eventListingSrc, function (template) {
        var serviceURL = "/iotserver/api/event/list";
        var successCallback = function (data) {
            var viewModel = {};
            viewModel.data = JSON.parse(data);
            var content = template(viewModel);
            $("#event-stream-place-holder").html(content);
            formatDates();
        };
        invokerUtil.get(serviceURL,
            successCallback, function (message) {
                console.log(message);
            });
    });
}

function formatDates() {
    $(".formatDate").each(function () {
        var timeStamp = $(this).html();
        $(this).html(new Date(parseInt(timeStamp)).toUTCString());
    });
}
