/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
app.dependencies = ['store-common'];
app.server = function(ctx) {
    return {
        endpoints: {
            pages: [{
                title: 'Store | Analytics',
                url: 'analytics',
                path: 'device-analytics.jag',
                secured:true
            },{
                title: 'Store | My Devices Page',
                url: 'my-items',
                path: 'my_items.jag',
                secured:true,
                permission:'APP_MYITEMS'
            }],
            apis: [{
                url: 'stats',
                path: 'stats-api.jag',
                secured:true
            }]
        },
        configs: {
            disabledAssets: ['ebook', 'api', 'wsdl', 'servicex','policy','proxy','schema','sequence','uri','wadl','endpoint', 'swagger','restservice','comments','soapservice', 'service', 'license', 'gadget', 'site','server']
        }
    }
};

app.pageHandlers = function (ctx) {
    return {
        onPageLoad: function () {
            if ((ctx.isAnonContext) && (ctx.endpoint.secured)) {
                ctx.res.sendRedirect(ctx.appContext + '/login');
                return false;
            }
            return true;
        }
    }
};

app.apiHandlers = function (ctx) {
    return {
        onApiLoad: function () {
            if ((ctx.isAnonContext) && (ctx.endpoint.secured)) {
                ctx.res.status = '401';
                ctx.res.sendRedirect(ctx.appContext + '/login');
                return false;
            }
            return true;
        }
    }
};

app.renderer = function(ctx) {
    var decoratorApi = require('/modules/page-decorators.js').pageDecorators;
    return {
        pageDecorators: {
            navigationBar: function(page) {
                return decoratorApi.navigationBar(ctx, page, this);
            },
            searchBar: function(page) {
                return decoratorApi.searchBar(ctx, page, this);
            },
            authenticationDetails: function(page) {
                return decoratorApi.authenticationDetails(ctx, page, this);
            },
            recentAssetsOfActivatedTypes: function(page) {
                return decoratorApi.recentAssetsOfActivatedTypes(ctx, page, this);
            },
            popularAssets:function(page){
                return decoratorApi.popularAssets(ctx,page,this);
            }
        }
    }
};