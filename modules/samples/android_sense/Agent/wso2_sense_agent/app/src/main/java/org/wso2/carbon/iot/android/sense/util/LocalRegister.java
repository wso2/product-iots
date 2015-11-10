/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.wso2.carbon.iot.android.sense.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;

public class LocalRegister {

    private static final String SENSE_SHARED_PREFERENCES = "senseSharedPreferences";
    private static final String USERNAME_KEY = "usernameKey";
    private static final String DEVICE_ID_KEY = "deviceIdKey";
    private static final String SERVER_HOST_KEY = "serverHostKey";
    private static boolean exists = false;
    private static String username;
    private static String deviceId;
    private static String serverURL;

    public static boolean isExist(Context context){
        if(!exists) {
            SharedPreferences sharedpreferences = context.getSharedPreferences(SENSE_SHARED_PREFERENCES, Context.MODE_PRIVATE);
            String username = sharedpreferences.getString(USERNAME_KEY, "");
            String deviceId = sharedpreferences.getString(DEVICE_ID_KEY, "");
            exists = (username != null && !username.isEmpty() && deviceId != null && !deviceId.isEmpty());
        }
        return exists;
    }

    public static void setExist(boolean status){
        exists = status;
    }


    public static void addUsername(Context context, String username){
        SharedPreferences sharedpreferences = context.getSharedPreferences(SENSE_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(USERNAME_KEY, username);
        editor.commit();
        LocalRegister.username = username;
    }

    public static String getUsername(Context context){
        if(LocalRegister.username==null || username.isEmpty()) {
            SharedPreferences sharedpreferences = context.getSharedPreferences(SENSE_SHARED_PREFERENCES, Context.MODE_PRIVATE);
            LocalRegister.username = sharedpreferences.getString(USERNAME_KEY, "");
            //TODO Throw exception
        }
        return LocalRegister.username;
    }

    public static void removeUsername(Context context){
        SharedPreferences sharedpreferences = context.getSharedPreferences(SENSE_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.clear();
        editor.remove(USERNAME_KEY);
        editor.commit();
        LocalRegister.username = null;
    }

    public static void addDeviceId(Context context, String deviceId){
        SharedPreferences sharedpreferences = context.getSharedPreferences(SENSE_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(DEVICE_ID_KEY, deviceId);
        editor.commit();
        LocalRegister.deviceId = deviceId;
    }



    public static void removeDeviceId(Context context){
        SharedPreferences sharedpreferences = context.getSharedPreferences(SENSE_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.remove(DEVICE_ID_KEY);
        editor.clear();
        editor.commit();
        LocalRegister.deviceId = null;
    }

    public static String getDeviceId(Context context){
        if(LocalRegister.deviceId==null || LocalRegister.deviceId.isEmpty()) {
            SharedPreferences sharedpreferences = context.getSharedPreferences(SENSE_SHARED_PREFERENCES, Context.MODE_PRIVATE);
            LocalRegister.deviceId = sharedpreferences.getString(DEVICE_ID_KEY, "");
            //TODO Throw exception
        }
        return LocalRegister.deviceId;
    }

    public static void addServerURL(Context context, String host){
        SharedPreferences sharedpreferences = context.getSharedPreferences(SENSE_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(SERVER_HOST_KEY, host);
        editor.commit();
        LocalRegister.serverURL = host;
    }

    public static void removeServerURL(Context context){
        SharedPreferences sharedpreferences = context.getSharedPreferences(SENSE_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.remove(SERVER_HOST_KEY);
        editor.clear();
        editor.commit();
        LocalRegister.serverURL = null;
    }

    public static String getServerURL(Context context){
        if(LocalRegister.serverURL ==null || LocalRegister.serverURL.isEmpty()) {
            SharedPreferences sharedpreferences = context.getSharedPreferences(SENSE_SHARED_PREFERENCES, Context.MODE_PRIVATE);
            LocalRegister.serverURL = sharedpreferences.getString(SERVER_HOST_KEY, "");
            //TODO Throw exception
        }
        return LocalRegister.serverURL;
    }

    public static String getServerHost(Context context){

        URL url = null;
        String urlString = getServerURL(context);
        try {
            url = new URL(urlString);
            return url.getHost();
        } catch (MalformedURLException e) {
            Log.e("Host " , "Invalid urlString :" + urlString);
            return null;
        }


    }
}
