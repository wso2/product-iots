/**
 * Sorting function of users
 * listed on User Management page in WSO2 MDM Console.
 */
$(function () {
    var sortableElem = '.wr-sortable';
    $(sortableElem).sortable({
        beforeStop : function () {
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
    $(modalPopupContent).css('max-height', ($(body).height() - ($(body).height()/100 * 30)));
    $(modalPopupContainer).css('margin-top', (-($(modalPopupContainer).height()/2)));
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

/**
 * Following click function would execute
 * when a user clicks on "Invite" link
 * on User Management page in WSO2 MDM Console.
 */
$("a.invite-user-link").click(function () {
    var username = $(this).data("username");
    var inviteUserAPI = "/iotserver/api/users/" + username + "/invite";

    $(modalPopupContent).html($('#invite-user-modal-content').html());
    showPopup();

    $("a#invite-user-yes-link").click(function () {
        invokerUtil.get(
            inviteUserAPI,
            function () {
                $(modalPopupContent).html($('#invite-user-success-content').html());
                $("a#invite-user-success-link").click(function () {
                    hidePopup();
                });
            },
            function () {
                $(modalPopupContent).html($('#invite-user-error-content').html());
                $("a#invite-user-error-link").click(function () {
                    hidePopup();
                });
            }
        );
    });

    $("a#invite-user-cancel-link").click(function () {
        hidePopup();
    });
});

/**
 * Following click function would execute
 * when a user clicks on "Remove" link
 * on User Management page in WSO2 MDM Console.
 */
$("a.remove-user-link").click(function () {
    var username = $(this).data("username");
    var removeUserAPI = "/iotserver/api/users/" + username + "/remove";

    $(modalPopupContent).html($('#remove-user-modal-content').html());
    showPopup();

    $("a#remove-user-yes-link").click(function () {
        invokerUtil.get(
            removeUserAPI,
            function (data) {
                if (data == 200) {
                    $("#" + username).addClass("hide");
                    $(modalPopupContent).html($('#remove-user-200-content').html());
                    $("a#remove-user-200-link").click(function () {
                        hidePopup();
                    });
                } else if (data == 400) {
                    $(modalPopupContent).html($('#remove-user-400-content').html());
                    $("a#remove-user-400-link").click(function () {
                        hidePopup();
                    });
                } else if (data == 403) {
                    $(modalPopupContent).html($('#remove-user-403-content').html());
                    $("a#remove-user-403-link").click(function () {
                        hidePopup();
                    });
                } else if (data == 409) {
                    $(modalPopupContent).html($('#remove-user-409-content').html());
                    $("a#remove-user-409-link").click(function () {
                        hidePopup();
                    });
                }
            },
            function () {
                $(modalPopupContent).html($('#remove-user-unexpected-error-content').html());
                $("a#remove-user-unexpected-error-link").click(function () {
                    hidePopup();
                });
            }
        );
    });

    $("a#remove-user-cancel-link").click(function () {
        hidePopup();
    });
});