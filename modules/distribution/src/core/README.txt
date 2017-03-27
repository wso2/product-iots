WSO2 IoT Server 3.1.0
==============================================================
Welcome to the WSO2 IoT Server (IoTS) 3.1.0 release.

WSO2 IoT Server (IoTS) provides the essential capabilities required to implement a scalable server-side IoT Platform. These capabilities
involve device management, API/App management for devices, analytics, customizable web portals, transport extensions for MQTT, XMPP
and much more. WSO2 IoTS contains sample device agent implementations for well-known development boards, such as Arduino UNO, Raspberry Pi,
Android, iOS, Windows and Virtual agents that demonstrate various capabilities. Furthermore, WSO2 IoTS is released under
the Apache Software License Version 2.0, one of the most business-friendly licenses available today.


Key Features
==================================
See the online WSO2 IoT documentation for more information on product features:
https://docs.wso2.com/display/IoTS300/About+this+Release


Installation & Running
==================================

Running the Integrator
==================================
1. Extract  wso2iot-3.1.0.zip and go to the extracted directory/bin.
2. Run iot-server.sh or iot-server.bat.
3. Point your favourite browser to  https://localhost:9443/devicemgt in order to see the available device types and operations.
4. Use the following username and password to login
   username : admin
   password : admin
5. Navigate to https://localhost:9443/api-store to see the available device APIs. You can subscribe to these APIs with the default application (or by creating a new application).
   In the API Store, go to my subscriptions and locate the client ID and secret, which can be used to invoke these APIs.


Running other runtimes individually (Analytics, Broker)
========================================================
1. Extract wso2iot-3.1.0.zip and go to the extracted directory.
2. Go to wso2iot-3.1.0/wso2 directory.
3. Go to appropriate runtime directory (analytics/broker) /bin.
4. Execute wso2server.sh or wso2server.bat.
3. Access the url related to the required runtime. (For example, use https://localhost:9445/carbon for the analytics runtime.)

Running all runtimes (Integrator, Analytics, Broker, Business-Process)
==================================================================

1. Extract  wso2iot-3.1.0.zip and go to the extracted directory/bin.
2. Run start-all.sh or start-all.bat.
3. Access appropriate url for the related runtime. (For example, use  https://localhost:9443/devicemgt for the IoT Server runtime)


System Requirements
==================================

1. Minimum memory - 4GB
2. The portal app requires full Javascript enablement on the Web browser


WSO2 EI distribution directory
=============================================

 - bin
	  Contains various scripts .sh & .bat scripts

    - database
	  Contains the database

    - dbscripts
	  Contains all the database scripts

    - lib
	  Contains the basic set of libraries required to startup IoT Server
	  in standalone mode

    - repository
	  The repository where services and modules deployed in WSO2 IoT.
	  are stored.

	- conf
	  Contains configuration files specific to IoT.

	- logs
	  Contains all log files created during execution of IoT.

    - resources
	  Contains additional resources that may be required, including sample
	  configuration and sample resources

    - samples
	  Contains some sample services and client applications that demonstrate
	  the functionality and capabilities of WSO2 IoT.

    - tmp
	  Used for storing temporary files, and is pointed to by the
	  java.io.tmpdir System property

    - LICENSE.txt
	  Apache License 2.0 and the relevant other licenses under which
	  WSO2 EI is distributed.

    - README.txt
	  This document.

    - release-notes.html
	  Release information for WSO2 IoT 3.1.0

	- patches
	  Used to add patches related for all runtimes.

	-dropins
	  Used to add external jars(dependencies) of all runtimes.

	-extensions
	  Used to add carbon extensions.

	-servicepacks
	 Used to add service packs related to all runtimes.

	-webapp-mode

	-wso2/components
	 Contains profiles for all runtimes and the plugins folder

	-wso2/lib
	  Contains jars that are required/shared by all runtimes.

	-wso2/analytics
	  Contains analytics runtime related files/folders.

	-wso2/analytics/conf
	  Analytics runtime specific configuration files.

    -wso2/analytics/repository
	  Where deployments of Analytics runtime is stored.

   -wso2/broker
      Contains broker runtime related files/folders.

   -wso2/broker/conf
      Broker runtime specific configuration files.

   -wso2/broker/repository
      Where deployments of broker runtime is stored.


Secure sensitive information in Carbon configuration files
----------------------------------------------------------

There is sensitive information such as passwords in the Carbon configuration.
You can secure them by using secure vault. Please go through the following steps to
secure them with the default mode.

1. Configure secure vault with the default configurations by running the ciphertool
  script from the bin directory.

> ciphertool.sh -Dconfigure   (in UNIX)

This script automates the following configurations that you would normally need to do manually.

(i) Replaces sensitive elements in configuration files that have been defined in
     cipher-tool.properties, with alias token values.
(ii) Encrypts the plain text password which is defined in the cipher-text.properties file.
(iii) Updates the secret-conf.properties file with the default keystore and callback class.

cipher-tool.properties, cipher-text.properties and secret-conf.properties files
      can be found in the <IoT_HOME>/conf/security directory.

2. Start the server by running the wso2server script, which is in the <IoT_HOME>/bin directory.

> wso2server.sh   (in UNIX)

When running the default mode, it asks you to enter the master password
(By default, the master password is the password of the Carbon keystore and private key)

3. Change any password by running the ciphertool script, which is in the <IoT_HOME>/bin directory.

> ciphertool -Dchange  (in UNIX)

For more information, see
https://docs.wso2.com/display/ADMIN44x/Carbon+Secure+Vault+Implementation

Training
--------

WSO2 Inc. offers a variety of professional Training Programs for WSO2 products.
For additional support on training information please goto http://wso2.com/training/


Support
-------

We are committed to ensuring that your enterprise middleware deployment is completely supported from evaluation to production. Our unique approach ensures that all support leverages our open development methodology and is provided by the very same engineers who build the technology.

For additional support information please refer to http://wso2.com/support/

---------------------------------------------------------------------------
(c) Copyright 2017 WSO2 Inc.