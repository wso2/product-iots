#!/bin/bash
#------------------------------------------------------------------------
# Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
#
# WSO2 Inc. licenses this file to you under the Apache License,
# Version 2.0 (the "License"); you may not use this file except
# in compliance with the License.
# You may obtain a copy of the License at

# http://www.apache.org/licenses/LICENSE-2.0

# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.
#-------------------------------------------------------------------------
# Profile creator tool for EI
#-------------------------------------------------------------------------



DIR="$(dirname "${BASH_SOURCE[0]}")"
DISTRIBUTION="wso2iot-@product.version@"
ALLPROF=0
BINDIR=$(pwd)
#get the desired profile
echo "This tool will erase all the files which are not required for the selected profile "
echo "and also creates a distribution of this profile in the same folder <IOT_HOME> resides."
echo "WSO2 IoT Server Supports following profiles."
echo "	1.IoT Gateway Profile"
echo "	2.IoT Key Manager Profile"
echo "	3.IoT Device Backend Profile"
echo "	4.IoT Device Manager Profile"
echo "	5.Analytics Profile"
echo "	6.Broker profile"
echo "	7.For All Profiles"
echo "Please enter the desired profile number to create the profile specific distribution."
read profileNumber

create_profile(){
echo "Creating profile - "${profileNumber}
if [ "$profileNumber" -lt 7 ] && [ "$profileNumber" -gt 0 ] ; then
    TEMPDIR=${DIR}/../../target

    if [ ! -d "$TEMPDIR" ]; then
        echo "Creating temporary directory"
        mkdir ${TEMPDIR}
    fi

    rm -rf ${TEMPDIR}/${DISTRIBUTION}
    echo "Copying the distribution to the temporary directory"
    cp -rf ${DIR}/../../${DISTRIBUTION} ${TEMPDIR}/
    DIR=${TEMPDIR}/${DISTRIBUTION}/bin
fi

#gateway profile
if [ ${profileNumber} -eq 1 ]
then
	echo "Preparing the IoT Gateway profile distribution"
	DEFAULT_BUNDLES="$(< ${DIR}/../wso2/components/http-gateway/configuration/org.eclipse.equinox.simpleconfigurator/bundles.info)"
	#remove broker
	echo "Removing Broker profile"
	rm -rf ${DIR}/../wso2/broker
	rm -rf ${DIR}/../wso2/components/broker-default
	rm -rf ${DIR}/broker.bat
	rm -rf ${DIR}/broker.sh

	#remove analytics
	echo "Removing Analytics profile"
	rm -rf ${DIR}/../wso2/analytics
	rm -rf ${DIR}/../wso2/components/analytics-default
	rm -rf ${DIR}/analytics.bat
	rm -rf ${DIR}/analytics.sh

    #remove iot
	echo "Removing IoT Other profiles"
	rm -rf ${DIR}/../wso2/components/default ${DIR}/../wso2/components/device-backend ${DIR}/../wso2/components/device-key-manager ${DIR}/../wso2/components/device-manager
	rm -rf ${DIR}/../samples/
	rm -rf ${DIR}/../plugins
	rm -rf ${DIR}/profile-creator.sh
	rm -rf ${DIR}/profile-creator.bat
	cp -rf ${DIR}/../repository/resources/profiles/gateway/*.sh ${DIR}/../bin/
	cp -rf ${DIR}/../repository/resources/profiles/gateway/*.bat ${DIR}/../bin/
	cp -rf ${DIR}/../repository/resources/profiles/gateway/carbon.xml ${DIR}/../conf/
	rm -rf ${DIR}/../repository/deployment/server/jaggeryapps/*
	rm -rf ${DIR}/../repository/deployment/server/webapps/*
	rm -rf ${DIR}/../repository/deployment/server/carbonapps/*
	rm -rf ${DIR}/../repository/deployment/server/axis2services/*
	rm -rf ${DIR}/../repository/deployment/server/devicetypes/*
	rm -rf ${DIR}/../conf/identity/sso-idp-config.xml

    PROFILE="_gateway"

#key-manager profile
elif [ ${profileNumber} -eq 2 ]
then
	echo "Preparing the IoT Keymanager profile distribution"
	DEFAULT_BUNDLES="$(< ${DIR}/../wso2/components/device-key-manager/configuration/org.eclipse.equinox.simpleconfigurator/bundles.info)"
	#remove broker
	echo "Removing Broker profile"
	rm -rf ${DIR}/../wso2/broker
	rm -rf ${DIR}/../wso2/components/broker-default
	rm -rf ${DIR}/broker.bat
	rm -rf ${DIR}/broker.sh

	#remove analytics
	echo "Removing Analytics profile"
	rm -rf ${DIR}/../wso2/analytics
	rm -rf ${DIR}/../wso2/components/analytics-default
	rm -rf ${DIR}/analytics.bat
	rm -rf ${DIR}/analytics.sh

    #remove iot
	echo "Removing IoT Other profiles"
	rm -rf ${DIR}/../wso2/components/default ${DIR}/../wso2/components/device-backend ${DIR}/../wso2/components/http-gateway ${DIR}/../wso2/components/device-manager
	rm -rf ${DIR}/../samples/
	rm -rf ${DIR}/../plugins
	rm -rf ${DIR}/profile-creator.sh
	rm -rf ${DIR}/profile-creator.bat
	cp -rf ${DIR}/../repository/resources/profiles/keymanager/*.sh ${DIR}/../bin/
	cp -rf ${DIR}/../repository/resources/profiles/keymanager/*.bat ${DIR}/../bin/
	cp -rf ${DIR}/../repository/resources/profiles/keymanager/carbon.xml ${DIR}/../conf/
	cp -rf ${DIR}/../repository/resources/profiles/keymanager/identity/application-authentication.xml ${DIR}/../conf/identity/
	rm -rf ${DIR}/../repository/deployment/server/jaggeryapps/*
	rm -rf ${DIR}/../repository/deployment/server/synapse-configs/default/api/*
	rm -rf ${DIR}/../repository/deployment/server/synapse-configs/default/sequences/_*.xml
	mkdir ${DIR}/../repository/deployment/server/tempwebapp
	cp ${DIR}/../repository/deployment/server/webapps/oauth2.war ${DIR}/../repository/deployment/server/tempwebapp/
	cp ${DIR}/../repository/deployment/server/webapps/throttle#data#v1.war ${DIR}/../repository/deployment/server/tempwebapp/
	cp ${DIR}/../repository/deployment/server/webapps/client-registration#v0.11.war ${DIR}/../repository/deployment/server/tempwebapp/
	cp ${DIR}/../repository/deployment/server/webapps/dynamic-client-web.war ${DIR}/../repository/deployment/server/tempwebapp/
	cp ${DIR}/../repository/deployment/server/webapps/authenticationendpoint.war ${DIR}/../repository/deployment/server/tempwebapp/
	rm -rf ${DIR}/../repository/deployment/server/webapps/*
	cp -rf ${DIR}/../repository/deployment/server/tempwebapp/* ${DIR}/../repository/deployment/server/webapps/
	rm -rf ${DIR}/../repository/deployment/server/tempwebapp
	rm -rf ${DIR}/../repository/deployment/server/carbonapps/*
	rm -rf ${DIR}/../repository/deployment/server/axis2services/*
	rm -rf ${DIR}/../repository/deployment/server/devicetypes/*

    PROFILE="_keymanager"

#device backend profile
elif [ ${profileNumber} -eq 3 ]
then
	echo "Preparing the IoT Device Backend profile distribution"
	DEFAULT_BUNDLES="$(< ${DIR}/../wso2/components/device-backend/configuration/org.eclipse.equinox.simpleconfigurator/bundles.info)"
	#remove broker
	echo "Removing Broker profile"
	rm -rf ${DIR}/../wso2/broker
	rm -rf ${DIR}/../wso2/components/broker-default
	rm -rf ${DIR}/broker.bat
	rm -rf ${DIR}/broker.sh

	#remove analytics
	echo "Removing Analytics profile"
	rm -rf ${DIR}/../wso2/analytics
	rm -rf ${DIR}/../wso2/components/analytics-default
	rm -rf ${DIR}/analytics.bat
	rm -rf ${DIR}/analytics.sh

    #remove iot
	echo "Removing IoT Other profiles"
	rm -rf ${DIR}/../wso2/components/default ${DIR}/../wso2/components/device-key-manager ${DIR}/../wso2/components/http-gateway ${DIR}/../wso2/components/device-manager
	rm -rf ${DIR}/../samples/
	rm -rf ${DIR}/../plugins
	rm -rf ${DIR}/profile-creator.sh
	rm -rf ${DIR}/profile-creator.bat
	rm -rf ${DIR}/../repository/deployment/server/synapse-configs/default/api/*
	rm -rf ${DIR}/../repository/deployment/server/synapse-configs/default/sequences/_*.xml
	cp -rf ${DIR}/../repository/resources/profiles/backend/*.sh ${DIR}/../bin/
	cp -rf ${DIR}/../repository/resources/profiles/backend/*.bat ${DIR}/../bin/
	cp -rf ${DIR}/../repository/resources/profiles/backend/carbon.xml ${DIR}/../conf/
	rm -rf ${DIR}/../repository/deployment/server/webapps/oauth2.war ${DIR}/../repository/deployment/server/webapps/shindig.war ${DIR}/../repository/deployment/server/webapps/api#am#publisher#v0.11.war ${DIR}/../repository/deployment/server/webapps/api#am#store#v0.11.war ${DIR}/../repository/deployment/server/webapps/api#appm#oauth#v1.0.war ${DIR}/../repository/deployment/server/webapps/api#appm#publisher#v1.1.war ${DIR}/../repository/deployment/server/webapps/api#appm#store#v1.1.war
	rm -rf ${DIR}/../repository/deployment/server/webapps/dynamic-client-web.war ${DIR}/../repository/deployment/server/webapps/client-registration#v0.11.war
	rm -rf ${DIR}/../repository/deployment/server/jaggeryapps/*
	rm -rf ${DIR}/../repository/deployment/server/axis2services/*
	rm -rf ${DIR}/../conf/identity/sso-idp-config.xml

    PROFILE="_device-backend"

#device manager profile
elif [ ${profileNumber} -eq 4 ]
then
	echo "Preparing the IoT Device Manager profile distribution"
	DEFAULT_BUNDLES="$(< ${DIR}/../wso2/components/device-manager/configuration/org.eclipse.equinox.simpleconfigurator/bundles.info)"
	#remove broker
	echo "Removing Broker profile"
	rm -rf ${DIR}/../wso2/broker
	rm -rf ${DIR}/../wso2/components/broker-default
	rm -rf ${DIR}/broker.bat
	rm -rf ${DIR}/broker.sh

	#remove analytics
	echo "Removing Analytics profile"
	rm -rf ${DIR}/../wso2/analytics
	rm -rf ${DIR}/../wso2/components/analytics-default
	rm -rf ${DIR}/analytics.bat
	rm -rf ${DIR}/analytics.sh

    #remove iot
	echo "Removing IoT Other profiles"
	rm -rf ${DIR}/../wso2/components/default ${DIR}/../wso2/components/device-key-manager ${DIR}/../wso2/components/http-gateway ${DIR}/../wso2/components/device-backend
	rm -rf ${DIR}/../samples/
	rm -rf ${DIR}/../plugins
	rm -rf ${DIR}/profile-creator.sh
	rm -rf ${DIR}/profile-creator.bat
	cp -rf ${DIR}/../repository/resources/profiles/manager/*.sh ${DIR}/../bin/
	cp -rf ${DIR}/../repository/resources/profiles/manager/*.bat ${DIR}/../bin/
	cp -rf ${DIR}/../repository/resources/profiles/manager/carbon.xml ${DIR}/../conf/
	mkdir ${DIR}/../repository/deployment/server/tempwebapp
	cp ${DIR}/../repository/deployment/server/webapps/api#am#publisher#v0.11.war ${DIR}/../repository/deployment/server/tempwebapp/
	cp ${DIR}/../repository/deployment/server/webapps/api#am#store#v0.11.war ${DIR}/../repository/deployment/server/tempwebapp/
	cp ${DIR}/../repository/deployment/server/webapps/api#appm#oauth#v1.0.war ${DIR}/../repository/deployment/server/tempwebapp/
	cp ${DIR}/../repository/deployment/server/webapps/api#appm#publisher#v1.1.war ${DIR}/../repository/deployment/server/tempwebapp/
	cp ${DIR}/../repository/deployment/server/webapps/api#appm#store#v1.1.war ${DIR}/../repository/deployment/server/tempwebapp/
	cp ${DIR}/../repository/deployment/server/webapps/shindig.war ${DIR}/../repository/deployment/server/tempwebapp/
	rm -rf ${DIR}/../repository/deployment/server/webapps/*
	cp -rf ${DIR}/../repository/deployment/server/tempwebapp/* ${DIR}/../repository/deployment/server/webapps/
	rm -rf ${DIR}/../repository/deployment/server/tempwebapp
	rm -rf ${DIR}/../repository/deployment/server/axis2services/*
	rm -rf ${DIR}/../repository/deployment/server/synapse-configs/default/api/*
	rm -rf ${DIR}/../repository/deployment/server/synapse-configs/default/sequences/_*.xml
	rm -rf ${DIR}/../conf/identity/sso-idp-config.xml

    PROFILE="_device-manager"

#Analytics profile
elif [ ${profileNumber} -eq 5 ]
then
	echo "Preparing the Analytics profile distribution"
	DEFAULT_BUNDLES="$(< ${DIR}/../wso2/components/analytics-default/configuration/org.eclipse.equinox.simpleconfigurator/bundles.info)"
	#remove broker
	echo "Removing Broker profile"
	rm -rf ${DIR}/../wso2/broker
	rm -rf ${DIR}/../wso2/components/broker-default
	rm -rf ${DIR}/broker.bat
	rm -rf ${DIR}/broker.sh

	#remove intergrator
	echo "Removing IoT profiles"
	rm -rf ${DIR}/../conf
	rm -rf ${DIR}/../wso2/components/default ${DIR}/../wso2/components/device-backend ${DIR}/../wso2/components/device-key-manager ${DIR}/../wso2/components/device-manager ${DIR}/../wso2/components/http-gateway
	rm -rf ${DIR}/../samples/
	rm -rf ${DIR}/../modules
	rm -rf ${DIR}/../dbscripts
	rm -rf ${DIR}/../plugins
	rm -rf ${DIR}/../repository
	rm -rf ${DIR}/../resources
	rm -rf ${DIR}/../tmp
	rm -rf ${DIR}/profile-creator.sh
	rm -rf ${DIR}/profile-creator.bat
	rm -rf ${DIR}/iot-server.bat
	rm -rf ${DIR}/iot-server.sh

	echo "Copying configurations"
	cp -rf ${DIR}/../wso2/analytics/repository/resources/profile/master-datasources.xml ${DIR}/../wso2/analytics/conf/datasources

	PROFILE="_analytics"

elif [ ${profileNumber} -eq 6 ]
then
	echo "Preparing the Broker profile distribution"
	DEFAULT_BUNDLES="$(< ${DIR}/../wso2/components/broker-default/configuration/org.eclipse.equinox.simpleconfigurator/bundles.info)"
	#remove analytics
	echo "Removing Analytics profile"
	rm -rf ${DIR}/../wso2/analytics
	rm -rf ${DIR}/../wso2/components/analytics-default
	rm -rf ${DIR}/analytics.bat
	rm -rf ${DIR}/analytics.sh

	#remove iot
	echo "Removing IoT profiles"
    rm -rf ${DIR}/../conf
	rm -rf ${DIR}/../wso2/components/default ${DIR}/../wso2/components/device-backend ${DIR}/../wso2/components/device-key-manager ${DIR}/../wso2/components/device-manager ${DIR}/../wso2/components/http-gateway
	rm -rf ${DIR}/../samples/
	rm -rf ${DIR}/../modules
	rm -rf ${DIR}/../dbscripts
	rm -rf ${DIR}/../plugins
	rm -rf ${DIR}/../repository
	rm -rf ${DIR}/../resources
	rm -rf ${DIR}/../tmp
	rm -rf ${DIR}/profile-creator.sh
	rm -rf ${DIR}/profile-creator.bat
	rm -rf ${DIR}/iot-server.bat
	rm -rf ${DIR}/iot-server.sh

	PROFILE="_broker"
else
	echo "Invalid profile number. Terminating."
	exit 3
fi



#remove unnecessary jar files
echo "Removing unnecessary jars from plugins folder."
mkdir -p ${DIR}/../wso2/components/tmp_plugins

for BUNDLE in $DEFAULT_BUNDLES; do
    IFS=',' read -a bundleArray <<< "$BUNDLE"
    JAR=${bundleArray[0]}_${bundleArray[1]}.jar
    cp ${DIR}/../wso2/components/plugins/${JAR} ${DIR}/../wso2/components/tmp_plugins 2> /dev/null
done

rm -r ${DIR}/../wso2/components/plugins
mv ${DIR}/../wso2/components/tmp_plugins ${DIR}/../wso2/components/plugins

echo "Preparing a profile distribution archive."
cd ${DIR}/../../
rm -rf ${DISTRIBUTION}${PROFILE}
mv ${DISTRIBUTION} ${DISTRIBUTION}${PROFILE}
zip -r ${DISTRIBUTION}${PROFILE}.zip ${DISTRIBUTION}${PROFILE}/
cd ${BINDIR}
DIR=${BINDIR}
echo "Profile created successfully in "$(pwd)"/"${DISTRIBUTION}${PROFILE}
}

if [ ${profileNumber} -eq 7 ]; then
    profileNumber=1
    while [  "$profileNumber" -lt 7 ]
        do
            create_profile
            ((profileNumber++))
    done
    echo "All profiles are created successfully!"
else
    create_profile
fi

exit 0
