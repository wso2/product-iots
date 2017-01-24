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

package org.wso2.iot.integration.common;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URL;

/**
 * This class is used to load the necessary payloads from payload files for integration tests.
 */
public class PayloadGenerator {

    private static final String PAYLOAD_LOCATION = "payloads/";
    private static JsonParser parser = new JsonParser();

    public static JsonObject getJsonPayload(String fileName, String method)
            throws FileNotFoundException {
        URL url = Thread.currentThread().getContextClassLoader().getResource(PAYLOAD_LOCATION + fileName);
        JsonObject jsonObject = parser.parse(new FileReader(url.getPath())).getAsJsonObject();
        return jsonObject.get(method).getAsJsonObject();
    }

    public static String getJsonPayloadToString(String fileName) throws IOException {
        URL url = Thread.currentThread().getContextClassLoader().getResource(PAYLOAD_LOCATION + fileName);
        FileInputStream fisTargetFile = new FileInputStream(new File(url.getPath()));
        String returnString = IOUtils.toString(fisTargetFile, Constants.UTF8);
        return returnString;
    }
}