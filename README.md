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
	
Dalek Server Setup:
	Copy The Dalek Server Folder
	Execute: python DalekServer.py


Configuration:
	Two XMLS:
		DalekServer.xml (TODO)
			Contains Server Initlization values
		HardwareInfo.xml
			Containts to List:
				List of Controllable Devices
				List of Sensor Devices
			See Schemea for values.
			Pins are in the Following form:
			I2C: I_(address)_(channel)
			BBB: P(side)_(pinID)

Controlable Devices:
	Bi-Directional Motors, Stepper, Servo, Motor Pair, Servo Pair
	
Future Controlable Devices:
	LED, RGB LEDs, Audio Out, Solenoid, Video Out
	
Sensor Devices:
	None
Future Sesonor Devices:
	Sonar, Motor Current Sense, Speed, Cardinal Direction, Battery Level, Battery Current, WIFI signal, internal temperature

TODOS:
Extensive Testing
Add Default/Required attributes to XML scheme
Add more robust connection detection


Communication Pattern:
	Command: [device ID] + [op code] + [operand]
