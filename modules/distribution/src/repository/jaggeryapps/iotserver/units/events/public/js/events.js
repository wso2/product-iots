$(document).ready(function () {
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
};