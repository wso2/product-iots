/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.homeautomation.firealarm.manager.api.util;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ResponsePayload {

    private int statusCode;
    private String messageFromServer;
    private Object responseContent;

    public static ResponsePayload.ResponsePayloadBuilder statusCode(int statusCode) {
        ResponsePayload message = new ResponsePayload();
        return message.getBuilder().statusCode(statusCode);
    }

    public static ResponsePayload.ResponsePayloadBuilder messageFromServer(
            String messageFromServer) {
        ResponsePayload message = new ResponsePayload();
        return message.getBuilder().messageFromServer(messageFromServer);
    }

    public static ResponsePayload.ResponsePayloadBuilder responseContent(String responseContent) {
        ResponsePayload message = new ResponsePayload();
        return message.getBuilder().responseContent(responseContent);
    }

    @XmlElement
    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    @XmlElement
    public String getMessageFromServer() {
        return messageFromServer;
    }

    public void setMessageFromServer(String messageFromServer) {
        this.messageFromServer = messageFromServer;
    }

    @XmlElement
    public Object getResponseContent() {
        return responseContent;
    }

    public void setResponseContent(Object responseContent) {
        this.responseContent = responseContent;
    }

    private ResponsePayload.ResponsePayloadBuilder getBuilder() {
        return new ResponsePayload.ResponsePayloadBuilder();
    }

    public class ResponsePayloadBuilder {

        private int statusCode;
        private String messageFromServer;
        private Object responseContent;

        public ResponsePayloadBuilder statusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public ResponsePayloadBuilder messageFromServer(String messageFromServer) {
            this.messageFromServer = messageFromServer;
            return this;
        }

        public ResponsePayloadBuilder responseContent(String responseContent) {
            this.responseContent = responseContent;
            return this;
        }

        public ResponsePayload build() {
            ResponsePayload payload = new ResponsePayload();
            payload.setStatusCode(statusCode);
            payload.setMessageFromServer(messageFromServer);
            payload.setResponseContent(responseContent);
            return payload;
        }
    }

}
