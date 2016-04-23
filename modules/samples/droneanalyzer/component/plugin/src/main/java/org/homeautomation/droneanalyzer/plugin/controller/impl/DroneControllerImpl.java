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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.homeautomation.droneanalyzer.plugin.controller.impl;


import org.homeautomation.droneanalyzer.plugin.controller.DroneController;

public class DroneControllerImpl implements DroneController {

    @Override
    public boolean takeoff() {
        return false;
    }

    @Override
    public boolean land() {
        return false;
    }

    @Override
    public boolean up(double speed, double duration) {
        return false;
    }

    @Override
    public boolean down(double speed, double duration) {
        return false;
    }

    @Override
    public boolean left(double speed, double duration) {
        return false;
    }

    @Override
    public boolean right(double speed, double duration) {
        return false;
    }

    @Override
    public boolean front(double speed, double duration) {
        return false;
    }

    @Override
    public boolean back(double speed, double duration) {
        return false;
    }

    @Override
    public boolean clockwise(double speed, double duration) {
        return false;
    }

    @Override
    public boolean conterClockwise(double speed, double duration) {
        return false;
    }
}
