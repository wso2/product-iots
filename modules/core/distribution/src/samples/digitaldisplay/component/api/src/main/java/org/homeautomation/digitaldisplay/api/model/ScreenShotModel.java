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

package org.homeautomation.digitaldisplay.api.model;

public class ScreenShotModel {

    private String[] screenShotData;
    private int length;

    public ScreenShotModel(){

    }

    public ScreenShotModel(String[] screenShotData , int length){
        this.screenShotData = screenShotData;
        this.length = length;
    }

    public void setScreenShotData(String[] screrenShotData){
        this.screenShotData = screenShotData;
    }

    public void setLength(int length){
        this.length = length;
    }

    public String[] getScreenShotData(){
        return this.screenShotData;
    }

    public int getLength(){
        return this.length;
    }

}
