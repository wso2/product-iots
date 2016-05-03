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

package org.coffeeking.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.coffeeking.api.util.APIUtil;
import org.coffeeking.api.util.SensorRecord;
import org.coffeeking.connectedcup.plugin.constants.ConnectedCupConstants;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationException;
import org.wso2.carbon.analytics.dataservice.commons.SORT;
import org.wso2.carbon.analytics.dataservice.commons.SortByField;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

public class ConnectedCupControllerServiceImpl implements ConnectedCupControllerService {

    private static Log log = LogFactory.getLog(ConnectedCupControllerServiceImpl.class);

    @Path("device/ordercoffee")
    @POST
    public Response orderCoffee(@QueryParam("deviceId") String deviceId) {
        try {
            if (!APIUtil.getDeviceAccessAuthorizationService().isUserAuthorized(new DeviceIdentifier(deviceId,
                                                 ConnectedCupConstants.DEVICE_TYPE))) {
                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
            }
            log.info("Coffee ordered....!");
            if (log.isDebugEnabled()) {
                log.debug("Sending request to read liquid level value of device [" + deviceId + "] via MQTT");
            }
            return Response.ok().entity("Coffee ordered.").build();
        } catch (DeviceAccessAuthorizationException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    @Path("stats/{deviceId}/sensors/{sensorName}")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public Response getDeviceStats(@PathParam("deviceId") String deviceId, @PathParam("sensorName") String sensor,
                                               @QueryParam("from") long from, @QueryParam("to") long to) {
        String fromDate = String.valueOf(from);
        String toDate = String.valueOf(to);
        String query = " deviceId:" + deviceId + " AND deviceType:" +
                ConnectedCupConstants.DEVICE_TYPE + " AND time : [" + fromDate + " TO " + toDate + "]";
        String sensorTableName = getSensorEventTableName(sensor);

        try {
            if (!APIUtil.getDeviceAccessAuthorizationService().isUserAuthorized(new DeviceIdentifier(deviceId,
                       ConnectedCupConstants.DEVICE_TYPE))) {
                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
            }
            List<SensorRecord> sensorDatas;
            List<SortByField> sortByFields = new ArrayList<>();
            SortByField sortByField = new SortByField("time", SORT.ASC, false);
            sortByFields.add(sortByField);
            sensorDatas = APIUtil.getAllEventsForDevice(sensorTableName, query, sortByFields);
            return Response.ok().entity(sensorDatas).build();
        } catch (AnalyticsException e) {
            String errorMsg = "Error on retrieving stats on table " + sensorTableName + " with query " + query;
            log.error(errorMsg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(errorMsg).build();
        } catch (DeviceAccessAuthorizationException e) {
            log.error(e.getErrorMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    /**
     * get the event table from the sensor name.
     */
    private String getSensorEventTableName(String sensorName) {
        String sensorEventTableName;
        switch (sensorName) {
            case "temperature":
                sensorEventTableName = "DEVICE_TEMPERATURE_SUMMARY";
                break;
            case "coffeelevel":
                sensorEventTableName = "DEVICE_COFFEELEVEL_SUMMARY";
                break;
            default:
                sensorEventTableName = "";
        }
        return sensorEventTableName;
    }

}
