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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.testng.Assert;

/**
 * This class contains methods to make assertions easier and which are not available out of the box, from testng.
 */
public class AssertUtil {
    /**
     * This can be used to compare if to json strings are matched or not.
     *
     * @param expectedJsonPayload the expected json string.
     * @param realPayload         real json string.
     * @param mustMatch           If the real and expected must match, in order to become the test successful or not.
     */
    public static void jsonPayloadCompare(String expectedJsonPayload, String realPayload, boolean mustMatch) {
        JsonElement jsonElement = new JsonParser().parse(expectedJsonPayload);
        JsonObject expectedPayloadObject = jsonElement.getAsJsonObject();
        jsonElement = new JsonParser().parse(realPayload);
        JsonObject realPayloadObject = jsonElement.getAsJsonObject();
        if (mustMatch) {
            Assert.assertTrue(realPayloadObject.equals(expectedPayloadObject));
        } else {
            Assert.assertFalse(realPayloadObject.equals(expectedPayloadObject));
        }
    }
}
