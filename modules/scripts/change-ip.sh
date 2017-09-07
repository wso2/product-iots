#!/bin/bash

echo ""
echo "----------------------------------------"
echo "WSO2 IoT Server IP configuration tool"
echo "----------------------------------------"


##################################### IP configs related to core ####################################

echo ""
echo ">>> Step 1: Change current IP address of the IoT server"

echo ""
echo "Please enter the IoT Core IP that you need to replace (if you are trying out IoT server for the first time this will be localhost)"
read val1;

while [[ -z $val1 ]]; do #if $val1 is a zero length String
    echo "Please enter the IoT Core IP that you need to replace (if you are trying out IoT server for the first time this will be localhost)"
    read val1;
done

echo ""
echo "Please enter your current IP"
read val2;

while [[ -z $val2 ]]; do #if $val2 is a zero length String
    echo "Please enter your current IP"
    read val2;
done


echo "--------------------------------------"
echo "All your " + $val1 + " IP's are replaced with " +$val2 ;
echo "--------------------------------------"

replaceText='s/localhost/'$val1'/g'

echo "Changing <IoT_HOME>/conf/carbon.xml"
sed -i '' -e 's#\(<HostName>\)'$val1'\(</HostName>\)#\1'$val2'\2#g' ../conf/carbon.xml
sed -i '' -e 's#\(<MgtHostName>\)'$val1'\(</MgtHostName>\)#\1'$val2'\2#g' ../conf/carbon.xml
echo "Completed!!"

#--------------------
echo "Changing <IoT_HOME>/conf/app-manager.xml"
sed -i '' -e 's#\(<IdentityProviderUrl>\)https\:\/\/'$val1'\:\${mgt\.transport\.https\.port}\/samlsso\(</IdentityProviderUrl>\)#\1'https://$val2:9443/samlsso'\2#g' ../conf/app-manager.xml
echo "Completed!!"

#--------------------
echo "Changing <IoT_HOME>/conf/identity/sso-idp-config.xml"
sed -i '' -e 's/'$val1'/'$val2'/g' ../conf/identity/sso-idp-config.xml
echo "Completed!!"

#--------------------
echo "Changing <IoT_HOME>/conf/iot-api-config.xml"
sed -i '' -e 's/'$val1'/'$val2'/g' ../conf/iot-api-config.xml
echo "Completed!!"

#--------------------
echo "Changing <IoT_HOME>/repository/deployment/server/jaggeryapps/devicemgt/app/conf/app-conf.json"
sed -i -e 's/"identityProvider.*/\"identityProviderUrl\"\ :\ "https\:\/\/'$val2':9443\/samlsso\"\,/' ../repository/deployment/server/jaggeryapps/devicemgt/app/conf/app-conf.json
sed -i -e 's/"acs.*/\"acs\"\ :\ "https\:\/\/'$val2':9443\/devicemgt\/uuf\/sso\/acs\"\,/' ../repository/deployment/server/jaggeryapps/devicemgt/app/conf/app-conf.json
echo "Completed!!"

#--------------------
echo "Changing <IoT_HOME>/repository/deployment/server/jaggeryapps/api-store/site/conf/site.json"
sed -i -e 's/"identityProvider.*/\"identityProviderURL\"\ :\ "https\:\/\/'$val2':9443\/samlsso\"\,/' ../repository/deployment/server/jaggeryapps/api-store/site/conf/site.json
echo "Completed!!"

#--------------------
echo "Changing <IoT_HOME>/repository/deployment/server/jaggeryapps/portal/configs/designer.json"
sed -i -e 's/"acs.*/\"acs\"\ :\ "https\:\/\/'$val2':9443\/portal\/acs\"\,/' ../repository/deployment/server/jaggeryapps/portal/configs/designer.json
echo "Completed!!"

#--------------------
echo "Changing  <IoT_HOME>/conf/api-manager.xml"
if grep -q '<!-- Server URL of the API key manager -->' ../conf/api-manager.xml;
then
echo 'found'
sed -i -e 's|<!-- Server URL of the API key manager -->||' ../conf/api-manager.xml
fi

if grep -q '<ServerURL>https:\/\/\${carbon.local.ip}:\${mgt.transport.https.port}\${carbon.context}services\/<\/ServerURL>' ../conf/api-manager.xml;
then
echo 'found'
sed -i -e 's/<ServerURL>https:\/\/\${carbon.local.ip}:\${mgt.transport.https.port}\${carbon.context}services\/<\/ServerURL>//' ../conf/api-manager.xml
fi

if grep -q '<ServerURL>https:\/\/'$val2':\${mgt.transport.https.port}\${carbon.context}services\/<\/ServerURL>' ../conf/api-manager.xml;
then
echo 'found'
sed -i -e 's/<ServerURL>https:\/\/'$val2':\${mgt.transport.https.port}\${carbon.context}services\/<\/ServerURL>//' ../conf/api-manager.xml
fi

sed -i '' -e 's/<APIKeyValidator>/<APIKeyValidator><!-- Server URL of the API key manager --><ServerURL>https:\/\/'$val2'\:\$\{mgt\.transport\.https\.port\}\$\{carbon\.context\}services\/<\/ServerURL>/g' ../conf/api-manager.xml
echo "Completed!!"

#--------------------
if grep -q '<RevokeAPIURL>https:\/\/localhost:\${https.nio.port}\/revoke<\/RevokeAPIURL>' ../conf/api-manager.xml;
then
sed -i -e 's|<RevokeAPIURL>https:\/\/localhost:\${https.nio.port}\/revoke<\/RevokeAPIURL>|<RevokeAPIURL>https:\/\/'$val2':\${https.nio.port}\/revoke<\/RevokeAPIURL>|' ../conf/api-manager.xml
fi

if grep -q '<RevokeAPIURL>https:\/\/'$val1':\${https.nio.port}\/revoke<\/RevokeAPIURL>' ../conf/api-manager.xml;
then
sed -i -e 's|<RevokeAPIURL>https:\/\/'$val1':\${https.nio.port}\/revoke<\/RevokeAPIURL>|<RevokeAPIURL>https:\/\/'$val2':\${https.nio.port}\/revoke<\/RevokeAPIURL>|' ../conf/api-manager.xml
fi

#--------------------
echo "Changing <IoT_HOME>/conf/etc/webapp-publisher-config.xml"
sed -i '' -e 's#\(<EnabledUpdateApi>\)false\(</EnabledUpdateApi>\)#\1'true'\2#g' ../conf/etc/webapp-publisher-config.xml
echo "Completed!!"

#--------------------
echo "Changing  <IoT_HOME>/bin/iot-server.sh"
sed -i -e 's/-Diot.manager.host.*/-Diot.manager.host="'$val2'" \\/' ../bin/iot-server.sh
sed -i -e 's/-Diot.core.host.*/-Diot.core.host="'$val2'" \\/' ../bin/iot-server.sh
sed -i -e 's/-Diot.keymanager.host.*/-Diot.keymanager.host="'$val2'" \\/' ../bin/iot-server.sh
sed -i -e 's/-Diot.gateway.host.*/-Diot.gateway.host="'$val2'" \\/' ../bin/iot-server.sh
echo "Completed!!"

#----------------------
echo ""
echo "Changing hostURL of <IoT_HOME>/wso2/broker/conf/broker.xml"
if grep -q '<property name="hostURL">https://'$val1':9443/services/OAuth2TokenValidationService</property>' ../wso2/broker/conf/broker.xml;
then
echo "found"
sed -i -e 's|<property name="hostURL">https:\/\/'$val1':9443\/services\/OAuth2TokenValidationService</\property>|<property name="hostURL">https:\/\/'$val2':9443\/services\/OAuth2TokenValidationService</\property>|' ../wso2/broker/conf/broker.xml
echo "Completed!!"
fi

#------------------------
echo ""
echo "Changing tokenEndpoint of <IoT_HOME>/wso2/broker/conf/broker.xml"
if grep -q '<property name="tokenEndpoint">https:\/\/'$val1':8243</\property>' ../wso2/broker/conf/broker.xml;
then
echo "found"
sed -i -e 's|<property name="tokenEndpoint">https:\/\/'$val1':8243</\property>|<property name="tokenEndpoint">https:\/\/'$val2':8243</\property>|' ../wso2/broker/conf/broker.xml
echo "Completed!!"
fi

#--------------------------
echo ""
echo "Changing deviceMgtServerUrl of <IoT_HOME>/wso2/broker/conf/broker.xml"
if grep -q '<property name="deviceMgtServerUrl">https:\/\/'$val1':8243</\property>' ../wso2/broker/conf/broker.xml;
then
echo "found"
sed -i -e 's|<property name="deviceMgtServerUrl">https:\/\/'$val1':8243</\property>|<property name="deviceMgtServerUrl">https:\/\/'$val2':8243</\property>|' ../wso2/broker/conf/broker.xml
echo "Completed!!"
fi

#--------------------
echo "Changing  <IoT_HOME>/wso2/analytics/bin/wso2server.sh"
sed -i -e 's/-Diot.keymanager.host.*/-Diot.keymanager.host="'$val2'" \\/' ../wso2/analytics/bin/wso2server.sh
sed -i -e 's/-Diot.gateway.host.*/-Diot.gateway.host="'$val2'" \\/' ../wso2/analytics/bin/wso2server.sh
echo "Completed!!"

#--------------------
echo "Changing  <IoT_HOME>/wso2/analytics/repository/deployment/server/jaggeryapps/portal/configs/designer.json"
sed -i -e 's/"identityProviderURL.*/\"identityProviderURL\"\:\"https\:\/\/'$val2':9443\/samlsso\"\,/' ../wso2/analytics/repository/deployment/server/jaggeryapps/portal/configs/designer.json
sed -i -e 's/"dynamicClientAppRegistrationServiceURL.*/\"dynamicClientAppRegistrationServiceURL\"\:\"https\:\/\/'$val2':9443\/dynamic-client-web\/register\"\,/' ../wso2/analytics/repository/deployment/server/jaggeryapps/portal/configs/designer.json
sed -i -e 's/"apiManagerClientAppRegistrationServiceURL.*/\"apiManagerClientAppRegistrationServiceURL\"\:\"https\:\/\/'$val2':9443\/api-application-registration\/register\/tenants\"\,/' ../wso2/analytics/repository/deployment/server/jaggeryapps/portal/configs/designer.json
sed -i -e 's/"tokenServiceURL.*/\"tokenServiceURL\"\: \"https\:\/\/'$val2':9443\/oauth2\/token\"/' ../wso2/analytics/repository/deployment/server/jaggeryapps/portal/configs/designer.json
sed -i -e 's/"hostname.*/\"hostname\"\: \"'$val2'\"\,/' ../wso2/analytics/repository/deployment/server/jaggeryapps/portal/configs/designer.json
echo "Completed!!"

##################################### IP configs related to broker ####################################
echo ""
echo ""
echo ">>> Step 2: Change current IP address of the IoT Broker"
echo "-------------------------------------------------------"

#--------------------
echo "Changing  <IoT_HOME>/wso2/analytics/bin/wso2server.sh"
sed -i -e 's/-Dmqtt.broker.host.*/-Dmqtt.broker.host="'$val2'" \\/' ../wso2/analytics/bin/wso2server.sh
echo "Completed!!"

#--------------------
echo "Changing  <IoT_HOME>/bin/iot-server.sh"
sed -i -e 's/-Dmqtt.broker.host.*/-Dmqtt.broker.host="'$val2'" \\/' ../bin/iot-server.sh
echo "Completed!!"

echo "Changing <IoT_HOME>/wso2/broker/conf/carbon.xml"
sed -i '' -e 's#\(<HostName>\)'$val1'\(</HostName>\)#\1'$val2'\2#g' ../wso2/broker/conf/carbon.xml
sed -i '' -e 's#\(<MgtHostName>\)'$val1'\(</MgtHostName>\)#\1'$val2'\2#g' ../wso2/broker/conf/carbon.xml
echo "Completed!!"




##################################### IP configs related to analytics ####################################

echo ""
echo ""
echo ">>> Step 3: Change current IP address of the IoT Analytics"
echo "-------------------------------------------------------"



#--------------------
echo "Changing  <IoT_HOME>/bin/iot-server.sh"
sed -i -e 's/-Diot.analytics.host.*/-Diot.analytics.host="'$val2'" \\/' ../bin/iot-server.sh
echo "Completed!!"

echo "Changing  <IoT_HOME>/wso2/analytics/repository/deployment/server/jaggeryapps/portal/configs/designer.json"
sed -i -e 's/"acs.*/\"acs\"\:\"https\:\/\/'$val2':9445\/portal\/acs\"\,/' ../wso2/analytics/repository/deployment/server/jaggeryapps/portal/configs/designer.json
sed -i -e 's/"callbackUrl.*/\"callbackUrl\"\:\"https\:\/\/'$val2':9445\/portal\"\,/' ../wso2/analytics/repository/deployment/server/jaggeryapps/portal/configs/designer.json
echo "Completed!!"

echo "Changing <IoT_HOME>/wso2/analytics/conf/carbon.xml"
sed -i '' -e 's#\(<HostName>\)'$val1'\(</HostName>\)#\1'$val2'\2#g' ../wso2/analytics/conf/carbon.xml
sed -i '' -e 's#\(<MgtHostName>\)'$val1'\(</MgtHostName>\)#\1'$val2'\2#g' ../wso2/analytics/conf/carbon.xml
echo "Completed!!"

echo ""
echo "-----------------------------------------------"
echo "Generating SSL certificates for the IoT Server"
echo "-----------------------------------------------"
echo ""

B_SUBJ=''
C_SUBJ=''
A_SUBJ=''
SERVER_ADDRESS=''
slash='/'
equal='='

buildSubject(){
    if [ $1 = "CN" ]; then
        echo "Please provide Common Name "
        read val
        while [[ -z $val ]]; do #if $val is a zero length String
            echo "Common name(your server IP/hostname) cannot be null. Please enter the Common name."
            read val;
        done
        if [ -n $val ]; then    #This is true if $val is not empty (If $val is not a non zero length String)
            if [ $3 = "C" ]; then
                C_SUBJ="$C_SUBJ$slash$1$equal$val"
                return
                elif [ $3 = "B" ]; then
                B_SUBJ="$B_SUBJ$slash$1$equal$val"
                return
                else
                A_SUBJ="$A_SUBJ$slash$1$equal$val"
                SERVER_ADDRESS=$val
                return
            fi
        fi
    fi

    echo "Please provide "$2". Press Enter to skip."
    read val;
    if [ ! -z $val ]; then  #If $val is not a zero length String; This is same as if[ -n $val]; then
        if [ $3 = "C" ]; then
            C_SUBJ="$C_SUBJ$slash$1$equal$val"
            return
        elif [ $3 = "B" ]; then
            B_SUBJ="$B_SUBJ$slash$1$equal$val"
            return
        else
            A_SUBJ="$A_SUBJ$slash$1$equal$val"
            return
        fi
    fi
}
mkdir tmp
echo ''
echo '=======Enter Values for IoT Core SSL Certificate======='

buildSubject 'C' 'Country' 'C'
buildSubject 'ST' 'State' 'C'
buildSubject 'L' 'Location' 'C'
buildSubject 'O' 'Organization' 'C'
buildSubject 'OU' 'Organizational Unit' 'C'
buildSubject 'emailAddress' 'Email Address' 'C'
buildSubject 'CN' 'Common Name' 'C'

echo ""
echo 'Provided IoT Core SSL Subject : ' $C_SUBJ

echo 'If you have a different IoT Core Keystore password please enter it here. Press Enter to use the default password.'
read -s password
if [ ! -z $password ]; then
    SSL_PASS=$password
else
    SSL_PASS="wso2carbon"
fi

echo ""
echo "Generating SSL Certificate for IoT Core"
openssl genrsa -out ./tmp/c.key 4096
openssl req -new -key ./tmp/c.key -out ./tmp/c.csr  -subj $C_SUBJ
openssl x509 -req -days 730 -in ./tmp/c.csr -signkey ./tmp/c.key -set_serial 044324884 -out ./tmp/c.crt

echo "Export to PKCS12"
openssl pkcs12 -export -out ./tmp/CKEYSTORE.p12 -inkey ./tmp/c.key -in ./tmp/c.crt -name "wso2carbon" -password pass:$SSL_PASS

echo "Export PKCS12 to JKS"
keytool -importkeystore -srckeystore ./tmp/CKEYSTORE.p12 -srcstoretype PKCS12 -destkeystore ../repository/resources/security/wso2carbon.jks -deststorepass wso2carbon -srcstorepass wso2carbon -noprompt
keytool -importkeystore -srckeystore ./tmp/CKEYSTORE.p12 -srcstoretype PKCS12 -destkeystore ../repository/resources/security/client-truststore.jks -deststorepass wso2carbon -srcstorepass wso2carbon -noprompt

keytool -importkeystore -srckeystore ./tmp/CKEYSTORE.p12 -srcstoretype PKCS12 -destkeystore ../wso2/broker/repository/resources/security/wso2carbon.jks -deststorepass wso2carbon -srcstorepass wso2carbon -noprompt
keytool -importkeystore -srckeystore ./tmp/CKEYSTORE.p12 -srcstoretype PKCS12 -destkeystore ../wso2/broker/repository/resources/security/client-truststore.jks -deststorepass wso2carbon -srcstorepass wso2carbon -noprompt

keytool -importkeystore -srckeystore ./tmp/CKEYSTORE.p12 -srcstoretype PKCS12 -destkeystore ../wso2/analytics/repository/resources/security/wso2carbon.jks -deststorepass wso2carbon -srcstorepass wso2carbon -noprompt
keytool -importkeystore -srckeystore ./tmp/CKEYSTORE.p12 -srcstoretype PKCS12 -destkeystore ../wso2/analytics/repository/resources/security/client-truststore.jks -deststorepass wso2carbon -srcstorepass wso2carbon -noprompt

echo ""
echo "Setting up the public certificate for the default idp"
if hash tac; then
    VAR=$(keytool -exportcert -alias wso2carbon -keystore ../repository/resources/security/wso2carbon.jks -rfc -storepass wso2carbon | tail -n +2 | tac | tail -n +2 | tac | tr -cd "[:print:]");
else
    VAR=$(keytool -exportcert -alias wso2carbon -keystore ../repository/resources/security/wso2carbon.jks -rfc -storepass wso2carbon | tail -n +2 | tail -r | tail -n +2 | tail -r | tr -cd "[:print:]"); fi

echo ""
echo "Printing certificate"
echo "-----------------------"
echo $VAR
sed -i '' -e 's#<Certificate>.*#<Certificate>'"$VAR"'</Certificate>#g' ../conf/identity/identity-providers/iot_default.xml

echo ""
if [ -e "../conf/identity/identity-providers/iot_default.xml-e" ]; then
        echo "IDP temp file exists, hence removing"
        rm -f ../conf/identity/identity-providers/iot_default.xml-e
fi

echo ""
echo "Configuration Completed!!!"
