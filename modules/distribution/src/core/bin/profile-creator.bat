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
echo This tool will erase all the files which are not required for the selected profile
echo and provide you a light weight package for the target profile.
echo WSO2 IoT Server Supports following profiles.
echo 	1.IoT Device Backend Profile
echo 	2.IoT Device Manager Profile
echo 	3.IoT Key Manager Profile
echo 	4.Analytics Profile
echo 	5.Broker profile
echo 	6.All Profiles

set /p profileNumber= [Please enter the desired profile number to create the profile specific distribution]

IF /I "%profileNumber%" EQU "1" goto Backend
IF /I "%profileNumber%" EQU "2" goto Manager
IF /I "%profileNumber%" EQU "3" goto KeyManager
IF /I "%profileNumber%" EQU "4" goto Analytics
IF /I "%profileNumber%" EQU "5" goto Broker
IF /I "%profileNumber%" EQU "6" goto All

echo Invalid profile identifier.

:COPY_DIST
set TEMPDIR=%DIR%..\..\target
IF NOT EXIST %TEMPDIR% mkdir %TEMPDIR%
IF EXIST %TEMPDIR%\%DISTRIBUTION% @RD /S /Q %TEMPDIR%\%DISTRIBUTION%
IF EXIST %TEMPDIR%\%DISTRIBUTION%%PROFILE% @RD /S /Q %TEMPDIR%\%DISTRIBUTION%%PROFILE%
xcopy %DIR%..\..\%DISTRIBUTION% %TEMPDIR%\%DISTRIBUTION%\ /s /e /h
set DIR=%TEMPDIR%\%DISTRIBUTION%\bin\
goto :eof


:KeyManager
	echo Preparing the KeyManager profile distribution.
	SET PROFILE=_keymanager
	call :COPY_DIST
	set DEFAULT_BUNDLES=%DIR%..\wso2\components\device-key-manager\configuration\org.eclipse.equinox.simpleconfigurator\bundles.info
    call :Remove_BROKER
    call :Remove_ANALYTICS
    call :Remove_JARS
    IF EXIST %DIR%..\wso2\components\default @RD /S /Q  %DIR%..\wso2\components\default
    IF EXIST %DIR%..\wso2\components\device-backend @RD /S /Q %DIR%..\wso2\components\device-backend
    IF EXIST %DIR%..\wso2\components\device-manager @RD /S /Q %DIR%..\wso2\components\device-manager
    IF EXIST %DIR%..\samples @RD /S /Q %DIR%..\samples
    IF EXIST %DIR%..\plugins @RD /S /Q %DIR%..\plugins
    for /R %DIR%..\repository\resources\profiles\keymanager %%f in (*.sh) do copy %%f %DIR%..\bin\
	for /R %DIR%..\repository\resources\profiles\keymanager %%f in (*.bat) do copy %%f %DIR%..\bin\
    copy /y %DIR%..\repository\resources\profiles\keymanager\carbon.xml %DIR%..\conf\
    copy /y %DIR%..\repository\resources\profiles\keymanager\identity\application-authentication.xml %DIR%..\conf\identity\
    copy /y %DIR%..\repository\resources\profiles\keymanager\tomcat\context.xml %DIR%..\conf\tomcat\
    copy /y %DIR%..\repository\resources\profiles\keymanager\axis2\axis2.xml %DIR%..\conf\axis2\
    IF EXIST %DIR%..\repository\deployment\server\jaggeryapps @RD /S /Q %DIR%..\repository\deployment\server\jaggeryapps
    IF EXIST %DIR%..\repository\deployment\server\carbonapps @RD /S /Q %DIR%..\repository\deployment\server\carbonapps
    IF EXIST %DIR%..\repository\deployment\server\axis2services @RD /S /Q %DIR%..\repository\deployment\server\axis2services
    IF EXIST %DIR%..\repository\deployment\server\devicetypes @RD /S /Q %DIR%..\repository\deployment\server\devicetypes
    IF EXIST %DIR%..\repository\deployment\server\synapse-configs @RD /S /Q %DIR%..\repository\deployment\server\synapse-configs
    IF EXIST %DIR%..\conf\cdm-config.xml del %DIR%..\conf\cdm-config.xml
    IF EXIST %DIR%..\conf\app-manager.xml del %DIR%..\conf\app-manager.xml
    If EXIST %DIR%..\conf\analytics @RD /S /Q %DIR%..\conf\analytics
    IF EXIST %DIR%..\conf\apim-integration.xml del %DIR%..\conf\apim-integration.xml
    IF EXIST %DIR%..\conf\certificate-config.xml del %DIR%..\conf\certificate-config.xml
    IF EXIST %DIR%..\conf\data-bridge @RD /S /Q %DIR%..\conf\data-bridge
    IF EXIST %DIR%..\conf\governance.xml del %DIR%..\conf\governance.xml
    IF EXIST %DIR%..\conf\input-event-adapters.xml del %DIR%..\conf\input-event-adapters.xml
    IF EXIST %DIR%..\conf\iot-api-config.xml del %DIR%..\conf\iot-api-config.xml
    IF EXIST %DIR%..\conf\mobile-config.xml del %DIR%..\conf\mobile-config.xml
    IF EXIST %DIR%..\conf\nhttp.properties del %DIR%..\conf\nhttp.properties
    IF EXIST %DIR%..\conf\output-event-adapters.xml del %DIR%..\conf\output-event-adapters.xml
    IF EXIST %DIR%..\conf\registry-event-broker.xml del %DIR%..\conf\registry-event-broker.xml
    IF EXIST %DIR%..\conf\social.xml del %DIR%..\conf\social.xml
    IF EXIST %DIR%..\conf\synapse-handlers.xml del %DIR%..\conf\synapse-handlers.xml
    IF EXIST %DIR%..\conf\synapse.properties del %DIR%..\conf\synapse.properties
    IF EXIST %DIR%..\conf\passthru-http.properties del %DIR%..\conf\passthru-http.properties
    IF EXIST %DIR%..\conf\registry-event-broker.xml del %DIR%..\conf\registry-event-broker.xml
    mkdir %DIR%..\repository\deployment\server\tempwebapp
    copy /y %DIR%..\repository\deployment\server\webapps\oauth2.war %DIR%..\repository\deployment\server\tempwebapp\
    copy /y %DIR%..\repository\deployment\server\webapps\client-registration#v0.11.war %DIR%..\repository\deployment\server\tempwebapp\
    copy /y %DIR%..\repository\deployment\server\webapps\dynamic-client-web.war %DIR%..\repository\deployment\server\tempwebapp\
    copy /y %DIR%..\repository\deployment\server\webapps\authenticationendpoint.war %DIR%..\repository\deployment\server\tempwebapp\
    IF EXIST %DIR%..\repository\deployment\server\webapps @RD /S /Q %DIR%..\repository\deployment\server\webapps
	mkdir %DIR%..\repository\deployment\server\jaggeryapps
	mkdir %DIR%..\repository\deployment\server\carbonapps
	mkdir %DIR%..\repository\deployment\server\axis2services
	mkdir %DIR%..\repository\deployment\server\devicetypes
	mkdir %DIR%..\repository\deployment\server\webapps
	for /R %DIR%..\repository\deployment\server\tempwebapp %%f in (*.war) do copy %%f %DIR%..\repository\deployment\server\webapps\
	IF EXIST %DIR%..\repository\deployment\server\tempwebapp @RD /S /Q %DIR%..\repository\deployment\server\tempwebapp
	IF EXIST %DIR%start-all.bat del %DIR%start-all.bat
	IF EXIST %DIR%start-all.sh del %DIR%start-all.sh
	IF EXIST %DIR%stop-all.bat del %DIR%stop-all.bat
	IF EXIST %DIR%stop-all.sh del %DIR%stop-all.sh
	call :RENAME_DIST
    echo Key Manager profile created successfully in %TEMPDIR%\%DISTRIBUTION%%PROFILE%.
	goto Exit

:Backend
	echo Preparing the Device Backend profile distribution.
	SET PROFILE=_device-backend
	call :COPY_DIST
	set DEFAULT_BUNDLES=%DIR%..\wso2\components\device-backend\configuration\org.eclipse.equinox.simpleconfigurator\bundles.info
    call :Remove_BROKER
    call :Remove_ANALYTICS
    call :Remove_JARS
    IF EXIST %DIR%..\wso2\components\default @RD /S /Q  %DIR%..\wso2\components\default
    IF EXIST %DIR%..\wso2\components\device-key-manager @RD /S /Q %DIR%..\wso2\components\device-key-manager
    IF EXIST %DIR%..\wso2\components\device-manager @RD /S /Q %DIR%..\wso2\components\device-manager
    IF EXIST %DIR%..\samples @RD /S /Q %DIR%..\samples
    IF EXIST %DIR%..\plugins @RD /S /Q %DIR%..\plugins
	for /R %DIR%..\repository\resources\profiles\backend %%f in (*.sh) do copy %%f %DIR%..\bin\
    for /R %DIR%..\repository\resources\profiles\backend %%f in (*.bat) do copy %%f %DIR%..\bin\
	copy /y %DIR%..\repository\resources\profiles\backend\carbon.xml %DIR%..\conf\
    IF EXIST %DIR%..\repository\deployment\server\jaggeryapps @RD /S /Q %DIR%..\repository\deployment\server\jaggeryapps
	IF EXIST %DIR%..\repository\deployment\server\axis2services @RD /S /Q %DIR%..\repository\deployment\server\axis2services
	IF EXIST %DIR%..\repository\deployment\server\webapps\shindig.war del %DIR%..\repository\deployment\server\webapps\shindig.war
    IF EXIST %DIR%..\repository\deployment\server\webapps\api#am#publisher#v0.11.war del %DIR%..\repository\deployment\server\webapps\api#am#publisher#v0.11.war
    IF EXIST %DIR%..\repository\deployment\server\webapps\api#am#store#v0.11.war del %DIR%..\repository\deployment\server\webapps\api#am#store#v0.11.war
    IF EXIST %DIR%..\repository\deployment\server\webapps\api#appm#oauth#v1.0.war del %DIR%..\repository\deployment\server\webapps\api#appm#oauth#v1.0.war
    IF EXIST %DIR%..\repository\deployment\server\webapps\api#appm#publisher#v1.1.war del %DIR%..\repository\deployment\server\webapps\api#appm#publisher#v1.1.war
    IF EXIST %DIR%..\repository\deployment\server\webapps\api#appm#store#v1.1.war del %DIR%..\repository\deployment\server\webapps\api#appm#store#v1.1.war
    IF EXIST %DIR%..\repository\deployment\server\webapps\dynamic-client-web.war del %DIR%..\repository\deployment\server\webapps\dynamic-client-web.war
    IF EXIST %DIR%..\repository\deployment\server\webapps\client-registration#v0.11.war del %DIR%..\repository\deployment\server\webapps\client-registration#v0.11.war
	mkdir %DIR%..\repository\deployment\server\jaggeryapps
	mkdir %DIR%..\repository\deployment\server\axis2services
    IF EXIST %DIR%..\conf\identity\sso-idp-config.xml del %DIR%..\conf\identity\sso-idp-config.xml
	IF EXIST %DIR%start-all.bat del %DIR%start-all.bat
	IF EXIST %DIR%start-all.sh del %DIR%start-all.sh
	IF EXIST %DIR%stop-all.bat del %DIR%stop-all.bat
	IF EXIST %DIR%stop-all.sh del %DIR%stop-all.sh
	call :RENAME_DIST
    echo Device Backend profile created successfully in %TEMPDIR%\%DISTRIBUTION%%PROFILE%.
	goto Exit

:Manager
	echo Preparing the Device Manager profile distribution.
	SET PROFILE=_device-manager
	call :COPY_DIST
	set DEFAULT_BUNDLES=%DIR%..\wso2\components\device-manager\configuration\org.eclipse.equinox.simpleconfigurator\bundles.info
    call :Remove_BROKER
    call :Remove_ANALYTICS
    call :Remove_JARS
    IF EXIST %DIR%..\wso2\components\default @RD /S /Q  %DIR%..\wso2\components\default
    IF EXIST %DIR%..\wso2\components\device-key-manager @RD /S /Q %DIR%..\wso2\components\device-key-manager
    IF EXIST %DIR%..\wso2\components\device-backend @RD /S /Q %DIR%..\wso2\components\device-backend
    IF EXIST %DIR%..\samples @RD /S /Q %DIR%..\samples
    IF EXIST %DIR%..\plugins @RD /S /Q %DIR%..\plugins
    for /R %DIR%..\repository\resources\profiles\manager %%f in (*.sh) do copy %%f %DIR%..\bin\
    for /R %DIR%..\repository\resources\profiles\manager %%f in (*.bat) do copy %%f %DIR%..\bin\
	copy /y %DIR%..\repository\resources\profiles\manager\carbon.xml %DIR%..\conf\
	copy /y %DIR%..\repository\resources\profiles\manager\axis2\axis2.xml %DIR%..\conf\axis2\
    IF EXIST %DIR%..\repository\deployment\server\synapse-configs @RD /S /Q %DIR%..\repository\deployment\server\synapse-configs
    IF EXIST %DIR%..\conf\nhttp.properties del %DIR%..\conf\nhttp.properties
    IF EXIST %DIR%..\conf\synapse-handlers.xml del %DIR%..\conf\synapse-handlers.xml
    IF EXIST %DIR%..\conf\synapse.properties del %DIR%..\conf\synapse.properties
    IF EXIST %DIR%..\conf\passthru-http.properties del %DIR%..\conf\passthru-http.properties
    IF EXIST %DIR%..\conf\datasources\android-datasources.xml del %DIR%..\conf\datasources\android-datasources.xml
    IF EXIST %DIR%..\conf\datasources\windows-datasources.xml del %DIR%..\conf\datasources\windows-datasources.xml
    IF EXIST %DIR%..\conf\datasources\cdm-datasources.xml del %DIR%..\conf\datasources\cdm-datasources.xml
    mkdir %DIR%..\repository\deployment\server\tempwebapp
    copy /y %DIR%..\repository\deployment\server\webapps\api#am#publisher#v0.11.war %DIR%..\repository\deployment\server\tempwebapp\
    copy /y %DIR%..\repository\deployment\server\webapps\api#am#store#v0.11.war %DIR%..\repository\deployment\server\tempwebapp\
    copy /y %DIR%..\repository\deployment\server\webapps\api#appm#oauth#v1.0.war %DIR%..\repository\deployment\server\tempwebapp\
    copy /y %DIR%..\repository\deployment\server\webapps\api#appm#publisher#v1.1.war %DIR%..\repository\deployment\server\tempwebapp\
    copy /y %DIR%..\repository\deployment\server\webapps\api#appm#store#v1.1.war %DIR%..\repository\deployment\server\tempwebapp\
    copy /y %DIR%..\repository\deployment\server\webapps\shindig.war %DIR%..\repository\deployment\server\tempwebapp\
	IF EXIST %DIR%..\repository\deployment\server\webapps @RD /S /Q %DIR%..\repository\deployment\server\webapps
    IF EXIST %DIR%..\repository\deployment\server\axis2services @RD /S /Q %DIR%..\repository\deployment\server\axis2services
	mkdir %DIR%..\repository\deployment\server\axis2services
	mkdir %DIR%..\repository\deployment\server\webapps
	for /R %DIR%..\repository\deployment\server\tempwebapp %%f in (*.war) do copy %%f %DIR%..\repository\deployment\server\webapps\
    IF EXIST %DIR%..\conf\identity\sso-idp-config.xml del %DIR%..\conf\identity\sso-idp-config.xml
	IF EXIST %DIR%..\repository\deployment\server\tempwebapp @RD /S /Q %DIR%..\repository\deployment\server\tempwebapp
	IF EXIST %DIR%start-all.bat del %DIR%start-all.bat
	IF EXIST %DIR%start-all.sh del %DIR%start-all.sh
	IF EXIST %DIR%stop-all.bat del %DIR%stop-all.bat
	IF EXIST %DIR%stop-all.sh del %DIR%stop-all.sh
	call :RENAME_DIST
    echo Device Manager profile created successfully in %TEMPDIR%\%DISTRIBUTION%%PROFILE%.
	goto Exit


:Analytics
    echo Preparing the Analytics profile.
	SET PROFILE=_analytics
	call :COPY_DIST
    set DEFAULT_BUNDLES=%DIR%..\wso2\components\analytics-default\configuration\org.eclipse.equinox.simpleconfigurator\bundles.info
    call :Remove_IoT
    call :Remove_BROKER
    call :Remove_JARS
	call :RENAME_DIST
    echo Analytics profile created successfully in %TEMPDIR%\%DISTRIBUTION%%PROFILE%.
    goto Exit

:Broker
    echo Preparing the Broker profile.
	SET PROFILE=_broker
	call :COPY_DIST
    set DEFAULT_BUNDLES=%DIR%..\wso2\components\broker-default\configuration\org.eclipse.equinox.simpleconfigurator\bundles.info
    call :Remove_IOT
    call :Remove_ANALYTICS
    call :Remove_JARS
	call :RENAME_DIST
    echo Broker profile created successfully in %TEMPDIR%\%DISTRIBUTION%%PROFILE%.
    goto Exit

:All
	echo Creating all profiles in IoT distribution
	call :Gateway
	call :KeyManager
	call :Backend
	call :Manager
	call :Analytics
	call :Broker
	echo All profiles are created successfully in %TEMPDIR%.
	goto :eof

:Remove_BROKER
    echo Removing Broker profile
    IF EXIST %DIR%..\wso2\broker @RD /S /Q %DIR%..\wso2\broker
    IF EXIST %DIR%..\wso2\components\broker-default @RD /S /Q %DIR%..\wso2\components\broker-default
    IF EXIST %DIR%broker.bat del %DIR%broker.bat
    IF EXIST %DIR%broker.sh del %DIR%broker.sh
    goto :eof

:Remove_ANALYTICS
    echo Removing Analytics profile
    IF EXIST %DIR%..\wso2\analytics @RD /S /Q %DIR%..\wso2\analytics
    IF EXIST %DIR%..\wso2\components\analytics-default @RD /S /Q %DIR%..\wso2\components\analytics-default
    IF EXIST %DIR%..\wso2\components\analytics-worker @RD /S /Q %DIR%..\wso2\components\analytics-worker
    IF EXIST %DIR%analytics.bat del %DIR%analytics.bat
    IF EXIST %DIR%analytics.sh del %DIR%analytics.sh
    goto :eof

:Remove_IoT
    echo Removing IoT profile
    IF EXIST %DIR%..\conf @RD /S /Q %DIR%..\conf
    IF EXIST %DIR%..\wso2\components\default @RD /S /Q %DIR%..\wso2\components\default
    IF EXIST %DIR%..\wso2\components\device-manager @RD /S /Q %DIR%..\wso2\components\device-manager
    IF EXIST %DIR%..\wso2\components\device-key-manager @RD /S /Q %DIR%..\wso2\components\device-key-manager
    IF EXIST %DIR%..\wso2\components\device-backend @RD /S /Q %DIR%..\wso2\components\device-backend
    IF EXIST %DIR%..\samples @RD /S /Q %DIR%..\samples
    IF EXIST %DIR%..\modules @RD /S /Q %DIR%..\modules
    IF EXIST %DIR%..\dbscripts @RD /S /Q %DIR%..\dbscripts
    IF EXIST %DIR%..\plugins @RD /S /Q %DIR%..\plugins
    IF EXIST %DIR%..\repository @RD /S /Q %DIR%..\repository
    IF EXIST %DIR%..\tmp @RD /S /Q %DIR%..\tmp
    IF EXIST %DIR%..\resources @RD /S /Q %DIR%..\resources
    IF EXIST %DIR%iot-server.bat del %DIR%iot-server.bat
    IF EXIST %DIR%iot-server.sh del %DIR%iot-server.sh
	IF EXIST %DIR%carbondump.bat del %DIR%carbondump.bat
	IF EXIST %DIR%carbondump.sh del %DIR%carbondump.sh
	IF EXIST %DIR%chpasswd.bat del %DIR%chpasswd.bat
	IF EXIST %DIR%chpasswd.sh del %DIR%chpasswd.sh
	IF EXIST %DIR%ciphertool.bat del %DIR%ciphertool.bat
	IF EXIST %DIR%ciphertool.sh del %DIR%ciphertool.sh
	IF EXIST %DIR%start-all.bat del %DIR%start-all.bat
	IF EXIST %DIR%start-all.sh del %DIR%start-all.sh
	IF EXIST %DIR%stop-all.bat del %DIR%stop-all.bat
	IF EXIST %DIR%stop-all.sh del %DIR%stop-all.sh
    goto :eof

:Remove_JARS
    echo Removing unnecessary jars
    mkdir %DIR%..\wso2\components\tmp_plugins

    FOR /F "tokens=1,2* delims=, " %%i in (%DEFAULT_BUNDLES%) do copy %DIR%..\wso2\components\plugins\%%i_%%j.jar %DIR%..\wso2\components\tmp_plugins

    @RD /S /Q %DIR%..\wso2\components\plugins
    rename %DIR%..\wso2\components\tmp_plugins plugins
    goto :eof

:RENAME_DIST
	rename %TEMPDIR%\%DISTRIBUTION% %DISTRIBUTION%%PROFILE%
	SET DIR=%TEMPDIR%\%DISTRIBUTION%%PROFILE%\bin\
	del /f %DIR%profile-creator.sh
	del /f %DIR%profile-creator.bat
	goto :eof

:Exit
	set DIR=%~dp0
    pause
