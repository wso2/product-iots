#!/bin/bash

echo "----------------------------------------------------------------"
echo "|		WSO2 IOT Sample				"
echo "|		  RaspiAlarm				"
echo "|	       ----------------				"
echo "|    ....initializing startup-script	"
echo "----------------------------------------------------------------"

while true; do
    read -p "Do you wish to run 'apt-get update' and continue? [Yes/No] " yn
    case $yn in
        [Yy]* ) sudo apt-get update;
				break;;
        [Nn]* ) echo "Continuing without apt-get update...";
				break;;
        * ) echo "Please answer yes or no.";
    esac
done

if [ $? -ne 0 ]; then
    echo "apt-get update failed.... Some dependencies may not get installed"
    echo "If an already installed version of the package exists, try running:"
    echo "----------------------------------------------------------------"
    echo "sudo -i"
    echo "cd /var/lib/dpkg/info"
    echo "rm -rf wso2-raspi-alarm*"
    echo "dpkg --remove --force-remove-reinstreq wso2-raspi-alarm"
    echo "exit"
    echo "----------------------------------------------------------------"
    echo "Retry Installation...."
    break;
fi

echo "Installing 'gdebi' package..."
sudo apt-get install gdebi			# installation of gdebi


if [ $? -ne 0 ]; then
	echo "gdebi installation failed.... dependencies will not be installed without gdebi"
	read -p "Do you wish to continue without gdebi? [Yes/No] " yn
    case $yn in
        [Yy]* ) echo "Continueing without gdebi.....";;
        [Nn]* ) echo "Try to resolve errors and re-run the script.";
				exit;;
        * ) exit;;
    esac
fi


for f in ./wso2-raspi-alarm_1.0_armhf.deb; do
    ## Check if the glob gets expanded to existing files.
    ## If not, f here will be exactly the pattern above
    ## and the exists test will evaluate to false.
    # [ -e "$f" ] && echo "'wso2-raspi-alarm_1.0_armhf.deb' file found and installing" || echo "'wso2-raspi-alarm_1.0_armhf.deb' file does not exist in current path"; exit;
    if [ -e "$f" ]; then
    	echo "'wso2-raspi-alarm_1.0_armhf.deb' file found and installing now...."
    else
    	echo "'wso2-raspi-alarm_1.0_armhf.deb' file does not exist in current path. \nExiting installation..."; 
    	exit;
    fi
    ## This is all we needed to know, so we can break after the first iteration
    break
done

echo "Installing the 'wso2-raspi-alarm deb package'"
sudo gdebi wso2-raspi-alarm_1.0_armhf.deb

if [ $? -ne 0 ]; then
	echo "Installation Failed...."
	exit;
fi


for f in ./deviceConfigs.cfg; do
    ## Check if the glob gets expanded to existing files.
    ## If not, f here will be exactly the pattern above
    ## and the exists test will evaluate to false.
    # [ -e "$f" ] && echo "'wso2-raspi-alarm_1.0_armhf.deb' file found and installing" || echo "'wso2-raspi-alarm_1.0_armhf.deb' file does not exist in current path"; exit;
    if [ -e "$f" ]; then
    	echo "Configuration file found......"
    else
    	echo "'deviceConfigs.cfg' file does not exist in current path. \nExiting installation..."; 
    	exit;
    fi
    ## This is all we needed to know, so we can break after the first iteration
    break
done

echo "Copying configurations file to /usr/local/src/RaspberryAgent"
sudo cp ./deviceConfigs.cfg /usr/local/src/RaspberryAgent/

if [ $? -ne 0 ]; then
	echo "Copying configuration file failed...."
	exit;
fi

echo "Running the RaspberryAgent service...."
# sudo service RaspberryService.sh start

while true; do
    read -p "Whats the time-interval (in seconds) between successive Data-Pushes to the WSO2-DC (ex: '60' indicates 1 minute) > " input

    if [ $input -eq $input 2>/dev/null ]
    then
        echo "Setting data-push interval to $input seconds."
        break;
    else
        echo "Input needs to be an integer indicating the number seconds between successive data-pushes."
    fi
done


cd /usr/local/src/RaspberryAgent/
sudo nohup ./RaspberryStats.py -i $input > /dev/null 2>&1 &

if [ $? -ne 0 ]; then
	echo "Could not start the service..."
	exit;
fi


echo "--------------------------------------------------------------------------"
echo "|			Successfully Started		"
echo "|		   --------------------------		"
#echo "|	 run 'sudo service RaspberryService.sh status'	to check status"
#echo "|	 run 'sudo service RaspberryService.sh stop'	to stop service"
#echo "|		   --------------------------		"
echo "|	 Find logs at: /usr/local/src/RaspberryAgent/logs/RaspberryStats.log"
echo "---------------------------------------------------------------------------"
