#!/bin/bash

echo ""
echo "----------------------------------------"
echo "WSO2 IoT Server Super Admin Credentials Changing tool"
echo "----------------------------------------"


##################################### Super Admin Credentials Change ####################################

echo ""
echo ">>> Change current super admin username and password of the IoT server"

echo ""
echo "Please enter the old username and password of the IoTS super Admin"
echo "if you are trying out IoTS for the first time username/password will be 'admin/admin'"
echo "Old Username : "
read val1;
echo "Old Password : "
read val2;

while [[ -z $val1 || -z $val2 ]]; do #if $val1 is a zero length String
    echo "Username or Password couldn't be empty, Hence Re-Enter old username and password of IoTS Super Admin"
    echo "Old Username : "
    read val1;
    echo "Old Password : "
    read val2;
done

echo ""
echo "Please enter the new password of the IoTS super admin"
echo "New Username : "
read val3;
echo "New Password : "
read val4;

while [[ -z $val3 || -z $val4 ]]; do #if $val2 is a zero length String
    echo "Username or Password couldn't be empty, Hence Re-Enter new username and password of IoTS Super Admin"
    echo "New Username : "
    read val3;
    echo "New Password : "
    read val4;
done

echo "Changing <IoT_HOME>/conf/cdm-config.xml"
sed -i -e 's#\(<AdminUsername>\)'$val1'\(</AdminUsername>\)#\1'$val3'\2#g' ../conf/cdm-config.xml
sed -i -e 's#\(<AdminPassword>\)'$val2'\(</AdminPassword>\)#\1'$val4'\2#g' ../conf/cdm-config.xml
echo "Completed!!"

echo "Changing <IoT_HOME>/conf/app-manager.xml"
sed -i -e 's#\(<Username>\)'$val1'\(</Username>\)#\1'$val3'\2#g' ../conf/app-manager.xml
sed -i -e 's#\(<Password>\)'$val2'\(</Password>\)#\1'$val4'\2#g' ../conf/app-manager.xml

sed -i -e 's#\(<DASUsername>\)'$val1'\(</DASUsername>\)#\1'$val3'\2#g' ../conf/app-manager.xml
sed -i -e 's#\(<DASPassword>\)'$val2'\(</DASPassword>\)#\1'$val4'\2#g' ../conf/app-manager.xml

sed -i -e 's#\(<username>\)'$val1'\(</username>\)#\1'$val3'\2#g' ../conf/app-manager.xml
sed -i -e 's#\(<password>\)'$val2'\(</password>\)#\1'$val4'\2#g' ../conf/app-manager.xml

sed -i -e 's#\(<property name="Username">\)'$val1'\(</property>\)#\1'$val3'\2#g' ../conf/app-manager.xml
sed -i -e 's#\(<property name="Password">\)'$val2'\(</property>\)#\1'$val4'\2#g' ../conf/app-manager.xml

sed -i -e 's#\(<Property name="AuthUser">\)'$val1'\(</Property>\)#\1'$val3'\2#g' ../conf/app-manager.xml
sed -i -e 's#\(<Property name="AuthPass">\)'$val2'\(</Property>\)#\1'$val4'\2#g' ../conf/app-manager.xml
echo "Completed!!"

echo "Changing <IoT_HOME>/conf/iot-api-config.xml"
sed -i -e 's#\(<Username>\)'$val1'\(</Username>\)#\1'$val3'\2#g' ../conf/iot-api-config.xml
sed -i -e 's#\(<Password>\)'$val2'\(</Password>\)#\1'$val4'\2#g' ../conf/iot-api-config.xml
echo "Completed!!"

echo "Changing <IoT_HOME>/conf/apim-integration.xml"
sed -i -e 's#\(<Username>\)'$val1'\(</Username>\)#\1'$val3'\2#g' ../conf/apim-integration.xml
sed -i -e 's#\(<Password>\)'$val2'\(</Password>\)#\1'$val4'\2#g' ../conf/apim-integration.xml
echo "Completed!!"

echo "Changing <IoT_HOME>/conf/analytics/analytics-data-config.xml"
sed -i -e 's#\(<Username>\)'$val1'\(</Username>\)#\1'$val3'\2#g' ../conf/analytics/analytics-data-config.xml
sed -i -e 's#\(<Password>\)'$val2'\(</Password>\)#\1'$val4'\2#g' ../conf/analytics/analytics-data-config.xml
echo "Completed!!"

echo "Changing <IoT_HOME>/wso2/analytics/conf/analytics/analytics-data-config.xml"
sed -i -e 's#\(<Username>\)'$val1'\(</Username>\)#\1'$val3'\2#g' ../wso2/analytics/conf/analytics/analytics-data-config.xml
sed -i -e 's#\(<Password>\)'$val2'\(</Password>\)#\1'$val4'\2#g' ../wso2/analytics/conf/analytics/analytics-data-config.xml
echo "Completed!!"

echo "Changing <IoT_HOME>/wso2/analytics/conf/input-event-adapters.xml"
sed -i -e 's#\(<property key="username">\)'$val1'\(</property>\)#\1'$val3'\2#g' ../wso2/analytics/conf/input-event-adapters.xml
sed -i -e 's#\(<property key="password">\)'$val2'\(</property>\)#\1'$val4'\2#g' ../wso2/analytics/conf/input-event-adapters.xml
echo "Completed!!"

echo "Changing <IoT_HOME>/wso2/analytics/conf/output-event-adapters.xml"
sed -i -e 's#\(<property key="username">\)'$val1'\(</property>\)#\1'$val3'\2#g' ../wso2/analytics/conf/input-event-adapters.xml
sed -i -e 's#\(<property key="password">\)'$val2'\(</property>\)#\1'$val4'\2#g' ../wso2/analytics/conf/input-event-adapters.xml
echo "Completed!!"

echo "Changing <IoT_HOME>/conf/user-mgt.xml"
sed -i -e 's#\(<UserName>\)'$val1'\(</UserName>\)#\1'$val3'\2#g' ../conf/user-mgt.xml
sed -i -e 's#\(<Password>\)'$val2'\(</Password>\)#\1'$val4'\2#g' ../conf/user-mgt.xml
echo "Completed!!"

echo "Changing <IoT_HOME>/wso2/broker/conf/user-mgt.xml"
sed -i -e 's#\(<UserName>\)'$val1'\(</UserName>\)#\1'$val3'\2#g' ../wso2/broker/conf/user-mgt.xml
sed -i -e 's#\(<Password>\)'$val2'\(</Password>\)#\1'$val4'\2#g' ../wso2/broker/conf/user-mgt.xml
echo "Completed!!"

echo "Changing <IoT_HOME>/wso2/analytics/conf/user-mgt.xml"
sed -i -e 's#\(<UserName>\)'$val1'\(</UserName>\)#\1'$val3'\2#g' ../wso2/analytics/conf/user-mgt.xml
sed -i -e 's#\(<Password>\)'$val2'\(</Password>\)#\1'$val4'\2#g' ../wso2/analytics/conf/user-mgt.xml
echo "Completed!!"

echo "Changing <IoT_HOME>/conf/metrics.xml"
sed -i -e 's#\(<Username>\)'$val1'\(</Username>\)#\1'$val3'\2#g' ../conf/metrics.xml
sed -i -e 's#\(<Password>\)'$val2'\(</Password>\)#\1'$val4'\2#g' ../conf/metrics.xml
echo "Completed!!"

echo "Changing <IoT_HOME>/wso2/analytics/conf/metrics.xml"
sed -i -e 's#\(<Username>\)'$val1'\(</Username>\)#\1'$val3'\2#g' ../wso2/analytics/conf/metrics.xml
sed -i -e 's#\(<Password>\)'$val2'\(</Password>\)#\1'$val4'\2#g' ../wso2/analytics/conf/metrics.xml
echo "Completed!!"

echo "Changing <IoT_HOME>/wso2/broker/conf/metrics.xml"
sed -i -e 's#\(<Username>\)'$val1'\(</Username>\)#\1'$val3'\2#g' ../wso2/broker/conf/metrics.xml
sed -i -e 's#\(<Password>\)'$val2'\(</Password>\)#\1'$val4'\2#g' ../wso2/broker/conf/metrics.xml
echo "Completed!!"

echo "Changing <IoT_HOME>/wso2/broker/conf/broker.xml"
sed -i -e 's#\(<property name="username">\)'$val1'\(</property>\)#\1'$val3'\2#g' ../wso2/broker/conf/broker.xml
sed -i -e 's#\(<property name="password">\)'$val2'\(</property>\)#\1'$val4'\2#g' ../wso2/broker/conf/broker.xml
echo "Completed!!"

echo ""
echo "If tenant domain is carbon.super enter 'Y' otherwise enter 'N' "
read val5;

if [ $val5 = "Y" ]; then
    echo "Changing <IoT_HOME>/repository/deployment/server/jaggeryapps/devicemgt/app/conf/config.json"
    sed -i -e 's#\("adminUser": "\)'$val1'\(@carbon.super"\)#\1'$val3'\2#g' ../repository/deployment/server/jaggeryapps/devicemgt/app/conf/config.json
    sed -i -e 's#\("owner": "\)'$val1'\(@carbon.super"\)#\1'$val3'\2#g' ../repository/deployment/server/jaggeryapps/devicemgt/app/conf/config.json
    echo "Completed!!"

else if [ $val5 = "N" ]; then
    echo ""
    echo "Please enter the old tenant domain of the IoTS "
    echo "if you are trying out IoTS for the first time 'carbon.super' will be the tenant domain"
    echo "Old tenant domain : "
    read val6;
    echo "New tenant domain : "
    read val7;

    while [[ -z $val6 || -z $val7 ]]; do #if $val1 is a zero length String
        echo "tenant domains couldn't be empty, Hence Re-Enter non-empty tenant domains"
        echo "Old tenant domain : "
        read val6;
        echo "New tenant domain : "
        read val7;
    done

    echo "Changing <IoT_HOME>/repository/deployment/server/jaggeryapps/devicemgt/app/conf/config.json"
    sed -i -e 's#\("adminUser": "\)'$val1'\(@\)'$val6'\("\)#\1'$val3'\2'$val7'\3#g' ../repository/deployment/server/jaggeryapps/devicemgt/app/conf/config.json
    sed -i -e 's#\("owner": "\)'$val1'\(@\)'$val6'\("\)#\1'$val3'\2'$val7'\3#g' ../repository/deployment/server/jaggeryapps/devicemgt/app/conf/config.json
    echo "Completed!!"
fi

fi

echo ""
echo "Configuration Completed!!!"
