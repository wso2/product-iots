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

var temperature = 0;
var coffee_amount = 0;

function updateCoffee(newValue){
    var coffee_level = document.getElementById("coffee_level");
    coffee_level.innerHTML = newValue + "%";
    coffee_amount =newValue;

    var coffee = document.getElementById("water");
    if(newValue == 0){
        coffee.style.height= (newValue*3) + 'px';
    }else{
        coffee.style.height= (newValue*3) - 3 + 'px';
    }
}

function updateTemperature(newValue){
    temperature = newValue;
    var temperature_level = document.getElementById("temperature_level");
    temperature_level.innerHTML = newValue + " C";
}