
WSO2 IoTs 3.3.0 QSG Setup Guide
---------------------------------

1. Start the WSO2 IoTS server
2. Navigate to <IoTS_HOME>/core/samples/mobile-qsg/ directory using the terminal.
3. Once server is started execute the mobile-qsg.sh script
4. There will be two users getting created once you ran the script with user name 'chris' and 'alex'.
5. Then login to the https://<your-server>:9443/devicemgt/ and use the username/password of chris/alex.
6. User chris will be having admin role, and you can login as chris with below details.
   username: chris
   password: chrisadmin
5. Alex will be restricted user, and he has permission to only enrol a device. You can login as alex with below details.
   username: alex
   password: alexuser

Note:
For this sample we have configured above user from the script. If you want to run this script again you have to login as
admin and remove the user alex, chris and role iotMobileUser from the IoT Server.