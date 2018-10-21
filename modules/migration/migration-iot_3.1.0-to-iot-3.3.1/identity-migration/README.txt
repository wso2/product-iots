* Copy the migration-resources folder  to the <IoT-3.3.1-HOME> directory.

* Build this https://github.com/wso2-support/product-is/tree/support-5.5.0/modules/migration/migration-service and
  copy the org.wso2.carbon.is.migration-5.5.0.jar to the <IoT-3.3.1-HOME>/dropins directory.

* Copy and replace the keystores used in the previous version (IoT-3.1.0) to the <IoT-3.3.1-HOME>/repository/resources/security directory.

* Run the following command
   ./iot-server.sh -Dmigrate -Dcomponent=identity
