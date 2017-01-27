/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.mdm.qsg;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.mdm.qsg.dto.EMMQSGConfig;
import org.wso2.mdm.qsg.dto.HTTPResponse;
import org.wso2.mdm.qsg.dto.MobileApplication;
import org.wso2.mdm.qsg.utils.Constants;
import org.wso2.mdm.qsg.utils.HTTPInvoker;
import org.wso2.mdm.qsg.utils.QSGUtils;

import java.io.File;
import java.util.HashMap;

/**
 * This class holds the app-mgt related operations.
 */
public class AppOperations {

    private static String appmPublisherMobileBinariesUrl = "/api/appm/publisher/v1.1/apps/mobile/binaries";
    private static String appmPublisherResourcesUrl = "/api/appm/publisher/v1.1/apps/static-contents?appType=mobileapp";
    private static String appmPublisherAppsUrl = "/api/appm/publisher/v1.1/apps/mobileapp";


    public static MobileApplication uploadApplication(String platform, String appName, String appContentType) {

        String appUploadEndpoint = EMMQSGConfig.getInstance().getEmmHost() + appmPublisherMobileBinariesUrl;
        String filePath = "apps" + File.separator + platform + File.separator + appName;
        HTTPResponse httpResponse = HTTPInvoker.uploadFile(appUploadEndpoint, filePath, appContentType);
        if (Constants.HTTPStatus.OK == httpResponse.getResponseCode()) {
            JSONObject appMeta = null;
            MobileApplication application = new MobileApplication();
            try {
                appMeta = (JSONObject) new JSONParser().parse(httpResponse.getResponse());
                application.setPackageId((String) appMeta.get("package"));
                application.setAppId(QSGUtils.getResourceId((String) appMeta.get("path")));
                application.setVersion((String) appMeta.get("version"));
                application.setPlatform(platform);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return application;
        }
        return null;
    }

    public static MobileApplication getPublicApplication(String packageId, String version, String platform) {
        MobileApplication application = new MobileApplication();
        application.setVersion(version);
        application.setPackageId(packageId);
        application.setPlatform(platform);
        return application;
    }

    private static String uploadAsset(String path) {

        String resUploadEndpoint = EMMQSGConfig.getInstance().getEmmHost() + appmPublisherResourcesUrl;
        HTTPResponse httpResponse = HTTPInvoker.uploadFile(resUploadEndpoint, path, "image/jpeg");
        if (Constants.HTTPStatus.OK == httpResponse.getResponseCode()) {
            JSONObject resp = null;
            try {
                resp = (JSONObject) new JSONParser().parse(httpResponse.getResponse());
                return (String) resp.get("id");
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static MobileApplication uploadAssets(String platform, MobileApplication application) {

        String assetDir = "apps" + File.separator + platform + File.separator + "images";
        //Upload the icon file
        String imgFile = assetDir + File.separator + "icon.jpg";
        String uploadPath = uploadAsset(imgFile);
        if (uploadPath != null && !uploadPath.isEmpty()) {
            application.setIcon(uploadPath);
        } else {
            System.out.println("Unable to upload the app icon file.");
            return null;
        }

        //Upload the banner file
        imgFile = assetDir + File.separator + "banner.jpg";
        uploadPath = uploadAsset(imgFile);
        if (uploadPath != null && !uploadPath.isEmpty()) {
            application.setBanner(uploadPath);
        } else {
            System.out.println("Unable to upload the app banner file.");
            return null;
        }

        //Upload the screenshot1 file
        imgFile = assetDir + File.separator + "screen1.jpg";
        uploadPath = uploadAsset(imgFile);
        if (uploadPath != null && !uploadPath.isEmpty()) {
            application.setScreenshot1(uploadPath);
        } else {
            System.out.println("Unable to upload the app screenshot1 file.");
            return null;
        }

        //Upload the screenshot2 file
        imgFile = assetDir + File.separator + "screen2.jpg";
        uploadPath = uploadAsset(imgFile);
        if (uploadPath != null && !uploadPath.isEmpty()) {
            application.setScreenshot2(uploadPath);
        } else {
            System.out.println("Unable to upload the app screenshot2 file.");
            return null;
        }

        //Upload the screenshot3 file
        imgFile = assetDir + File.separator + "screen3.jpg";
        uploadPath = uploadAsset(imgFile);
        if (uploadPath != null && !uploadPath.isEmpty()) {
            application.setScreenshot3(uploadPath);
        } else {
            System.out.println("Unable to upload the app screenshot3 file.");
            return null;
        }
        return application;
    }

    public static boolean addApplication(String name, MobileApplication mblApp, boolean isEnterpriseApp) {
        HashMap<String, String> headers = new HashMap<String, String>();

        String appEndpoint = EMMQSGConfig.getInstance().getEmmHost() + appmPublisherAppsUrl;
        //Set the application payload
        JSONObject application = new JSONObject();
        application.put("name", name);
        application.put("description", "Sample application");
        application.put("type", "enterprise");
        //Set appMeta data
        JSONObject appMeta = new JSONObject();
        appMeta.put("package", mblApp.getPackageId());
        appMeta.put("version", mblApp.getVersion());
        if (isEnterpriseApp) {
            application.put("marketType", "enterprise");
            appMeta.put("path", mblApp.getAppId());
        } else {
            application.put("marketType", "public");
        }
        application.put("provider", "admin");
        application.put("displayName", name);
        application.put("category", "Business");
        application.put("thumbnailUrl", mblApp.getIcon());
        application.put("version", mblApp.getVersion());
        application.put("banner", mblApp.getBanner());
        application.put("platform", mblApp.getPlatform());
        application.put("appType", mblApp.getPlatform());
        //application.put("appUrL", mblApp.getAppId());
        application.put("mediaType", "application/vnd.wso2-mobileapp+xml");

        //Set screenshots
        JSONArray screenshots = new JSONArray();
        screenshots.add(mblApp.getScreenshot1());
        screenshots.add(mblApp.getScreenshot2());
        screenshots.add(mblApp.getScreenshot3());
        application.put("appmeta", appMeta);
        application.put("screenshots", screenshots);

        //Set the headers
        headers.put(Constants.Header.CONTENT_TYPE, Constants.ContentType.APPLICATION_JSON);
        HTTPResponse
                httpResponse =
                HTTPInvoker.sendHTTPPostWithOAuthSecurity(appEndpoint, application.toJSONString(), headers);
        if (Constants.HTTPStatus.OK == httpResponse.getResponseCode()) {
            return true;
        }
        return false;
    }
}
