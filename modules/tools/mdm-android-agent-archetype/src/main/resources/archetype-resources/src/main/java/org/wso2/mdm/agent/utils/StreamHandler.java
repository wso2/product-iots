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

package org.wso2.mdm.agent.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.util.Log;

public class StreamHandler {

	/**
	 * Close a ByteArrayOutputStream passed in.
	 * 
	 * @param stream
	 *            - ByteArrayOutputStream to be closed.
	 */
	public static void closeOutputStream(OutputStream stream, String tag) {
		if (stream != null) {
			try {
				stream.close();
			} catch (IOException e) {
				Log.e(tag, "Exception occured when closing ByteArrayOutputStream." + e);
			}
		}
	}

	/**
	 * Close a InputStream passed in.
	 * 
	 * @param stream
	 *            - InputStream to be closed.
	 */
	public static void closeInputStream(InputStream stream, String tag) {
		if (stream != null) {
			try {
				stream.close();
			} catch (IOException e) {
				Log.e(tag, "Exception occured when closing InputStream." + e);
			}
		}
	}

	/**
	 * Close a InputStream passed in.
	 * 
	 * @param stream
	 *            - InputStream to be closed.
	 */
	public static void closeBufferedReader(BufferedReader stream, String tag) {
		if (stream != null) {
			try {
				stream.close();
			} catch (IOException e) {
				Log.e(tag, "Exception occured when closing BufferedReader." + e);
			}
		}
	}

}
