Robotics-Control-Sceme
======================

Designed for a Dalek, but code for extendability

How to use:

On the BeagleBoneBlack:
install:

For PWM/GPIO access
adafruit-beaglebone-io-python
	Link: https://github.com/adafruit/adafruit-beaglebone-io-python
	
	Instructions:
		/usr/bin/ntpdate -b -s -u pool.ntp.org
		opkg update && opkg install python-pip python-setuptools
		pip install Adafruit_BBIO
For i2c access:
	i2c-tools
	Instructions: opkg install i2c-tools
	
Dalek Server:
Copy The Dalek Server Folder
Execute: python DalekServer.py


Configuration:
TODO. eh look at the xml and figure it out :) (I will be making a schema shortly)



TODOS:
make any i2c an option for any pam pin
test/implement i2c everywhere
Test Servo code
Implement Stepper code
make schema for devices, audio,videos
make XML for control device, such that when connected the xml sends deviceIDS, types, and device names to the control device. Which generates a list of available UI components.

Future Device Interfaces:
RGB LEDS
Audio Playback



Control Device
	Make a text document describing communication patter
	
	Android Tablet. make a it better :)