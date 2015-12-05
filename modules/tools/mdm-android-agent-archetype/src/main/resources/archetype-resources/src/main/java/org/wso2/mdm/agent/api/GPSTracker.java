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
package org.wso2.mdm.agent.api;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class GPSTracker extends Service implements LocationListener {

	private Location location;
	private double latitude;
	private double longitude;
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
	private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;
	protected LocationManager locationManager;

	public GPSTracker(Context context) {
		locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
		getLocation();
	}

	/**
	 * Function to get device location using GPS.
	 * @return - Device location coordinates.
	 */
	private Location getLocation() {
		try {			
			boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			if (isNetworkEnabled) {
				locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
				                                       MIN_TIME_BW_UPDATES,
				                                       MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
				if (locationManager != null) {
					location = locationManager.getLastKnownLocation(
									                          LocationManager.NETWORK_PROVIDER);
					if (location != null) {
						latitude = location.getLatitude();
						longitude = location.getLongitude();
					}
				}
			}

			if (isGpsEnabled) {
				if (location == null) {
					locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
					                                       MIN_TIME_BW_UPDATES,
					                                       MIN_DISTANCE_CHANGE_FOR_UPDATES,
					                                       this);
					if (locationManager != null) {
						location = locationManager.getLastKnownLocation(
									                          LocationManager.GPS_PROVIDER);
						if (location != null) {
							latitude = location.getLatitude();
							longitude = location.getLongitude();
						}
					}
				}
			}

		} catch (RuntimeException e) {
			Log.e("Location", "No network/GPS Switched off." + e);
		}

		return location;
	}

	/**
	 * Stop using GPS listener.
	 * Calling this function will stop using GPS the agent.
	 */
	public void stopUsingGps() {
		if (locationManager != null) {
			locationManager.removeUpdates(GPSTracker.this);
		}
	}

	/**
	 * Function to get latitude.
	 * @return - Device current latitude.
	 */
	public double getLatitude() {
		if (location != null) {
			latitude = location.getLatitude();
		}
		return latitude;
	}

	/**
	 * Function to get longitude.
	 * @return - Device current longitude.
	 */
	public double getLongitude() {
		if (location != null) {
			longitude = location.getLongitude();
		}
		return longitude;
	}

	@Override
	public void onLocationChanged(Location location) {
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
}