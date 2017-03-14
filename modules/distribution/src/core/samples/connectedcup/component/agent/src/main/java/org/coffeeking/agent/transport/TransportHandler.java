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

package org.coffeeking.agent.transport;

/**
 * This interface consists of the core functionality related to the transport between any device and the server. The
 * interface is an abstraction, regardless of the underlying protocol used for the transport. Implementation of this
 * interface by any class that caters a specific protocol (ex: HTTP, XMPP, MQTT, CoAP) would ideally have methods
 * specific to the protocol used for communication and thees methods that implement the logic related to the devices
 * using the protocol.
 *
 * @param <T> a message type specific to the protocol implemented
 */
public interface TransportHandler<T> {
    int DEFAULT_TIMEOUT_INTERVAL = 5000;      // millis ~ 10 sec

    void connect();

    boolean isConnected();

    void processIncomingMessage() throws TransportHandlerException;

    void processIncomingMessage(T message) throws TransportHandlerException;

    void processIncomingMessage(T message, String... messageParams) throws TransportHandlerException;

    void publishDeviceData() throws TransportHandlerException;

    void publishDeviceData(T publishData) throws TransportHandlerException;

    void publishDeviceData(String... publishData) throws TransportHandlerException;

    void disconnect();
}
