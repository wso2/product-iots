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
    Handlebars.registerHelper("groupMap", function (group) {
        group.ownerId = validateAndReturn(group.ownerId);
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
var groupCheckbox = "#ast-container .ctrl-wr-asset .itm-select input[type='checkbox']";
var assetContainer = "#ast-container";

/*
 * DOM ready functions.
 */
$(document).ready(function () {
    /* Adding selected class for selected devices */
    $(groupCheckbox).each(function () {
        addGroupSelectedClass(this);
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
    changeGroupView('grid', this);
});

/*
 * On Select All Group button click function.
 *
 * @param button: Select All Group button
 */
function selectAllGroups(button) {
    if (!$(button).data('select')) {
        $(groupCheckbox).each(function (index) {
            $(this).prop('checked', true);
            addGroupSelectedClass(this);
        });
        $(button).data('select', true);
        $(button).html('Deselect All Groups');
    } else {
        $(groupCheckbox).each(function (index) {
            $(this).prop('checked', false);
            addGroupSelectedClass(this);
        });
        $(button).data('select', false);
        $(button).html('Select All Groups');
    }
}

/*
 * On listing layout toggle buttons click function.
 *
 * @param view: Selected view type
 * @param selection: Selection button
 */
function changeGroupView(view, selection) {
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
function addGroupSelectedClass(checkbox) {
    if ($(checkbox).is(":checked")) {
        $(checkbox).closest(".ctrl-wr-asset").addClass("selected device-select");
    } else {
        $(checkbox).closest(".ctrl-wr-asset").removeClass("selected device-select");
    }
}
function loadGroups(searchType, searchParam) {
    var groupListing = $("#group-listing");
    var groupListingSrc = groupListing.attr("src");
    var imageResource = groupListing.data("image-resource");
    $.template("group-listing", groupListingSrc, function (template) {
        var serviceURL;
        if ($.hasPermission("LIST_GROUPS")) {
            serviceURL = "/iotserver/api/group/all";
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
            viewModel.groups = data;
            viewModel.imageLocation = imageResource;
            if (!data || data.length <= 0) {
                $("#ast-container").html($("#no-groups-div-content").html());
            } else {
                var content = template(viewModel);
                $("#ast-container").html(content);
                /*
                 * On group checkbox select add parent selected style class
                 */
                $(groupCheckbox).click(function () {
                    addGroupSelectedClass(this);
                });
                attachEvents();
                formatDates();
            }
            attachGroupAdding();
        };
        invokerUtil.get(serviceURL,
            successCallback, function (message) {
                console.log(message);
            });
    });
}
$(document).ready(function () {
    loadGroups();
});

function formatDates() {
    $(".formatDate").each(function () {
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

var errorHandler = function () {
    $(modalPopupContent).html($('#add-group-unexpected-error-content').html());
    $("a#group-unexpected-error-link").click(function () {
        hidePopup();
    });
};

function attachGroupAdding() {
    /**
     * Following click function would execute
     * when a user clicks on "Remove" link
     * on Group Management page in WSO2 IoT Server Console.
     */
    $("a.add-group-link").click(function () {
        var addGroupApi = "/iotserver/api/group/add";
        $(modalPopupContent).html($('#add-group-modal-content').html());
        showPopup();

        $("a#add-group-yes-link").click(function () {
            var newGroupName = $('#add-group-name').val();
            var newGroupDescription = $('#add-group-description').val();
            var group = {"name": newGroupName, "description": newGroupDescription};
            invokerUtil.post(
                addGroupApi,
                group,
                function (data, txtStatus, jqxhr) {
                    var status = jqxhr.status;
                    if (status == 200) {
                        if (data != "false") {
                            $(modalPopupContent).html($('#add-group-200-content').html());
                            $("a#add-group-200-link").click(function () {
                                hidePopup();
                                location.reload();
                            });
                        } else {
                            $(modalPopupContent).html($('#group-400-content').html());
                            $("a#group-400-link").click(function () {
                                hidePopup();
                            });
                        }
                    } else if (status == 400) {
                        $(modalPopupContent).html($('#group-400-content').html());
                        $("a#group-400-link").click(function () {
                            hidePopup();
                        });
                    } else if (status == 403) {
                        $(modalPopupContent).html($('#agroup-403-content').html());
                        $("a#group-403-link").click(function () {
                            hidePopup();
                        });
                    } else if (status == 409) {
                        $(modalPopupContent).html($('#group-409-content').html());
                        $("a#group-409-link").click(function () {
                            hidePopup();
                        });
                    }
                }, errorHandler
            );
        });

        $("a#add-group-cancel-link").click(function () {
            hidePopup();
        });

    });
}

/**
 * Following functions should be triggered after AJAX request is made.
 */
function attachEvents() {
    /**
     * Following click function would execute
     * when a user clicks on "Share" link
     * on Group Management page in WSO2 IoT Server Console.
     */
    $("a.view-group-link").click(function () {
        $("#group_data").closest('form').submit();
    });

    /**
     * Following click function would execute
     * when a user clicks on "Share" link
     * on Group Management page in WSO2 IoT Server Console.
     */
    $("a.share-group-link").click(function () {
        var groupId = $(this).data("groupid");
        var username = $("#user-details").data("username");
        $(modalPopupContent).html($('#share-group-w1-modal-content').html());
        $('#user-names').html('Loading...');
        showPopup();
        $("a#share-group-next-link").hide();
        invokerUtil.get("/iotserver/api/users",
            function (data, txtStatus, jqxhr) {
                var users = JSON.parse(data);
                var status = jqxhr.status;
                if (status == 200) {
                    var str = '<br /><select id="share-user-selector" style="color:#3f3f3f;padding:5px;width:250px;">';
                    var hasUsers = false;
                    for (var user in users) {
                        if (users[user].username != username) {
                            str += '<option value="' + users[user].username + '">' + users[user].username + '</option>';
                            hasUsers = true;
                        }
                    }
                    str += '</select>';
                    if (!hasUsers) {
                        str = "There is no any other users registered";
                        $('#user-names').html(str);
                        return;
                    }
                    $('#user-names').html(str);
                    $("a#share-group-next-link").show();
                    $("a#share-group-next-link").click(function () {
                        var selectedUser = $('#share-user-selector').val();
                        $(modalPopupContent).html($('#share-group-w2-modal-content').html());
                        $('#user-roles').html('Loading...');
                        $("a#share-group-yes-link").hide();
                        invokerUtil.get("/iotserver/api/group/id/" + groupId + "/" + selectedUser + "/rolemapping",
                            function (data, txtStatus, jqxhr) {
                                var roleMap = JSON.parse(data);
                                var status = jqxhr.status;
                                if (status == 200) {
                                    var str = '';
                                    var isChecked = '';
                                    var hasRoles = false;
                                    for (var role in roleMap) {
                                        if (roleMap[role].assigned == true) {
                                            isChecked = 'checked';
                                        }
                                        str += '<label class="checkbox-text"><input type="checkbox" id="user-role-' + roleMap[role].role + '" value="' + roleMap[role].role
                                            + '" ' + isChecked + '/>' + roleMap[role].role + '</label>&nbsp;&nbsp;&nbsp;&nbsp;';
                                        hasRoles = true;
                                    }
                                    if (!hasRoles) {
                                        str = "There is no any roles for this group";
                                        return;
                                    }
                                    $('#user-roles').html(str);
                                    $("a#share-group-yes-link").show();
                                    $("a#share-group-yes-link").click(function () {
                                        var updatedRoleMap = [];
                                        for (var role in roleMap) {
                                            if ($('#user-role-' + roleMap[role].role).is(':checked') != roleMap[role].assigned){
                                                roleMap[role].assigned = $('#user-role-' + roleMap[role].role).is(':checked');
                                                updatedRoleMap.push(roleMap[role]);
                                            }
                                        }
                                        invokerUtil.post("/iotserver/api/group/id/" + groupId + "/" + selectedUser + "/roleupdate",
                                            updatedRoleMap,
                                            function (data, txtStatus, jqxhr) {
                                                var status = jqxhr.status;
                                                if (status == 200) {
                                                    $(modalPopupContent).html($('#share-group-200-content').html());
                                                    $("a#share-group-200-link").click(function () {
                                                        hidePopup();
                                                    });
                                                }else {
                                                    displayErrors(status);
                                                }
                                            }, errorHandler);
                                    });
                                } else {
                                    displayErrors(status);
                                }
                            }, errorHandler);
                        $("a#share-group-w2-cancel-link").click(function () {
                            hidePopup();
                        });
                    });
                } else {
                    displayErrors(status);
                }
            }, errorHandler);

        $("a#share-group-w1-cancel-link").click(function () {
            hidePopup();
        });

    });

    /**
     * Following click function would execute
     * when a user clicks on "Remove" link
     * on Group Management page in WSO2 IoT Server Console.
     */
    $("a.remove-group-link").click(function () {
        var groupId = $(this).data("groupid");
        var removeGroupApi = "/iotserver/api/group/id/" + groupId + "/remove";

        $(modalPopupContent).html($('#remove-group-modal-content').html());
        showPopup();

        $("a#remove-group-yes-link").click(function () {
            invokerUtil.delete(
                removeGroupApi,
                function (data, txtStatus, jqxhr) {
                    var status = jqxhr.status;
                    if (status == 200) {
                        if (data != "false") {
                            $(modalPopupContent).html($('#remove-group-200-content').html());
                            $('div[data-group="' + groupId + '"]').remove();
                            $("a#remove-group-200-link").click(function () {
                                hidePopup();
                                location.reload();
                            });
                        } else {
                            $(modalPopupContent).html($('#group-409-content').html());
                            $("a#group-409-link").click(function () {
                                hidePopup();
                            });
                        }
                    } else {
                        displayErrors(status);
                    }
                },
                function () {
                    $(modalPopupContent).html($('#group-unexpected-error-content').html());
                    $("a#group-unexpected-error-link").click(function () {
                        hidePopup();
                    });
                }
            );
        });

        $("a#remove-group-cancel-link").click(function () {
            hidePopup();
        });

    });

    /**
     * Following click function would execute
     * when a user clicks on "Edit" link
     * on Device Management page in WSO2 MDM Console.
     */
    $("a.edit-group-link").click(function () {
        var groupId = $(this).data("groupid");
        var groupName = $(this).data("groupname");
        var groupDescription = $(this).data("groupdescription");
        var editGroupApi = "/iotserver/api/group/id/" + groupId + "/update";

        $(modalPopupContent).html($('#edit-group-modal-content').html());
        $('#edit-group-name').val(groupName);
        $('#edit-group-description').val(groupDescription);
        showPopup();

        $("a#edit-group-yes-link").click(function () {
            var newGroupName = $('#edit-group-name').val();
            var newGroupDescription = $('#edit-group-description').val();
            var group = {"name": newGroupName, "description": newGroupDescription};
            invokerUtil.post(
                editGroupApi,
                group,
                function (data, txtStatus, jqxhr) {
                    var status = jqxhr.status;
                    if (status == 200) {
                        if (data != "false") {
                            $(modalPopupContent).html($('#edit-group-200-content').html());
                            $("div[data-groupid='" + groupId + "'] .ast-name").html(newGroupName);
                            $("a#edit-group-200-link").click(function () {
                                hidePopup();
                            });
                        } else {
                            $(modalPopupContent).html($('#group-409-content').html());
                            $("a#group-409-link").click(function () {
                                hidePopup();
                            });
                        }
                    } else {
                        displayErrors(status);
                    }
                },
                function () {
                    $(modalPopupContent).html($('#group-unexpected-error-content').html());
                    $("a#group-unexpected-error-link").click(function () {
                        hidePopup();
                    });
                }
            );
        });

        $("a#edit-group-cancel-link").click(function () {
            hidePopup();
        });
    });
}

function displayErrors(status) {
    if (status == 400) {
        $(modalPopupContent).html($('#group-400-content').html());
        $("a#group-400-link").click(function () {
            hidePopup();
        });
    } else if (status == 403) {
        $(modalPopupContent).html($('#group-403-content').html());
        $("a#group-403-link").click(function () {
            hidePopup();
        });
    } else if (status == 409) {
        $(modalPopupContent).html($('#group-409-content').html());
        $("a#group-409-link").click(function () {
            hidePopup();
        });
    }
}
