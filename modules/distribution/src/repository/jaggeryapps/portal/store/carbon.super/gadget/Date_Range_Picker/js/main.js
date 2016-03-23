var href = parent.window.location.href,
    hrefLastSegment = href.substr(href.lastIndexOf('/') + 1),
    resolveURI = parent.ues.global.dashboard.id == hrefLastSegment ? '../' : '../../';

$(function() {
    var TOPIC = "range-selected";

    //if there are url elemements present, use them. Otherwis use last hour
    var timeFrom = moment().subtract(29, 'days');
    var timeTo = moment();

    var qs = gadgetUtil.getQueryString();
    if (qs.timeFrom != null) {
        timeFrom = qs.timeFrom;
    }
    if (qs.timeTo != null) {
        timeTo = qs.timeTo;
    }
    var count = 0;
    console.log("TimeFrom: " + timeFrom + " TimeTo: " + timeTo);

    //make the selected time range highlighted
    var timeUnit = qs.timeUnit;
    if(timeUnit != null) {
        $("#btnLast" + timeUnit).addClass("active");
    } else {
        $("#btnLastMonth").addClass("active");
    }

    cb(moment().subtract(29, 'days'), moment());

    function cb(start, end) {
        $('#reportrange #btnCustomRange').html(start.format('MMMM D, YYYY HH:mm') + ' - ' + end.format('MMMM D, YYYY HH:mm'));
        if (count != 0) {
            var message = {
                timeFrom: new Date(start).getTime(),
                timeTo: new Date(end).getTime(),
                timeUnit: "Custom"
            };
            gadgets.Hub.publish(TOPIC, message);
        }
        count++;
        // if(qs.timeUnit == 'Custom'){
        //     $("#date-select button").removeClass("active");
        //     $("#reportrange #btnCustomRange").addClass("active");
        // }
        // parent.window.location.hash = "some";
    }

    $('#btnCustomRange').daterangepicker({
        "timePicker" : true,
        "autoApply": true,
        "alwaysShowCalendars": true,
        "opens": "left"
    }, cb);

    $("#btnLastHour").click(function() {
        $("#date-select button").removeClass("active");
        $(this).addClass("active");
        var timeFrom = new Date(moment().subtract(1, 'hours')).getTime();
        var timeTo = new Date(moment()).getTime();
        var message = {
            timeFrom: timeFrom,
            timeTo: timeTo,
            timeUnit: "Hour"
        };
        gadgets.Hub.publish(TOPIC, message);
    });

    $("#btnLastDay").click(function() {
        $("#date-select button").removeClass("active");
        $(this).addClass("active");
        var message = {
            timeFrom: new Date(moment().subtract(1, 'day')).getTime(),
            timeTo: new Date(moment()).getTime(),
            timeUnit: "Day"
        };
        gadgets.Hub.publish(TOPIC, message);
    });

    $("#btnLastMonth").click(function() {
        $("#date-select button").removeClass("active");
        $(this).addClass("active");
        var message = {
            timeFrom: new Date(moment().subtract(29, 'days')).getTime(),
            timeTo: new Date(moment()).getTime(),
            timeUnit: "Month"
        };
        gadgets.Hub.publish(TOPIC, message);
    });

    $("#btnLastYear").click(function() {
        $("#date-select button").removeClass("active");
        $(this).addClass("active");
        var message = {
            timeFrom: new Date(moment().subtract(1, 'year')).getTime(),
            timeTo: new Date(moment()).getTime(),
            timeUnit: "Year"
        };
        gadgets.Hub.publish(TOPIC, message);
    });

    function appendToHash(timeFrom,timeTo) {
        parent.window.location.hash = "#timeFrom=" + timeFrom + "&timeTo=" + timeTo;
    }

});

$(window).load(function() {
    var datePicker = $('.daterangepicker'),
        parentWindow = window.parent.document,
        thisParentWrapper = $('#' + gadgets.rpc.RPC_ID, parentWindow).closest('.gadget-body');

    $('head', parentWindow).append('<link rel="stylesheet" type="text/css" href="'+resolveURI+'store/carbon.super/gadget/Date_Range_Picker/css/daterangepicker.css" />');
    $('body', parentWindow).append('<script src="'+resolveURI+'store/carbon.super/gadget/Date_Range_Picker/js/daterangepicker.js" type="text/javascript"></script>');
    $(thisParentWrapper).append(datePicker);
    $(thisParentWrapper).closest('.ues-component-box').addClass('widget form-control-widget');
    $('body').addClass('widget');
});