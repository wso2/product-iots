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
 
app.dependencies = ['publisher-common'];
app.server = function(ctx) {
    return {
        endpoints: {
            //pages: [{
            //    title: 'Publisher | Servicex Page',
            //    url: 'devices_global',
            //    path: 'device_global.jag'
            //}, {
            //    title: 'Publisher | Servicex Splash page',
            //    url: 'devices',
            //    path: 'device_top_assets.jag'
            //}]
        },
        configs:{
            landingPage:'/assets/deviceType/list',
            disabledAssets:['ebook', 'api', 'wsdl', 'service','policy','proxy','schema','sequence','servicex','uri','wadl','endpoint','swagger','restservice','comments','soapservice', 'license', 'gadget', 'site', 'server']
        }
    }
};
