Robotics-Control-Sceme
======================

Designed for a Dalek, but code for extendability


Todos:
Code a network driver class that functions in the same manner as the serial driver class
Check Code TOODs

Generate a setup guide and insturctions


Generate XML schemea

Add Stepper Devices Class
Add Servo Device Class
Add IC2 interface
Use Case:  Plan to add an adafruit 16 PWM Expansion on it to control motors.
    So it should be classified as a ControlUnit and each other control unit should have an IC2 function.
      

Known Issues:
Communication with arduino can be come deadlocked, need to ideentify what causes it.
  Seems the Server is waiting for a message from arduino, but arduino is also waiting...
  

Good Pinout Pic:
http://goo.gl/U513Z
