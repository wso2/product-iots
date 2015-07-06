/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
 * Setting-up global variables.
 */

var operations = '.wr-operations',
    modalPopup = '.wr-modalpopup',
    modalPopupContainer = modalPopup + ' .modalpopup-container',
    modalPopupContent = modalPopup + ' .modalpopup-content',
    deviceCheckbox = '#ast-container .ctrl-wr-asset .itm-select input[type="checkbox"]',
    showOperationsBtn = '#showOperationsBtn',
    navHeight = $('#nav').height(),
    headerHeight = $('header').height(),
    offset = (headerHeight + navHeight),
    maxOperationsLimit = 15,
    hiddenOperation = '.wr-hidden-operations-content > div',
    deviceSelection = '.device-select',
    currentOperationList = [];

/*
 * DOM ready functions.
 */
$(document).ready(function(){
    if($(operations + "> a").length > maxOperationsLimit){
        $(showOperationsBtn).show();
    }
    else{
        $(operations).show();
    }
    toggleMoreOperationsHeight();
    //loadOperationBar("ios");
    /**
     * Android App type javascript
     */
    $(".wr-modalpopup").on("click", ".appTypesInput", function(){
        var appType = $(".appTypesInput").val();
        if (appType == "Public") {
            $('.appURLInput').prop( "disabled", true );
        }else if (appType == "Enterprise"){
            $('.appURLInput').prop( "disabled", false );
        }
    }).trigger("change");
});


/*
 * On window loaded functions.
 */
$(window).load(function(){
    setPopupMaxHeight();
});

/*
 * On window resize functions.
 */
$(window).resize(function(){
    toggleMoreOperationsHeight();
    setPopupMaxHeight();
});

/*
 * On main div.container resize functions.
 * @required  jquery.resize.js
 */
$('.container').resize(function(){
    toggleMoreOperationsHeight();
});

/*
 * On Show Operations click operation show toggling function.
 */
function showOperations(){
    $(operations).toggle('slide');
}

/*
 * On operation click function.
 * @param selection: Selected operation
 */
function operationSelect(selection){
    var deviceIdList = getSelectedDeviceIds();
    $(modalPopupContent).addClass("operation-data");
    if (deviceIdList == 0){
        $(modalPopupContent).html($('#errorOperations').html());
    }else {
        $(modalPopupContent).html($(operations + ' .operation[data-operation='+selection+']').html());
        $(modalPopupContent).data("operation", selection);
    }
    showPopup();
}

/*
 * show popup function.
 */
function showPopup() {
    $(modalPopup).show();
}

/*
 * hide popup function.
 */
function hidePopup() {
    $(modalPopupContent).html('');
    $(modalPopupContent).removeClass("operation-data");
    $(modalPopup).hide();
}

/*
 * set popup maximum height function.
 */
function setPopupMaxHeight() {
    $(modalPopupContent).css('max-height', ($('body').height() - ($('body').height()/100 * 30)));
    $(modalPopupContainer).css('margin-top', (-($(modalPopupContainer).height()/2)));
}


/*
 * Function to open hidden device operations list
 */
function toggleMoreOperations(){
    $('.wr-hidden-operations, .wr-page-content').toggleClass('toggled');
    $(showOperationsBtn).toggleClass('selected');
    //$('.footer').toggleClass('wr-hidden-operations-toggled');
}

/*
 * Function to fit hidden device operation window height with the screen
 */
function toggleMoreOperationsHeight(){
    $('.wr-hidden-operations').css('min-height', $('html').height() - (offset+140));
}

/*
 * Advance operations sub categories show/hide toggle function
 */
function showAdvanceOperation(operation, button){
    $(button).addClass('selected');
    $(button).siblings().removeClass('selected');
    $(hiddenOperation + '[data-operation="' + operation + '"]').show();
    $(hiddenOperation + '[data-operation="' + operation + '"]').siblings().hide();
}

/*
 * Function to get selected devices ID's
 */
function getSelectedDeviceIds(){
    var deviceIdentifierList = [];
    $(deviceSelection).each(function(index){
        var device = $(this);
        var deviceId = device.data('deviceid');
        var deviceType = device.data('type');
        deviceIdentifierList.push({
            "id" : deviceId,
            "type" : deviceType
        });
    });
    return deviceIdentifierList;
}
function getDevicesByTypes(deviceList){
    var deviceTypes = {};
    jQuery.each(deviceList, function(index, item) {
        if(!deviceTypes[item.type]){
            deviceTypes[item.type] = [];
        }
        if(item.type == "ios"){
            //for iOS we are sending only the IDS cause we are sending it to the JAX-RS
            deviceTypes[item.type].push(item.id);
        }
    });
    return deviceTypes;
}
function unloadOperationBar(){
    $("#showOperationsBtn").addClass("hidden");
    $(".wr-operations").html("");
}

function loadOperationBar(deviceType){
    var operationBar = $("#operations-bar");
    var operationBarSrc = operationBar.attr("src");
    var platformType = deviceType;
    $.template("operations-bar", operationBarSrc, function (template) {
        var serviceURL = "/mdm-admin/features/" + platformType;
        var successCallback = function (data) {
            var viewModel = {};
            var iconMap = {};
            data = data.filter(function(current){
                var iconName;
                if (deviceType == "android"){
                    var iconName = operationModule.getAndroidIconForFeature(current.code);
                } else if (deviceType == "ios"){
                    var iconName = operationModule.getiOSIconForFeature(current.code);
                }
                if (iconName){
                    current.icon = iconName;
                    return current;
                }
            });
            viewModel.features = data;
            var content = template(viewModel);
            $(".wr-operations").html(content);
        };
        invokerUtil.get(serviceURL,
            successCallback, function(message){
                console.log(message);
            });
    });
    var hiddenOperationBar = $("#hidden-operations-bar-" + deviceType);
    var hiddenOperationBarSrc = hiddenOperationBar.attr("src");
    $.template("hidden-operations-bar-" + deviceType, hiddenOperationBarSrc, function (template) {
        var serviceURL = "/mdm-admin/features/" + platformType;
        var successCallback = function (data) {
            var viewModel = {};
            viewModel.features = data.reduce(function (total, current) {
                total[current.code] = current;
                return total;
            }, {});
            currentOperationList = viewModel.features;
            var content = template(viewModel);
            $(".wr-hidden-operations").html(content);
        };
        invokerUtil.get(serviceURL,
            successCallback, function(message){
                console.log(message);
            });
    });
    $("#showOperationsBtn").removeClass("hidden");
}

function runOperation(operationName) {
    var deviceIdList = getSelectedDeviceIds();
    var list = getDevicesByTypes(deviceIdList);
    var successCallback = function(message){
        console.log(message);
        $(".wr-notification-bar").append('<div class="wr-notification-desc new"><div ' +
        'class="wr-notification-operation">' + currentOperationList[operationName].name +
        '- Operation Successful!</div><hr /></div>');
        var notificationCount = parseInt($(".wr-notification-bubble").html());
        notificationCount++;
        $(".wr-notification-bubble").html(notificationCount);
    };
    if(list["ios"]){
        var payload = operationModule.generatePayload("ios", operationName, list["ios"]);
        var serviceEndPoint = operationModule.getiOSServiceEndpoint(operationName);

    }
    if(list["android"]){
        var payload = operationModule.generatePayload("android", operationName, list["android"]);
        var serviceEndPoint = operationModule.getAndroidServiceEndpoint(operationName);
    }
    invokerUtil.post(serviceEndPoint, payload,
        successCallback, function(jqXHR, textStatus, errorThrown){
            console.log(textStatus);
        });
    $(modalPopupContent).removeData();
    hidePopup();
}
