WSO2 IoT Server
----------------------
Welcome to the WSO2 IoT Server (IoTS) 3.0.0 release.
=======

Key Features
------------
Self-service device enrollment and management with an end-user IoTS console.


Installation & Running
----------------------
1. Extract the downloaded ZIP file.
2. Follow the INSTALL.txt file for install instructions.
3. After the server starts, point your Web browser to https://localhost:9443/devicemgt in order to see the available device types and operations
4. Navigate to https://localhost:9443/api-store to see the available device APIs. You can subscribe to these APIs with the default application (or by creating a new application).
   In the API Store, go to my subscriptions and locate the client ID and secret, which can be used to invoke these APIs.

System Requirements
-------------------

1. Minimum memory - 4GB
2. The portal app requires full Javascript enablement on the Web browser


WSO2 IoT Server (IoTS) Binary Distribution Directory Structure
-----------------------------------------------------
  IoT_HOME
    .
    ├── core              //core component
	├── analytics         //Analytics component
	├── broker            //Message Broker component
	├── samples           //sample device types e.g., connectedcup
	├── plugins           //pre-built device types

The IoTS Core, Analytics, and Broker have similar directory structures. For example Core has the following structure:
  IoT_HOME/core
        .
        ├── bin              //executables
        ├── dbscripts        //DBScripts
        ├── INSTALL.txt
        ├── lib
        ├── LICENSE.txt
        ├── modules          //Jaggery Modules
        ├── README.txt
        ├── release-notes.html
        ├── repository       // repository
        ├── tmp
        ├── webapp-mode


    - bin
      Contains various scripts (i.e., .sh & .bat scripts).

    - dbscripts
      Contains the database creation & seed data population SQL scripts for
      various supported databases.

    - lib
      Contains the basic set of libraries required to startup WSO2 IoT Server
      in standalone mode

    - repository
      The repository where Carbon artifacts & Axis2 services and
      modules deployed in WSO2 Carbon, and other custom deployers such as
      dataservices and axis1services are stored.

        - carbonapps
          Carbon Application hot deployment directory.

      - components
          Contains all OSGi related libraries and configurations.

        - conf
          Contains server configuration files. e.g., axis2.xml, carbon.xml

        - data
          Contains internal LDAP related data.

        - database
          Contains the WSO2 Registry & User Manager database.

        - deployment
          Contains server side and client side Axis2 repositories.
          All deployment artifacts should go into this directory.

        - logs
          Contains all log files created during execution.

        - resources
          Contains additional resources that may be required.

  - tenants
    This directory will contain relevant tenant artifacts
    in the case of a multi-tenant deployment.

    - tmp
      Used for storing temporary files, and is pointed to by the
      java.io.tmpdir system property.

    - webapp-mode
      You have the option of running WSO2 Carbon in the webapp mode (hosted as a web-app in an application server). This directory contains files required to run Carbon in the webapp mode.

    - LICENSE.txt
      Apache License 2.0 under which WSO2 Carbon is distributed.

    - README.txt
      This document.

    - INSTALL.txt
      This document contains information on installing WSO2 IoT Server.

    - release-notes.html
      Release information for WSO2 IoT Server 3.0.0

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
      can be found in the <IoT_HOME>/repository/conf/security directory.

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