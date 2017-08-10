/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.raspberrypicodebased.api.util;

import org.wso2.carbon.apimgt.application.extension.constants.ApiApplicationConstants;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.utils.CarbonUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.wso2.carbon.core.util.Utils;
import org.json.JSONObject;
import org.apache.commons.codec.binary.Base64;

/**
 * This is used to create a zip file that includes the necessary configuration required for the agent.
 */
public class ZipUtil {

    private static final String LOCALHOST = "localhost";
    private static final String HTTPS_PROTOCOL_URL = "https://${iot.gateway.host}:${iot.gateway.https.port}";
    private static final String HTTP_PROTOCOL_URL = "http://${iot.gateway.host}:${iot.gateway.http.port}";
    private static final String CONFIG_TYPE = "general";
    private static final String DEFAULT_MQTT_ENDPOINT = "tcp://${mqtt.broker.host}:${mqtt.broker.port}";
    public static final String HOST_NAME = "HostName";

    public ZipArchive createZipFile(String owner, String tenantDomain, String deviceType,
                                    String deviceId, String deviceName, String token,
                                    String refreshToken, String apiApplicationKey) throws DeviceManagementException {

        String sketchFolder = "repository" + File.separator + "resources" + File.separator + "sketches";
        String archivesPath = CarbonUtils.getCarbonHome() + File.separator + sketchFolder + File.separator + "archives" +
                File.separator + deviceId;
        String templateSketchPath = sketchFolder + File.separator + deviceType;
        String iotServerIP;

        try {
            iotServerIP = getServerUrl();
            String httpsServerEP = Utils.replaceSystemProperty(HTTPS_PROTOCOL_URL);
            String httpServerEP = Utils.replaceSystemProperty(HTTP_PROTOCOL_URL);
            String mqttEndpoint = Utils.replaceSystemProperty(DEFAULT_MQTT_ENDPOINT);
            if (mqttEndpoint.contains(LOCALHOST)) {
                mqttEndpoint = mqttEndpoint.replace(LOCALHOST, iotServerIP);
                httpsServerEP = httpsServerEP.replace(LOCALHOST, iotServerIP);
                httpServerEP = httpServerEP.replace(LOCALHOST, iotServerIP);
            }
            String base64EncodedApplicationKey = getBase64EncodedAPIAppKey(apiApplicationKey).trim();

            Map<String, String> contextParams = new HashMap<>();
            contextParams.put("SERVER_NAME", APIUtil.getTenantDomainOftheUser());
            contextParams.put("DEVICE_OWNER", owner);
            contextParams.put("DEVICE_ID", deviceId);
            contextParams.put("DEVICE_NAME", deviceName);
            contextParams.put("HTTPS_EP", httpsServerEP);
            contextParams.put("HTTP_EP", httpServerEP);
            contextParams.put("APIM_EP", httpsServerEP);
            contextParams.put("MQTT_EP", mqttEndpoint);
            contextParams.put("DEVICE_TOKEN", token);
            contextParams.put("DEVICE_REFRESH_TOKEN", refreshToken);
            contextParams.put("API_APPLICATION_KEY", base64EncodedApplicationKey);

            ZipArchive zipFile;
            zipFile = getSketchArchive(archivesPath, templateSketchPath, contextParams, deviceName);
            return zipFile;
        } catch (IOException e) {
            throw new DeviceManagementException("Zip File Creation Failed", e);
        }
    }

    private String getBase64EncodedAPIAppKey(String apiAppCredentialsAsJSONString) {

        JSONObject jsonObject = new JSONObject(apiAppCredentialsAsJSONString);
        String consumerKey = jsonObject.get(ApiApplicationConstants.OAUTH_CLIENT_ID).toString();
        String consumerSecret = jsonObject.get(ApiApplicationConstants.OAUTH_CLIENT_SECRET).toString();
        String stringToEncode = consumerKey + ":" + consumerSecret;
        return Base64.encodeBase64String(stringToEncode.getBytes());
    }

    public static String getServerUrl() {
        try {
            return org.apache.axis2.util.Utils.getIpAddress();
        } catch (SocketException e) {
            return "localhost";
        }
    }

    public static ZipArchive getSketchArchive(String archivesPath, String templateSketchPath, Map contextParams
            , String zipFileName)
            throws DeviceManagementException, IOException {
        String sketchPath = CarbonUtils.getCarbonHome() + File.separator + templateSketchPath;
        FileUtils.deleteDirectory(new File(archivesPath));//clear directory
        FileUtils.deleteDirectory(new File(archivesPath + ".zip"));//clear zip
        if (!new File(archivesPath).mkdirs()) { //new dir
            String message = "Could not create directory at path: " + archivesPath;
            throw new DeviceManagementException(message);
        }
        zipFileName = zipFileName + ".zip";
        try {
            Map<String, List<String>> properties = getProperties(sketchPath + File.separator + "sketch" + ".properties");
            List<String> templateFiles = properties.get("templates");

            for (String templateFile : templateFiles) {
                parseTemplate(templateSketchPath + File.separator + templateFile, archivesPath + File.separator + templateFile,
                              contextParams);
            }

            templateFiles.add("sketch.properties");         // ommit copying the props file
            copyFolder(new File(sketchPath), new File(archivesPath), templateFiles);
            createZipArchive(archivesPath);
            FileUtils.deleteDirectory(new File(archivesPath));
            File zip = new File(archivesPath + ".zip");
            return new ZipArchive(zipFileName, zip);
        } catch (IOException ex) {
            throw new DeviceManagementException(
                    "Error occurred when trying to read property " + "file sketch.properties", ex);
        }
    }

    private static Map<String, List<String>> getProperties(String propertyFilePath) throws IOException {
        Properties prop = new Properties();
        InputStream input = null;

        try {

            input = new FileInputStream(propertyFilePath);

            // load a properties file
            prop.load(input);
            Map<String, List<String>> properties = new HashMap<String, List<String>>();

            String templates = prop.getProperty("templates");
            List<String> list = new ArrayList<String>(Arrays.asList(templates.split(",")));
            properties.put("templates", list);

            final String filename = prop.getProperty("zipfilename");
            list = new ArrayList<String>() {{
                add(filename);
            }};
            properties.put("zipfilename", list);
            return properties;

        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private static void parseTemplate(String srcFile, String dstFile, Map contextParams) throws IOException {
        //read from file
        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(srcFile);
            outputStream = new FileOutputStream(dstFile);
            String content = IOUtils.toString(inputStream, StandardCharsets.UTF_8.toString());
            Iterator iterator = contextParams.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry mapEntry = (Map.Entry) iterator.next();
                content = content.replaceAll("\\{" + mapEntry.getKey() + "\\}", mapEntry.getValue().toString());
            }
            IOUtils.write(content, outputStream, StandardCharsets.UTF_8.toString());
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    private static void copyFolder(File src, File dest, List<String> excludeFileNames) throws IOException {

        if (src.isDirectory()) {
            //if directory not exists, create it
            if (!dest.exists() && !dest.mkdirs()) {
                String message = "Could not create directory at path: " + dest;
                throw new IOException(message);
            }
            //list all the directory contents
            String files[] = src.list();

            if (files == null) {
                return;
            }

            for (String file : files) {
                //construct the src and dest file structure
                File srcFile = new File(src, file);
                File destFile = new File(dest, file);
                //recursive copy
                copyFolder(srcFile, destFile, excludeFileNames);
            }

        } else {
            for (String fileName : excludeFileNames) {
                if (src.getName().equals(fileName)) {
                    return;
                }
            }
            //if file, then copy it
            //Use bytes stream to support all file types
            InputStream in = null;
            OutputStream out = null;

            try {
                in = new FileInputStream(src);
                out = new FileOutputStream(dest);

                byte[] buffer = new byte[1024];

                int length;
                //copy the file content in bytes
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            } finally {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            }
        }
    }

    private static boolean createZipArchive(String srcFolder) throws IOException {
        BufferedInputStream origin = null;
        ZipOutputStream out = null;

        try {
            final int BUFFER = 2048;
            FileOutputStream dest = new FileOutputStream(new File(srcFolder + ".zip"));
            out = new ZipOutputStream(new BufferedOutputStream(dest));
            byte data[] = new byte[BUFFER];
            File subDir = new File(srcFolder);
            String subdirList[] = subDir.list();
            if (subdirList == null) {
                return false;
            }
            for (String sd : subdirList) {
                // get a list of files from current directory
                File f = new File(srcFolder + "/" + sd);
                if (f.isDirectory()) {
                    String files[] = f.list();

                    if (files == null) {
                        return false;
                    }

                    for (int i = 0; i < files.length; i++) {
                        FileInputStream fi = new FileInputStream(srcFolder + "/" + sd + "/" + files[i]);
                        origin = new BufferedInputStream(fi, BUFFER);
                        ZipEntry entry = new ZipEntry(sd + "/" + files[i]);
                        out.putNextEntry(entry);
                        int count;
                        while ((count = origin.read(data, 0, BUFFER)) != -1) {
                            out.write(data, 0, count);
                            out.flush();
                        }

                    }
                } else //it is just a file
                {
                    FileInputStream fi = new FileInputStream(f);
                    origin = new BufferedInputStream(fi, BUFFER);
                    ZipEntry entry = new ZipEntry(sd);
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER)) != -1) {
                        out.write(data, 0, count);
                        out.flush();
                    }
                }
            }
            out.flush();
        } finally {
            if (origin != null) {
                origin.close();
            }
            if (out != null) {
                out.close();
            }
        }
        return true;
    }
}
