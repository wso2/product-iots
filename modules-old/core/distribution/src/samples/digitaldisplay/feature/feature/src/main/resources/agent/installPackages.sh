#!/bin/bash
# installing dependencies
echo ===Installing Dependencies
sudo apt-get install python-pip
sudo pip install paho-mqtt
sudo apt-get install python-lxml
sudo apt-get install gtk2
sudo apt-get install xdotool

# unzipping the archive
echo ===Unzipping DigitalDisplay.zip
unzip DigitalDisplay

# copying the property file
echo ===Moving deviceConfig.properties
cp ./deviceConfig.properties ./DigitalDisplay