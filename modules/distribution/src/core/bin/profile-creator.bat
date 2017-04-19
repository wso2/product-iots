@echo OFF

REM ---------------------------------------------------------------------------
REM        Copyright 2017 WSO2, Inc. http://www.wso2.org
REM
REM  Licensed under the Apache License, Version 2.0 (the "License");
REM  you may not use this file except in compliance with the License.
REM  You may obtain a copy of the License at
REM
REM      http://www.apache.org/licenses/LICENSE-2.0
REM
REM  Unless required by applicable law or agreed to in writing, software
REM  distributed under the License is distributed on an "AS IS" BASIS,
REM  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
REM  See the License for the specific language governing permissions and
REM  limitations under the License.
REM ---------------------------------------------------------------------------
REM Profile creator tool for EI
REM ---------------------------------------------------------------------------

set DIR=%~dp0
set DISTRIBUTION=wso2iot-@product.version@
REM get the desired profile
echo This tool will erase all the files which are not required for the selected profile.
echo WSO2 IoT Server Supports following profiles.
echo 	1.IoT Gateway Profile
echo 	2.IoT Key Manager Profile
echo 	3.IoT Device Backend Profile
echo 	4.IoT Device Manager Profile
echo 	5.Analytics Profile
echo 	6.Broker profile
echo 	7.For All Profiles

set /p profileNumber= [Please enter the desired profile number to create the profile specific distribution]

IF /I "%profileNumber%" EQU "1" goto Gateway
IF /I "%profileNumber%" EQU "2" goto KeyManager
IF /I "%profileNumber%" EQU "3" goto Backend
IF /I "%profileNumber%" EQU "4" goto Manager
IF /I "%profileNumber%" EQU "5" goto Analytics
IF /I "%profileNumber%" EQU "5" goto Broker
IF /I "%profileNumber%" EQU "5" goto All

echo Invalid profile identifier.
goto Exit

:Gateway
	echo Preparing the Gateway profile distribution.
	set DEFAULT_BUNDLES=%DIR%..\wso2\components\http-gateway\configuration\org.eclipse.equinox.simpleconfigurator\bundles.info
    call :Remove_BROKER
    call :Remove_ANALYTICS
    call :Remove_JARS
    IF EXIST %DIR%\..\wso2\components\default @RD /S /Q  %DIR%\..\wso2\components\default
    IF EXIST %DIR%\..\wso2\components\device-backend @RD /S /Q %DIR%\..\wso2\components\device-backend
    IF EXIST %DIR%\..\wso2\components\device-key-manager @RD /S /Q %DIR%\..\wso2\components\device-key-manager
    IF EXIST %DIR%\..\wso2\components\device-manager @RD /S /Q %DIR%\..\wso2\components\device-manager
    IF EXIST %DIR%\..\samples @RD /S /Q %DIR%\..\samples
    IF EXIST %DIR%\..\plugins @RD /S /Q %DIR%\..\plugins
    IF EXIST %DIR%\profile-creator.sh del %DIR%\profile-creator.sh
    IF EXIST %DIR%\profile-creator.bat del %DIR%\profile-creator.bat
    for /R %DIR%\..\repository\resources\profiles\gateway %%f in (*.sh) do copy %%f %DIR%\..\bin\
    copy /y %DIR%\..\repository\resources\profiles\gateway\carbon.xml %DIR%\..\conf\
    IF EXIST %DIR%\..\repository\deployment\server\jaggeryapps del %DIR%\..\repository\deployment\server\jaggeryapps\*
    IF EXIST %DIR%\..\repository\deployment\server\webapps del %DIR%\..\repository\deployment\server\webapps\*
    IF EXIST %DIR%\..\repository\deployment\server\carbonapps del %DIR%\..\repository\deployment\server\carbonapps\*
    IF EXIST %DIR%\..\repository\deployment\server\axis2services @RD /S /Q %DIR%\..\repository\deployment\server\axis2services\*
    IF EXIST %DIR%\..\repository\deployment\server\devicetypes @RD /S /Q %DIR%\..\repository\deployment\server\devicetypes\*
    IF EXIST %DIR%\..\conf\identity\sso-idp-config.xml del %DIR%\..\conf\identity\sso-idp-config.xml
    echo Gateway profile created successfully.
	goto Exit

:KeyManager
	echo Preparing the Gateway profile distribution.
	set DEFAULT_BUNDLES=%DIR%..\wso2\components\http-gateway\configuration\org.eclipse.equinox.simpleconfigurator\bundles.info
    call :Remove_BROKER
    call :Remove_ANALYTICS
    call :Remove_JARS
    IF EXIST %DIR%\..\wso2\components\default @RD /S /Q  %DIR%\..\wso2\components\default
    IF EXIST %DIR%\..\wso2\components\http-gateway @RD /S /Q %DIR%\..\wso2\components\http-gateway
    IF EXIST %DIR%\..\wso2\components\device-backend @RD /S /Q %DIR%\..\wso2\components\device-backend
    IF EXIST %DIR%\..\wso2\components\device-manager @RD /S /Q %DIR%\..\wso2\components\device-manager
    IF EXIST %DIR%\..\samples @RD /S /Q %DIR%\..\samples
    IF EXIST %DIR%\..\plugins @RD /S /Q %DIR%\..\plugins
    IF EXIST %DIR%\profile-creator.sh del %DIR%\profile-creator.sh
    IF EXIST %DIR%\profile-creator.bat del %DIR%\profile-creator.bat
    for /R %DIR%\..\repository\resources\profiles\keymanager %%f in (*.sh) do copy %%f %DIR%\..\bin\
    copy /y %DIR%\..\repository\resources\profiles\keymanager\carbon.xml %DIR%\..\conf\
    copy /y %DIR%\..\repository\resources\profiles\keymanager\identity\application-authentication.xml %DIR%\..\conf\identity\
    IF EXIST %DIR%\..\repository\deployment\server\jaggeryapps del %DIR%\..\repository\deployment\server\jaggeryapps\*
    IF EXIST %DIR%\..\repository\deployment\server\carbonapps del %DIR%\..\repository\deployment\server\carbonapps\*
    IF EXIST %DIR%\..\repository\deployment\server\axis2services @RD /S /Q %DIR%\..\repository\deployment\server\axis2services\*
    IF EXIST %DIR%\..\repository\deployment\server\devicetypes @RD /S /Q %DIR%\..\repository\deployment\server\devicetypes\*
    IF EXIST %DIR%\..\repository\deployment\server\synapse-configs\default\api @RD /S /Q %DIR%\..\repository\deployment\server\synapse-configs\default\api\*
    IF EXIST %DIR%\..\repository\deployment\server\synapse-configs\default\sequences @RD /S /Q %DIR%\..\repository\deployment\server\synapse-configs\default\sequences\_*.xml
    mkdir %DIR%\..\repository\deployment\server\tempwebapp
    copy /y %DIR%\..\repository\deployment\server\webapps\oauth2.war %DIR%\..\repository\deployment\server\tempwebapp\
    copy /y %DIR%\..\repository\deployment\server\webapps\client-registration#v0.11.war %DIR%\..\repository\deployment\server\tempwebapp\
    copy /y %DIR%\..\repository\deployment\server\webapps\dynamic-client-web.war %DIR%\..\repository\deployment\server\tempwebapp\
    copy /y %DIR%\..\repository\deployment\server\webapps\authenticationendpoint.war %DIR%\..\repository\deployment\server\tempwebapp\
    IF EXIST %DIR%\..\repository\deployment\server\webapps del %DIR%\..\repository\deployment\server\webapps\*
    echo Gateway profile created successfully.
	goto Exit

:Backend
	echo Preparing the Gateway profile distribution.
	set DEFAULT_BUNDLES=%DIR%..\wso2\components\device-backend\configuration\org.eclipse.equinox.simpleconfigurator\bundles.info
    call :Remove_BROKER
    call :Remove_ANALYTICS
    call :Remove_JARS
    IF EXIST %DIR%\..\wso2\components\default @RD /S /Q  %DIR%\..\wso2\components\default
    IF EXIST %DIR%\..\wso2\components\http-gateway @RD /S /Q %DIR%\..\wso2\components\http-gateway
    IF EXIST %DIR%\..\wso2\components\device-key-manager @RD /S /Q %DIR%\..\wso2\components\device-key-manager
    IF EXIST %DIR%\..\wso2\components\device-manager @RD /S /Q %DIR%\..\wso2\components\device-manager
    IF EXIST %DIR%\..\samples @RD /S /Q %DIR%\..\samples
    IF EXIST %DIR%\..\plugins @RD /S /Q %DIR%\..\plugins
    IF EXIST %DIR%\profile-creator.sh del %DIR%\profile-creator.sh
    IF EXIST %DIR%\profile-creator.bat del %DIR%\profile-creator.bat
    for /R %DIR%\..\repository\resources\profiles\backend %%f in (*.sh) do copy %%f %DIR%\..\bin\
    copy /y %DIR%\..\repository\resources\profiles\backend\carbon.xml %DIR%\..\conf\
    IF EXIST %DIR%\..\repository\deployment\server\synapse-configs\default\api @RD /S /Q %DIR%\..\repository\deployment\server\synapse-configs\default\api\*
    IF EXIST %DIR%\..\repository\deployment\server\synapse-configs\default\sequences @RD /S /Q %DIR%\..\repository\deployment\server\synapse-configs\default\sequences\_*.xml    
    IF EXIST %DIR%\..\repository\deployment\server\jaggeryapps del %DIR%\..\repository\deployment\server\jaggeryapps\*
    IF EXIST %DIR$%\..\repository\deployment\server\webapps\oauth2.war del %DIR$%\..\repository\deployment\server\webapps\oauth2.war
    IF EXIST %DIR$%\..\repository\deployment\server\webapps\shindig.war del %DIR$%\..\repository\deployment\server\webapps\shindig.war
    IF EXIST %DIR$%\..\repository\deployment\server\webapps\api#am#publisher#v0.11.war del %DIR$%\..\repository\deployment\server\webapps\api#am#publisher#v0.11.war
    IF EXIST %DIR$%\..\repository\deployment\server\webapps\api#am#store#v0.11.war del %DIR$%\..\repository\deployment\server\webapps\api#am#store#v0.11.war
    IF EXIST %DIR$%\..\repository\deployment\server\webapps\api#appm#oauth#v1.0.war del %DIR$%\..\repository\deployment\server\webapps\api#appm#oauth#v1.0.war
    IF EXIST %DIR$%\..\repository\deployment\server\webapps\api#appm#publisher#v1.1.war del %DIR$%\..\repository\deployment\server\webapps\api#appm#publisher#v1.1.war
    IF EXIST %DIR$%\..\repository\deployment\server\webapps\api#appm#store#v1.1.war del %DIR$%\..\repository\deployment\server\webapps\api#appm#store#v1.1.war
    IF EXIST %DIR$%\..\repository\deployment\server\webapps\dynamic-client-web.war del %DIR$%\..\repository\deployment\server\webapps\dynamic-client-web.war
    IF EXIST %DIR$%\..\repository\deployment\server\webapps\client-registration#v0.11.war del %DIR$%\..\repository\deployment\server\webapps\client-registration#v0.11.war
    IF EXIST %DIR%\..\repository\deployment\server\carbonapps del %DIR%\..\repository\deployment\server\carbonapps\*
    IF EXIST %DIR%\..\repository\deployment\server\axis2services @RD /S /Q %DIR%\..\repository\deployment\server\axis2services\*
    IF EXIST %DIR%\..\repository\deployment\server\devicetypes @RD /S /Q %DIR%\..\repository\deployment\server\devicetypes\*
    IF EXIST %DIR%\..\conf\identity\sso-idp-config.xml del %DIR%\..\conf\identity\sso-idp-config.xml
    echo Backend profile created successfully.
	goto Exit


:Analytics
    echo Preparing the Analytics profile.
    set DEFAULT_BUNDLES=%DIR%..\wso2\components\analytics-default\configuration\org.eclipse.equinox.simpleconfigurator\bundles.info
    call :Remove_IoT
    call :Remove_BROKER
    call :Remove_JARS
    echo Analytics profile created successfully.
    goto Exit

:Broker
    echo Preparing the Broker profile.
    set DEFAULT_BUNDLES=%DIR%..\wso2\components\broker-default\configuration\org.eclipse.equinox.simpleconfigurator\bundles.info
    call :Remove_IOT
    call :Remove_ANALYTICS
    call :Remove_JARS
    echo Broker profile created successfully.
    goto Exit



:Remove_BROKER
    echo Removing Broker profile
    IF EXIST %DIR%\..\wso2\broker @RD /S /Q %DIR%\..\wso2\broker
    IF EXIST %DIR%\..\wso2\components\broker-default @RD /S /Q %DIR%\..\wso2\components\broker-default
    IF EXIST %DIR%\broker.bat del %DIR%\broker.bat
    IF EXIST %DIR%\broker.sh del %DIR%\broker.sh
    goto :eof

:Remove_ANALYTICS
    echo Removing Analytics profile
    IF EXIST %DIR%\..\wso2\analytics @RD /S /Q %DIR%\..\wso2\analytics
    IF EXIST %DIR%\..\wso2\components\analytics-default @RD /S /Q %DIR%\..\wso2\components\analytics-default
    IF EXIST %DIR%\..\wso2\components\analytics-worker @RD /S /Q %DIR%\..\wso2\components\analytics-worker
    IF EXIST %DIR%\analytics.bat del %DIR%\analytics.bat
    IF EXIST %DIR%\analytics.sh del %DIR%\analytics.sh
    goto :eof

:Remove_IoT
    echo Removing Integrator profile
    IF EXIST %DIR%\..\conf @RD /S /Q %DIR%\..\conf
    IF EXIST %DIR%\..\wso2\components\default @RD /S /Q %DIR%\..\wso2\components\default
    IF EXIST %DIR%\..\wso2\components\device-manager @RD /S /Q %DIR%\..\wso2\components\device-manager
    IF EXIST %DIR%\..\wso2\components\device-key-manager @RD /S /Q %DIR%\..\wso2\components\device-key-manager
    IF EXIST %DIR%\..\wso2\components\device-backend @RD /S /Q %DIR%\..\wso2\components\device-backend
    IF EXIST %DIR%\..\wso2\components\http-gateway @RD /S /Q %DIR%\..\wso2\components\http-gateway
    IF EXIST %DIR%\..\samples @RD /S /Q %DIR% %DIR%\..\samples
    IF EXIST %DIR%\..\modules @RD /S /Q %DIR% %DIR%\..\modules
    IF EXIST %DIR%\..\dbscripts @RD /S /Q %DIR% %DIR%\..\dbscripts
    IF EXIST %DIR%\..\plugins @RD /S /Q %DIR% %DIR%\..\plugins
    IF EXIST %DIR%\..\repository @RD /S /Q %DIR% %DIR%\..\repository
    IF EXIST %DIR%\..\tmp @RD /S /Q %DIR% %DIR%\..\tmp
    IF EXIST %DIR%\..\resources @RD /S /Q %DIR% %DIR%\..\resources
    IF EXIST %DIR%\profile-creator.bat del %DIR%\profile-creator.bat
    IF EXIST %DIR%\profile-creator.sh del %DIR%\profile-creator.sh
    IF EXIST %DIR%\iot-server.bat del %DIR%\iot-server.bat
    IF EXIST %DIR%\iot-server.sh del %DIR%\iot-server.sh
    goto :eof

:Remove_JARS
    echo Removing unnecessary jars
    mkdir %DIR%\..\wso2\components\tmp_plugins

    FOR /F "tokens=1,2* delims=, " %%i in (%DEFAULT_BUNDLES%) do copy %DIR%\..\wso2\components\plugins\%%i_%%j.jar %DIR%\..\wso2\components\tmp_plugins

    @RD /S /Q %DIR%\..\wso2\components\plugins
    rename %DIR%\..\wso2\components\tmp_plugins plugins
    goto :eof


:Exit
    pause
