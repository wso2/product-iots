/**
 * Sorting function of users
 * listed on User Management page in WSO2 MDM Console.
 */
$(function () {
    var sortableElem = '.wr-sortable';
    $(sortableElem).sortable({
        beforeStop: function () {
            var sortedIDs = $(this).sortable('toArray');
            console.log(sortedIDs);
        }
    });
    $(sortableElem).disableSelection();
});

var modalPopup = ".wr-modalpopup";
var modalPopupContainer = modalPopup + " .modalpopup-container";
var modalPopupContent = modalPopup + " .modalpopup-content";
var body = "body";

/*
 * set popup maximum height function.
 */
function setPopupMaxHeight() {
    $(modalPopupContent).css('max-height', ($(body).height() - ($(body).height() / 100 * 30)));
    $(modalPopupContainer).css('margin-top', (-($(modalPopupContainer).height() / 2)));
}

/*
 * show popup function.
 */
function showPopup() {
    $(modalPopup).show();
    setPopupMaxHeight();
}

/*
 * hide popup function.
 */
function hidePopup() {
    $(modalPopupContent).html('');
    $(modalPopup).hide();
}

$(document).ready(function () {
    formatDates();
});

function formatDates() {
    $(".formatDate").each(function () {
        var timeStamp = $(this).html();
        $(this).html(new Date(parseFloat(timeStamp)).toUTCString());
    });
}

/**
 * Following click function would execute
 * when a user clicks on "Remove" link
 * on User Management page in WSO2 MDM Console.
 */
$("a.remove-policy-link").click(function () {
    var deviceType = $(this).data("devicetype");
    var policyName = $(this).data("policyname");
    var policyUUID = $(this).data("policyuuid");
    ///{context}/api/policies/{deviceType}/{policyName}/remove
    var removePolicyAPI = "/iotserver/api/policies/" + deviceType + "/" + policyName + "/remove";

    $(modalPopupContent).html($('#remove-policy-modal-content').html());
    showPopup();

    $("a#remove-policy-yes-link").click(function () {
        invokerUtil.get(
            removePolicyAPI,
            function (data) {
                if (data == 200 || data == "true") {
                    $(modalPopupContent).html($('#remove-policy-200-content').html());
                    $('#' + policyUUID).remove();
                    $("a#remove-policy-200-link").click(function () {
                        hidePopup();
                    });
                } else if (data == 400) {
                    $(modalPopupContent).html($('#remove-policy-400-content').html());
                    $("a#remove-policy-400-link").click(function () {
                        hidePopup();
                    });
                } else if (data == 403) {
                    $(modalPopupContent).html($('#remove-policy-403-content').html());
                    $("a#remove-policy-403-link").click(function () {
                        hidePopup();
                    });
                } else if (data == 409 || data == "false") {
                    $(modalPopupContent).html($('#remove-policy-409-content').html());
                    $("a#remove-policy-409-link").click(function () {
                        hidePopup();
                    });
                } else if (data == 500) {
                    $(modalPopupContent).html($('#remove-policy-unexpected-content').html());
                    $("a#remove-policy-unexpected-link").click(function () {
                        hidePopup();
                    });
                }
            },
            function () {
                $(modalPopupContent).html($('#remove-policy-unexpected-error-content').html());
                $("a#remove-policy-unexpected-error-link").click(function () {
                    hidePopup();
                });
            }
        );
    });

    $("a#remove-policy-cancel-link").click(function () {
        hidePopup();
    });
});