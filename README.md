<a href="http://wso2.com/products/iot-server/">
<img src="http://b.content.wso2.com/sites/all/common/images/product-logos/IoT-server.svg"
     srcset="http://b.content.wso2.com/sites/all/common/images/product-logos/IoT-server.svg@2x.png 2x"
     alt="WSO2 IoT Server" />
</a>
# Welcome to WSO2 IoT Server 
[![Build Status](https://wso2.org/jenkins/buildStatus/icon?job=product-iots)](https://wso2.org/jenkins/view/Dashboard/job/product-iots/)

WSO2 IoT Server is a complete solution that enables device manufacturers and enterprises to connect and manage their devices, build apps, manage events, secure devices and data, and visualize sensor data in a scalable manner.

It also offers a complete and secure enterprise mobility management (EMM/MDM) solution that aims to address mobile computing challenges faced by enterprises today. Supporting iOS, Android, and Windows devices, it helps organizations deal with both corporate owned, personally enabled (COPE) and employee-owned devices with the bring your own device (BYOD) concept.

WSO2 IoT Server comes with advanced analytics, enabling users to analyze speed, proximity and geo-fencing information of devices including details of those in motion and stationary state.

Find the online documentation at : 
https://docs.wso2.com/display/IoTS300/WSO2+IoT+Server+Documentation.

### Key Features of WSO2 IoT Server

#### Generic framework for Device Management
* Extensions for registering built-in/custom device types
* Self-service enrollment and management of connected devices
* Group, manage and monitor connected devices
* Share device operations/data with other users
* Distribute and manage applications/firmware of devices
* Edge computing powered by the WSO2 Complex Event Processor (CEP) streaming engine (Siddhi - https://github.com/wso2/siddhi)
* Out of the Box support for some known device types such as Raspberry Pi, Arduino Uno,...etc
* Supports mobile platforms such as Android, Windows, and iOS.

#### Mobile Device and App Management
* Implement self-service device enrollment and management for iOS, Android and Windows devices
* Provide policy-driven device and profile management for security, data, and device features
* Enable compliance monitoring for applied policies on devices and role-based access control
* Provision/de-provision apps to multiple enrolled devices per user and to enrolled devices based on roles

#### IoT Protocol Support
* Leverage MQTT, HTTP, Websockets and XMPP protocols for device communications with IoT Server Framework extension for adding more protocols and data formats

#### IoT Analytics
* Support for batch, interactive, real-time and predictive analytics through WSO2 Data Analytics Server (DAS)

#### Pre-built visualization support for sensor readings
* View instant, visualized statistics of individual or multiple devices
* Traverse through, analyse and zoom in/out of filtered data
* Stats-API to write your own visualization
* Pre-built graphs for common sensor reading types like temperature, velocity

#### API Management for App Development
* All connected devices are exposed via managed REST APIs
* API Store for easy discovery of all product/device APIs for app development

#### Identity and Access Management
* Identity Management for devices
* Token based access control for devices & operations (protect back end services via exposing device type APIs)
* Support for SCEP protocol (encryption and authenticity)

### How to Run
* Extract the downloaded wso2iot-3.0.0.zip file; this will create a folder named ‘wso2iot-3.0.0’.
* IoT Server comes with three runnable components namely broker, analytics and core. Start these components in following order by executing wso2server.sh [.bat]
    * wso2iot-3.0.0/broker/bin
    * wso2iot-3.0.0/core/bin
    * wso2iot-3.0.0/analytics/bin

### How to Contribute

* WSO2 IoT Server code is hosted in [GitHub](https://github.com/wso2/product-iots).
* Please report issues at [IoTS JIRA](https://wso2.org/jira/browse/IOTS) and Send your pull requests to [development branch](https://github.com/wso2/product-iots).

### Contact us

WSO2 IoT Server developers can be contacted via the mailing lists:

* WSO2 Developers List : dev@wso2.org
* WSO2 Architecture List : architecture@wso2.org

