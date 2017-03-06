#WSO2 Message Broker

Welcome to the WSO2 Message broker.

WSO2 MB is a lightweight and easy-to-use Open Source Distributed Message Brokering
Server (MB) which is available under the Apache Software License v2.0.

This is based on the revolutionary WSO2 Carbon [Middleware a' la carte]
framework. All the major features have been developed as pluggable Carbon
components.

<h2>Key Features of WSO2 MB</h2>
WSO2 Message Broker brings messaging and eventing capabilities into your SOA framework.
The latest version of this product possesses following key features. All these features
can be used as standalone message broker or as a distributed message brokering system.

WSO2 Message Broker compatible with Advanced Message Queuing Protocol (AMQP)(0-91)) 
and  Message Queuing Telemetry Transport Protocol (MQTT) v 3.1.1. 
<ul>
<li> JMS Queuing support </li>
<li> JMS Pub/Sub mechanism for topics </li>
<li> Hierarchical Topics Subscriptions </li>
<li> Queue Message browsing with added UI support </li>
<li> Message Re-Delivery Tries Configuration </li>
<li> Message Re delivery Header Field support </li>
<li> Sample text message sender tool in UI </li>
<li> Queue purging support </li>
<li> Simple clustering machanism based on carbon clustering </li>
<li> Ability to view details of the cluster using Management Console </li>
<li> Message delivery fine tuning capabilities </li>
<li> Relational databases as a storage machanism </li>
</ul>

<h2>System Requirements</h2>
<ol>
<li> Minimum memory - 2GB </li>
<li> Processor      - Pentium 800MHz or equivalent at minimum </li>
<li> Java SE Development Kit 1.7 or higher </li>
<li> The Management Console requires you to enable Javascript of the Web browser,
   with MS IE 7. In addition to JavaScript, ActiveX should also be enabled
   with IE. This can be achieved by setting your security level to
   medium or lower. </li>
<li> To compile and run the sample clients, an Ant version is required. Ant 1.7.0
   version is recommended. </li>
<li> To build WSO2 MB from the Source distribution, it is necessary that you have
   JDK 7  and Maven 3.0.4 or later </li>
</ol>

For more details see
    https://docs.wso2.com/display/MB320/Installation+Prerequisites

<h2>Installation & Running</h2>

<ol>
<li> Extract the wso2mb-3.2.0.zip and go to the extracted directory </li>
<li> Run the wso2server.sh or wso2server.bat as appropriate </li>
<li> Point your favourite browser to

    https://localhost:9443/carbon
</li>
<li> Use the following username and password to login

    username : admin
    password : admin
</li>
</ol>

<h2>WSO2 MB 3.2.0 distribution directory structure</h2>

	CARBON_HOME
		|-- bin <folder>
		|-- dbscripts <folder>
		|-- client-lib <folder>
		|-- lib <folder>
		|-- repository <folder>
		|   |-- components <folder>
		|   |-- conf <folder>
		|       |-- Advanced <folder>
            |-- datasources <folder>
		|   |-- database <folder>
		|   |-- deployment <folder>
		|   |-- logs <folder>
		|   |-- tenants <folder>
		|   |-- resources <folder>
		|       |-- security <folder>
		|-- tmp <folder>
		|-- LICENSE.txt <file>
		|-- README.txt <file>
		`-- release-notes.html <file>

    - bin
	    Contains various scripts .sh & .bat scripts

    - dbscripts
      Contains the SQL scripts for setting up the database on a variety of
      Database Management Systems, including H2, Derby, MSSQL, MySQL abd
      Oracle.

    - client-lib
      Contains required libraries for JMS,Event Clients

    - lib
      Contains the basic set of libraries required to start-up  WSO2 MB
      in standalone mode

    - repository
      The repository where services and modules deployed in WSO2 MB
      are stored.

        - components
          Contains OSGi bundles and configurations
      
        - conf
          Contains configuration files
             - datasources
                contains configuration for setting up databases.
         
        - database
          Contains the database

        - deployment
          Contains Axis2 deployment details
          
        - logs
          Contains all log files created during execution

        - tenants
          Contains tenant details

    - resources
      Contains additional resources that may be required

        - security
          Contains security resources
          
    - tmp
      Used for storing temporary files, and is pointed to by the
      java.io.tmpdir System property

    - LICENSE.txt
      Apache License 2.0 under which WSO2 MB is distributed.

    - README.txt
      This document.



<h2>Support</h2>
WSO2 Inc. offers a variety of development and production support
programs, ranging from Web-based support up through normal business
hours, to premium 24x7 phone support.

For additional support information please refer to http://wso2.com/support/

For more information on WSO2 MB, visit the WSO2 Oxygen Tank (http://wso2.org)

For more details and to take advantage of this unique opportunity please visit
http://wso2.com/support/

Thank you for your interest in WSO2 Message Broker.

<h2>Known Issues</h2>

https://wso2.org/jira/issues/?filter=12509

 WSO2 Message Broker is compatible with AMQP 0-91 version only.

<h2> Build Status </h2>

|  Branch | Build Status |
| :------------ |:-------------
| Java 7      | [![Build Status](https://wso2.org/jenkins/job/product-mb/badge/icon)](https://wso2.org/jenkins/job/product-mb) |
| Java 8 | [![Build Status](https://wso2.org/jenkins/job/product-mb__java8/badge/icon)](https://wso2.org/jenkins/job/product-mb__java8/) |


(c) 2015, WSO2 Inc.

