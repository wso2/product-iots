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

package org.wso2.mdm.integration.device.policy;

import junit.framework.Assert;
import org.apache.commons.httpclient.HttpStatus;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.mdm.integration.common.*;

/**
 * This contain tests to check policy endpoints supported by Android.
 * Test are executed against a previously enrolled device.
 */
public class AndroidPolicy extends TestBase {

    private RestClient client;

    @BeforeTest(alwaysRun = true, groups = {Constants.AndroidEnrollment.ENROLLMENT_GROUP})
    public void initTest() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String accessTokenString = "Bearer " + OAuthUtil.getOAuthToken(backendHTTPURL, backendHTTPSURL);
        this.client = new RestClient(backendHTTPURL, Constants.APPLICATION_JSON, accessTokenString);
    }

    @Test(groups = Constants.AndroidPolicy.POLICY_GROUP, description = "Test Android getEffectivePolicy.")
    public void testGetEffectivePolicy() throws Exception {
        HttpResponse response = client.get(Constants.AndroidPolicy.POLICY_ENDPOINT + Constants.DEVICE_ID);
        AssertUtil.jsonPayloadCompare(PayloadGenerator.getJsonPayload(
                                              Constants.AndroidPolicy.POLICY_RESPONSE_PAYLOAD_FILE_NAME,
                                              Constants.AndroidPolicy.GET_EFFECTIVE_POLICY).toString(),
                                      response.getData().toString(), true);
    }
}
