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
var menuButton = '.ctrl-asset-type-switcher',
    menu = '.popover.menu',
    deviceOptions = '#device-filter-options',
    menuContainer = '#asset-select',
    menuSubContainer = '#asset-selected',
    menuItems = '#asset-select ul li',
    tagsContainer = '.wr-search-tags',
    searchField = '#search',
    menuItemsID = 0,
    prevSelected = 0,
    searchBtn = ".btn-search";

/*
 * DOM ready functions.
 */
$(document).ready(function(){

    /* Bind filter menu relationship on load */
    addingIdentity();

    /* Bind filter select function to all assets on load */
    $(menuItems).each(function(){
        menuItemsID++;
        $(this).attr({
            'asset':menuItemsID,
            'onclick':'selectAsset(this)',
            'level': $(this).parents('ul').length
        });
    });
    $(searchBtn).click(function(){
        var input = $(searchField).html();
        var searchType = $(searchField).data("search-type");
        loadGroups(searchType, input);
    });
});

/*
 * Bind filter window hide function to the document.
 */
$(document).bind('click', function(e) {
    if($(e.target).attr('rel') !== 'assetfilter') {
        $(menu).hide();
    }
});

/*
 * On filter switch button click show filter option window.
 */
$(menuButton).click(function(e){
    e.stopPropagation();
    $(menu).toggle();
});

/*
 * On asset filter options window advance filter radio button change function
 */
$('#advance-filter-options input[type=radio]').change(function() {
    if(this.value !== 'devices'){
        $(searchField).attr('data-placeholder', 'Search '+ this.value + ' ...');
    }
    else {
        $(deviceOptions).show();
    }
    $(searchField).data("search-type", $(this).data("type"));
});

/*
 * Update filter container.
 * @param  asset: Selected asset
 */
function containerUpdate(asset){
    var options = $(asset).next('ul').length ? $(asset).next('ul').html() : "";

    $(menuSubContainer).html('<ul class="selected"><li level="'+ $(asset).attr('level') +'" '
    +'onclick="selectAsset(this)"'
    +'asset="' + $(asset).attr('asset') + '">'+ $(asset).html() +'</li>'
    +'<a id="goBack" href="javascript:void(0);" onclick="goBack(this); return false;" '
    +'class="cu-btn" title="Go Back">'
    +'<span class="fw-stack">'
    +'<i class="fw fw-ring fw-stack-2x"></i>'
    +'<i class="fw fw-left-arrow fw-stack-1x"></i>'
    +'</span></a>'
    +'<a id="resetNav" href="javascript:void(0);" onclick="resetNav(); return false;" '
    +'class="cu-btn" title="Reset">'
    +'<span class="fw-stack">'
    +'<i class="fw fw-refresh fw-stack-2x"></i>'
    +'</span></a>'
    +'</ul>' + '<ul class="options">'+ options +'</ul>');

    prevSelected = ($(menuSubContainer + ' ul.selected li').attr('asset'));

    //$(menuSubContainer + ' ul.options li').length == 0 ? $(menuSubContainer + ' ul.options').hide() :
    //    + $(menuSubContainer + ' ul.options').show();

    addingIdentity();
}

/*
 * On asset click selecting filter.
 * @param  asset: Selected asset
 */
function selectAsset(asset){
    var platformType = $(asset).data("type");
    loadOperationBar(platformType);
    loadGroups("type", platformType);
    //$(tagsContainer +' span').each(function(){
    //    if($(this).attr('level') == $(asset).attr('level')){
    //        removeTags(this);
    //    }
    //});

    containerUpdate(asset);

    $(menuSubContainer).show();
    $(menuContainer).hide();

    addTags(asset);
}

/*
 * Adding asset filtering relationship menu child elements.
 */
function addingIdentity(){
    $(menu).find('*').each(function(){
        $(this).attr('rel', 'assetfilter');
    });
    $(tagsContainer).find('*').each(function(){
        $(this).attr('rel', 'assetfilter');
    });
}

/*
 * Filter window go back function.
 * @param  backButton: Filter window back button
 */
function goBack(backButton){
    if($(backButton).prev('li').attr('level') == 1){
        $(menuSubContainer).hide();
        $(menuContainer).show();
    }
    else{
        containerUpdate($(menuContainer + ' [asset='+prevSelected+']').parent('ul').prev('li'));
        $(backButton).remove();
    }
}

/*
 * Filter window reset function.
 * @param  backButton: Filter window reset button
 */
function resetNav(){
    $('#goBack').remove();
    $('#resetNav').remove();
    $(menuSubContainer).hide();
    $(menuContainer).show();
}

/*
 * Find parent function.
 * @param  selection: Current asset parent search tag on search parent loop
 */
function getParent(selection){
    return $(menuContainer + ' [asset=' + $(selection).attr('asset') + ']').closest('ul').prev('li');
}

/*
 * On filter select ad asset & its parents to search field.
 * @param  tag: Selected filter search tag
 */
function addTags(tag){
    var level = $(tag).attr('level'),
        selection = tag,
        content = '<span asset="'+ $(tag).attr('asset') +'" level="'+ $(tag).attr('level') +'" '
            +'onclick="removeTags(this)">' + $(tag).find('.tag-name').html() + ' <b>x</b></span>';

    if(level !== 1) {
        for (var i = 1; i < level; i++) {
            content = '<span asset="'+ $(getParent(selection)).attr('asset') +'" level="'
            + $(getParent(selection)).attr('level')
            +'" onclick="removeTags(this)">' + $(getParent(selection)).find('.tag-name').html() + ' <b>x</b></span>'
            +content;

            selection = getParent(selection);
        }
        $(tagsContainer).html(content);
    }
    addingIdentity();
}

/*
 * On filter tags click remove asset & its child from search field.
 * @param  tag: Selected filter search tag
 */
function removeTags(tag){
    $(tag).nextAll('span').andSelf().each(function(){
        $($(menuContainer + ' [asset='+ $(this).attr('asset') +']')).removeClass('selected');
        $(this).remove();
    });
    unloadOperationBar();
    loadGroups();
}
