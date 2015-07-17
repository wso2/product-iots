/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

(function () {
    var cache = {};
    var permissionSet = {};
    var validateAndReturn = function (value) {
        return (value == undefined || value == null) ? "Unspecified" : value;
    };
    Handlebars.registerHelper("deviceMap", function (device) {
        device.owner = validateAndReturn(device.owner);
        device.ownership = validateAndReturn(device.ownership);
        var arr = device.properties;
        if (arr) {
            device.properties = arr.reduce(function (total, current) {
                total[current.name] = validateAndReturn(current.value);
                return total;
            }, {});
        }
    });

    //This method is used to setup permission for device listing
    $.setPermission = function (permission) {
        permissionSet[permission] = true;
    };

    $.hasPermission = function (permission) {
        return permissionSet[permission];
    };
})();

/*
 * Setting-up global variables.
 */
var deviceCheckbox = "#ast-container .ctrl-wr-asset .itm-select input[type='checkbox']";
var assetContainer = "#ast-container";

/*
 * DOM ready functions.
 */
$(document).ready(function () {
    /* Adding selected class for selected devices */
    $(deviceCheckbox).each(function () {
        addDeviceSelectedClass(this);
    });

    var i;
    var permissionList = $("#permission").data("permission");
    for (i = 0; i < permissionList.length; i++) {
        $.setPermission(permissionList[i]);
    }

    /* for device list sorting drop down */
    $(".ctrl-filter-type-switcher").popover({
        html: true,
        content: function () {
            return $("#content-filter-types").html();
        }
    });
});

/*
 * On Select All Device button click function.
 *
 * @param button: Select All Device button
 */
function selectAllDevices(button) {
    if (!$(button).data('select')) {
        $(deviceCheckbox).each(function (index) {
            $(this).prop('checked', true);
            addDeviceSelectedClass(this);
        });
        $(button).data('select', true);
        $(button).html('Deselect All Devices');
    } else {
        $(deviceCheckbox).each(function (index) {
            $(this).prop('checked', false);
            addDeviceSelectedClass(this);
        });
        $(button).data('select', false);
        $(button).html('Select All Devices');
    }
}

/*
 * On listing layout toggle buttons click function.
 *
 * @param view: Selected view type
 * @param selection: Selection button
 */
function changeDeviceView(view, selection) {
    $(".view-toggle").each(function () {
        $(this).removeClass("selected");
    });
    $(selection).addClass("selected");
    if (view == "list") {
        $(assetContainer).addClass("list-view");
    } else {
        $(assetContainer).removeClass("list-view");
    }
}

/*
 * Add selected style class to the parent element function.
 *
 * @param checkbox: Selected checkbox
 */
function addDeviceSelectedClass(checkbox) {
    if ($(checkbox).is(":checked")) {
        $(checkbox).closest(".ctrl-wr-asset").addClass("selected device-select");
    } else {
        $(checkbox).closest(".ctrl-wr-asset").removeClass("selected device-select");
    }
}
function loadDevices(searchType, searchParam) {
    var deviceListing = $("#device-listing");
    var deviceListingSrc = deviceListing.attr("src");
    var imageResource = deviceListing.data("image-resource");
    $.template("device-listing", deviceListingSrc, function (template) {
        var serviceURL;
        if ($.hasPermission("LIST_DEVICES")) {
            serviceURL = "/iotserver/api/devices/all";
        } else if ($.hasPermission("LIST_OWN_DEVICES")) {
            //Get authenticated users devices
            serviceURL = "/iotserver/api/devices/all";
        } else {
            $("#ast-container").html("Permission denied");
            return;
        }
        if (searchParam) {
            if (searchType == "users") {
                serviceURL = serviceURL + "?user=" + searchParam;
            } else if (searchType == "user-roles") {
                serviceURL = serviceURL + "?role=" + searchParam;
            } else {
                serviceURL = serviceURL + "?type=" + searchParam;
            }
        }
        var successCallback = function (data) {
            data = JSON.parse(data);
            var viewModel = {};
            viewModel.devices = data.data;
            viewModel.imageLocation = imageResource;
            if(!data.data || data.data.length <= 0){
                $("#ast-container").html($("#no-devices-div-content").html());
            } else {
                var content = template(viewModel);
                $("#ast-container").html(content);
                /*
                 * On device checkbox select add parent selected style class
                 */
                $(deviceCheckbox).click(function () {
                    addDeviceSelectedClass(this);
                });
                attachEvents();
                formatDates();
            }
        };
        invokerUtil.get(serviceURL,
            successCallback, function (message) {
                console.log(message);
            });
    });
}
$(document).ready(function () {
    loadDevices();
});

function formatDates(){
    $(".formatDate").each(function(){
        var timeStamp = $(this).html();
        $(this).html(new Date(parseInt(timeStamp)).toUTCString());
    });
}

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


/**
 * Following functions should be triggered after AJAX request is made.
 */
function attachEvents() {
    /**
     * Following click function would execute
     * when a user clicks on "Remove" link
     * on Device Management page in WSO2 MDM Console.
     */
    $("a.remove-device-link").click(function () {
        var deviceId = $(this).data("deviceid");
        var deviceType = $(this).data("devicetype");
        var removeDeviceAPI = "/iotserver/api/device/" + deviceType + "/" + deviceId + "/remove";

        $(modalPopupContent).html($('#remove-device-modal-content').html());
        showPopup();

        $("a#remove-device-yes-link").click(function () {
            invokerUtil.get(
                removeDeviceAPI,
                function (data,txtStatus,jqxhr) {
		    var status = jqxhr.status;
                    if (status == 200) {
                        $(modalPopupContent).html($('#remove-device-200-content').html());
                        $('div[data-deviceid="' + deviceId  + '"]').remove();
			$("a#remove-device-200-link").click(function () {
                            hidePopup();
                        });
                    } else if (status == 400) {
                        $(modalPopupContent).html($('#remove-device-400-content').html());
                        $("a#remove-device-400-link").click(function () {
                            hidePopup();
                        });
                    } else if (status == 403) {
                        $(modalPopupContent).html($('#remove-device-403-content').html());
                        $("a#remove-device-403-link").click(function () {
                            hidePopup();
                        });
                    } else if (status == 409) {
                        $(modalPopupContent).html($('#remove-device-409-content').html());
                        $("a#remove-device-409-link").click(function () {
                            hidePopup();
                        });
                    }
                },
                function () {
                    $(modalPopupContent).html($('#remove-device-unexpected-error-content').html());
                    $("a#remove-device-unexpected-error-link").click(function () {
                        hidePopup();
                    });
                }
            );
        });

        $("a#remove-device-cancel-link").click(function () {
            hidePopup();
        });

    });

    /**
     * Following click function would execute
     * when a user clicks on "Edit" link
     * on Device Management page in WSO2 MDM Console.
     */
    $("a.edit-device-link").click(function () {
        var deviceId = $(this).data("deviceid");
        var deviceType = $(this).data("devicetype");
        var deviceName = $(this).data("devicename");
        var editDeviceAPI = "/iotserver/api/device/" + deviceType + "/" + deviceId + "/update";

        $(modalPopupContent).html($('#edit-device-modal-content').html());
        $('#edit-device-name').val(deviceName);
        showPopup();

        $("a#edit-device-yes-link").click(function () {
            var newDeviceName = $('#edit-device-name').val();
	    var device={"device":{"name" : newDeviceName}};
            invokerUtil.post(
                editDeviceAPI,
                device,
                function (data,txtStatus,jqxhr) {
                    var status = jqxhr.status;
                    if (status == 200) {
                        $(modalPopupContent).html($('#edit-device-200-content').html());
			console.log("asdsdasda" + newDeviceName);
			$("div[data-deviceid='"+deviceId+"'] .ast-name").html(newDeviceName);
                        $("a#edit-device-200-link").click(function () {
                            hidePopup();
                        });
                    } else if (status == 400) {
                        $(modalPopupContent).html($('#edit-device-400-content').html());
                        $("a#edit-device-400-link").click(function () {
                            hidePopup();
                        });
                    } else if (status == 403) {
                        $(modalPopupContent).html($('#edit-device-403-content').html());
                        $("a#edit-device-403-link").click(function () {
                            hidePopup();
                        });
                    } else if (status == 409) {
                        $(modalPopupContent).html($('#edit-device-409-content').html());
                        $("a#edit-device-409-link").click(function () {
                            hidePopup();
                        });
                    }
                },
                function () {
                    $(modalPopupContent).html($('#edit-device-unexpected-error-content').html());
                    $("a#edit-device-unexpected-error-link").click(function () {
                        hidePopup();
                    });
                }
            );
        });

        $("a#edit-device-cancel-link").click(function () {
            hidePopup();
        });
    });
}
