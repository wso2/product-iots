#!/usr/bin/env python

"""
/**
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
**/
"""

import json
import requests
import urllib
import iotUtils

applicationKey = None
refreshToken = None
filename = "deviceConfig.properties"


# ----------------------------------------------------------------------------------------------
#
# Responsible for generating an access token form the Refresh token.
# -----------------------------------------------------------------------------------------------
class RefreshToken():
    def post(self, url, payload, appKey):
        headers = {'Authorization': 'Basic ' + appKey,
                   'Content-Type': 'application/x-www-form-urlencoded'}
        baseUrl = iotUtils.HTTP_EP + url
        baseUrl = baseUrl.replace('$', '')
        response = requests.post(baseUrl, params=payload, headers=headers)
        return response

    def read_server_conf(self):
        with open(filename, 'r') as outfile:
            conf_file = outfile.readlines()

        return conf_file

    def updateFile(self, response):
        newRefreshToken = response['refresh_token']
        newAccessToken = response['access_token']

        with open(filename, 'r+') as f:
            lines = f.readlines()
            f.seek(0)
            f.truncate()
            for line in lines:
                if line.__contains__("auth-token="):
                    line = "auth-token=" + newAccessToken + "\n"
                if line.__contains__("refresh-token="):
                    line = "refresh-token=" + newRefreshToken + "\n"
                f.write(line)

    def updateTokens(self, ):
        global applicationKey
        global refreshToken
        refreshToken = iotUtils.REFRESH_TOKEN
        applicationKey = iotUtils.APPLICATION_KEY

        params = urllib.urlencode({"grant_type": "refresh_token", "refresh_token": refreshToken,
                                   "scope": "Enroll device"})
        data = self.post("/token", params, applicationKey)
        response = json.loads(data.content)
        self.updateFile(response)
        return response
