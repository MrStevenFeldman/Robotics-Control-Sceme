Robotics-Control-Sceme
======================

Designed for a Dalek, but code for extendability


Todos:


Control Unit code
Rename driver files to signify any serial device
Add network driver file (telnet)

Misc:
Finish Todos


To Use:

Edit the two files to suite your needs

The Device List file:
Each Device is an entire line of signle spaced sepeated integers
BidirectionalMotor: DeviceID-ControlUnitID-enablepin-pwmA-pwmB-currentSenseA-currentSenseB
UnidirectionalMotor: DeviceID-controlUnitid-enablepin-pwm-currentsense

