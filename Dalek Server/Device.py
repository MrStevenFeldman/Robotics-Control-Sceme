#TODO:
    #Code I2C communication
    #Code Stepper/Servo Translation
    #Code Audio Device
    #Code RGB light PWM Class
    #code: locks, threads
    
import time
import socket
import sys
import threading
import struct

import Adafruit_BBIO.GPIO as GPIO
import Adafruit_BBIO.ADC as ADC
import Adafruit_BBIO.PWM as PWM

#I2C
import Adafruit_I2C  as I2C

PCA9685_MODE1 = 0x0
PCA9685_PRESCALE = 0xFE

#end I2C

pwm_freq = 1000
SPEED_DELAY = .1
MAX_SPEED = 100
SPEED_INCREMENT = 1

SETENABLE = 0
SETSPEED = 1
SETDIRECTION = 2
FORWARD = 0
REVERSE = 1
ENABLE = 1
DISABLE = 0


def initilize_device():
    ADC.setup()
    #Adafruit_I2C(module address, i2c busnum, debug=[true |false]}
    i2c =  Adafruit_I2C(0x70, 1 , False)
    i2c.write8(PCA9685_MODE1, 0x01)  
    time.sleep(0.05)
    
    
def destroy_devices():
    PWM.cleanup()
    setI2CFrequency(100)
    
def setI2CFrequency(freq):
		prescaleval = 25000000
		prescaleval /= 4096
		prescaleval /= float(freq)
		prescaleval -= 1
		prescale = int(prescaleval + 0.5)

		oldmode = i2c.readU8(PCA9685_MODE1)
		newmode = (oldmode&0x7F) | 0x10
		i2c.write8(PCA9685_MODE1, newmode)  
		i2c.write8(PCA9685_PRESCALE, prescale)
		i2c.write8(PCA9685_MODE1, oldmode)
		time.sleep(0.05)
		i2c.write8(PCA9685_MODE1, oldmode | 0xA1)


def setI2CDutyCycle(channel, cycle):
	off = min(1, cycle)
	off = int(cycle*4095)
	bytes = [0x00, 0x00, off & 0xFF, off >> 8]
	i2c.writeList(0x06+(4*channel), bytes)
    

class BiMotor:
    def command(self, sock):
        data = sock.recv(1)
        command1 = int(ord(data[0]))
 
        if(command1 != SETENABLE):
            data = sock.recv(4)
            (command2,) = struct.unpack( "!f", data[0:4] )
            #Send New Task
               
            self.threadStop = 'true'
               
            while (self.thread and self.thread.isAlive() ):
                pass
             
            self.threadStop = 'false'
            self.thread = threading.Thread(target=self.updateSpeed, args=(command2))
            
            self.thread.start()
        else:
            data = sock.recv(1)
            command2 = int(ord(data[0]))
            self.setEnable(command2)
        
        return
        
    def __init__(self, deviceId, name, pinA, pinB, enableA, currentSenseA):
        self.deviceId = deviceId
        self.name = name
         #pins
        self.pinA = pinA
        self.currentSenseA = currentSenseA
        self.pinB = pinB
        self.enableA = enableA
        
        
        self.enabled = DISABLE
        self.currentSpeed = 0
        self.targetSpeed = 0
        self.direction = FORWARD
        
        GPIO.setup(self.enableA, GPIO.OUT)
        GPIO.output(self.enableA, GPIO.LOW)
        
        #PWM.start(channel, duty, freq=2000)
        #duty values are valid 0-100
        PWM.start(self.pinA, 0)
        PWM.start(self.pinB, 0)
        
        self.threadStop = 'false'
        self.thread = threading.Thread()
        
    def __del__(self):
        self.reset()
        PWM.stop(self.pinA)
        if(self.pinB):
            PWM.stop(self.pinB)
        PWM.cleanup()
        
        
    def toString(self):
        return 'BiMotor: Id: [%(self.deviceId)s] pinA: %(self.pinA)s pinB: %(self.pinB)s currentSenseA: %(self.currentSenseA)s enabled: %(self.enabled)s Current Direction: %(self.direction)s currentSpeed: %(self.currentSpeed)s targetSpeed: %(self.targetSpeed)s' %{'self.deviceId': self.deviceId,'self.pinA':self.pinA,'self.pinB':self.pinB,'self.currentSenseA':self.currentSenseA, 'self.enabled':self.enabled, 'self.direction':self.direction, 'self.currentSpeed':self.currentSpeed, 'self.targetSpeed':self.targetSpeed }
    
    def reset(self):
        self.threadStop = 'true'
           
        while (self.thread and self.thread.isAlive() ):
            pass
        self.setEnable(DISABLE)
         
    def updateSpeed(self, speed):
        
        self.targetSpeed = speed
        if(self.targetSpeed > MAX_SPEED):
            self.targetSpeed = MAX_SPEED
            
        while self.targetSpeed != self.currentSpeed:
            if(self.currentSpeed < self.targetSpeed):
                self.currentSpeed = self.currentSpeed + SPEED_INCREMENT
                if(self.currentSpeed > self.targetSpeed):
                    self.currentSpeed = self.targetSpeed
            if(self.currentSpeed > self.targetSpeed):
                self.currentSpeed = self.currentSpeed - SPEED_INCREMENT
                if(self.currentSpeed < self.targetSpeed):
                    self.currentSpeed = self.targetSpeed
                
            if(self.motor1.currentSpeed >0):
                PWM.set_duty_cycle(self.pinA, self.currentSpeed)
                PWM.set_duty_cycle(self.motor1.pinB, 0)
                
            else:
                if(self.pinB):
                    PWM.set_duty_cycle(self.pinB, -1*self.currentSpeed)
                    PWM.set_duty_cycle(self.pinA, 0)
            
            if(self.threadStop == 'true'):
                return
                
            time.sleep(SPEED_DELAY)
        
    def setEnable(self, enable):
        self.targetSpeed = 0
        self.currentSpeed = 0
        
        PWM.set_duty_cycle(self.pinB, 0)
        PWM.set_duty_cycle(self.pinA, 0)
        if(enable != self.enabled):
            self.enabled = enable
            if(enable == ENABLE):
                GPIO.output(self.enableA, GPIO.HIGH)
            else:
                GPIO.output(self.enableA, GPIO.LOW)
        
    def setEnableNoStop(self, enable): 
        if(enable != self.enabled):
            self.enabled = enable
            if(enable == ENABLE):
                GPIO.output(self.enableA, GPIO.HIGH)
            else:
                GPIO.output(self.enableA, GPIO.LOW)


class MotorPair:
    
    def command(self, sock):
        data = sock.recv(1)
        command1 = int(ord(data[0]))
 
        if(command1 != SETENABLE):
            data = sock.recv(8)
            (command2,) = struct.unpack( "!f", data[0:4] )
            (command3,) = struct.unpack( "!f", data[4:8] )
            #Send New Task
            self.threadStop = 'true'
               
            while (self.thread and self.thread.isAlive() ):
                pass
             
            self.threadStop = 'false'
            self.thread = threading.Thread(target=self.updateSpeed2, args=(command2, command3))
            self.thread.start()
            
        else:
            data = sock.recv(1)
            command2 = int(ord(data[0]))
            self.setEnable(command2)
        
        return
      #  print 'MotorPair: OpCode', command1, ' Operand 1: ', command2,' Operand 2: ', command3, '.'
    
            
    def __init__(self, deviceId, name,  m1, m2):
        self.deviceId = deviceId
        self.name = name
        
        self.motor1 = m1
        self.motor2 = m2
        
        self.threadStop = 'false'
        self.thread = threading.Thread()
        
    def reset(self):
        self.threadStop = 'true'
           
        while (self.thread and self.thread.isAlive() ):
            pass
            
        self.setEnable(DISABLE)

    def toString(self):
        return 'MotorPair: ID[%(did)s]\n\t[%(m1)s]\n\t[%(m2)s]' %{'did':self.deviceId,'m1':self.motor1.toString(), 'm2':self.motor2.toString()}
    
    def updateSpeed2(self, speed1, speed2 ):
        #todo slow write speed, 
        self.motor1.targetSpeed = speed1
        self.motor2.targetSpeed = speed2
        
        if(self.motor1.targetSpeed > MAX_SPEED):
            self.motor1.targetSpeed = MAX_SPEED
        if(self.motor2.targetSpeed > MAX_SPEED):
            self.motor2.targetSpeed = MAX_SPEED
        
        while (self.motor1.targetSpeed != self.motor1.currentSpeed or 
            self.motor2.targetSpeed != self.motor2.currentSpeed) :
        
            if(self.motor1.currentSpeed < self.motor1.targetSpeed):
                self.motor1.currentSpeed = self.motor1.currentSpeed + SPEED_INCREMENT
                if(self.motor1.currentSpeed > self.motor1.targetSpeed):
                    self.motor1.currentSpeed = self.motor1.targetSpeed
            if(self.motor1.currentSpeed > self.motor1.targetSpeed):
                self.motor1.currentSpeed = self.motor1.currentSpeed - SPEED_INCREMENT
                if(self.motor1.currentSpeed < self.motor1.targetSpeed):
                    self.motor1.currentSpeed = self.motor1.targetSpeed
        
            if(self.motor2.currentSpeed < self.motor2.targetSpeed):
                self.motor2.currentSpeed = self.motor2.currentSpeed + SPEED_INCREMENT
                if(self.motor2.currentSpeed > self.motor2.targetSpeed):
                    self.motor2.currentSpeed = self.motor2.targetSpeed
            if(self.motor2.currentSpeed > self.motor2.targetSpeed):
                self.motor2.currentSpeed = self.motor2.currentSpeed - SPEED_INCREMENT
                if(self.motor2.currentSpeed < self.motor2.targetSpeed):
                    self.motor2.currentSpeed = self.motor2.targetSpeed
                
            
            if(self.motor1.currentSpeed >0):
                PWM.set_duty_cycle(self.motor1.pinA, self.motor1.currentSpeed)
                PWM.set_duty_cycle(self.motor1.pinB, 0)
                
            else:
                PWM.set_duty_cycle(self.motor1.pinB, -1*self.motor1.currentSpeed)
                PWM.set_duty_cycle(self.motor1.pinA, 0)
                
            if(self.motor2.currentSpeed >0):
                PWM.set_duty_cycle(self.motor2.pinA, self.motor2.currentSpeed)
                PWM.set_duty_cycle(self.motor2.pinB, 0)
            else:
                PWM.set_duty_cycle(self.motor2.pinB, -1*self.motor2.currentSpeed)
                PWM.set_duty_cycle(self.motor2.pinA, 0)
                
            
            if(self.threadStop):
              #  print "I yeiled"
                break
            time.sleep(SPEED_DELAY)
            
      #  print 'targetSpeed', self.motor1.targetSpeed, ' and ', self.motor2.targetSpeed
      #  print 'targetSpeed', self.motor1.currentSpeed, ' and ', self.motor2.currentSpeed
            
    def setEnable(self, enable):
        self.motor1.targetSpeed = 0
        self.motor2.targetSpeed = 0
        self.motor1.currentSpeed = 0
        self.motor2.currentSpeed = 0
        PWM.set_duty_cycle(self.motor1.pinB, 0)
        PWM.set_duty_cycle(self.motor1.pinA, 0)
        PWM.set_duty_cycle(self.motor2.pinA, 0)
        PWM.set_duty_cycle(self.motor2.pinB, 0)
        if(self.motor1.enabled != enable or self.motor2.enabled != enable):       
            self.motor1.setEnableNoStop(enable)
            self.motor2.setEnableNoStop(enable)

            
class Stepper:
    def command(self, sock):
        data = sock.recv(2)
        command1 = int(ord(data[0]))
        command2 = int(ord(data[0]))
        #TODO, Take X Steps or Rotate X RPMS
        #TODO make threaded
        print 'Stepper: Set Angle to',command1,'.'
        
        
    def __init__(self, deviceId, name, pwm, nSteps, dirPin):
        self.name = name
        self.deviceId = deviceId
        self.pwm = pwm
        self.dirPin = dirPin
        self.nSteps = nSteps
        
        self.cv = threading.Lock()
        
        
    def setSpeed(self, rpm):
        print 'Set Speed NOT IMPLEMENTED'
        
    def takeSteps(self, steps):
        self.setSpeed(0)
        #TODO
    def reset(self):
        self.setSpeed(0)
        
    def toString(self):
        return 'Stepper: Id[%(id)s] Name: %(name)s Pwm: %(pwm)s dirPin: %(dirPin)s ' %{'id':self.deviceId,'name':self.name,'pwm':self.pwm, 'dirPin':self.dirPin}


class StepperI2C:

    def __init__(self, deviceId, name, i2c, ch, dirPin):
        self.name = name
        self.deviceId = deviceId
        
        self.ch = ch
        self.dirPin = dirPin
        self.i2c = i2c
        
        self.cv = threading.Lock()
        
        
    def setSpeed(self, rpm):
       print "Set speed not implemented"
        
    def takeSteps(self, steps):
        self.setSpeed(0)
        #TODO
    def reset(self):
        self.setSpeed(0)
        
    def toString(self):
        return 'Stepper: Id[%(id)s] Name: %(name)s I2c: %s(i2c)s Ch: %(ch)s dirPin: %(dirPin)s ' %{'id':self.deviceId,'name':self.name,'i2c':self.i2c,'ch':self.ch, 'dirPin':self.dirPin }
    
    
class Servo:
    def command(self, sock):
        data = sock.recv(1)
        command1 = int(ord(data[0]))
      
        print 'Servo: Set Angle to',command1,'.'
        
        #First Stop any thread not disabling the motor
        self.threadStop = True
        #Wait for it to finish
        self.cv.acquire()
        self.threadStop = False
       
        #Send New Task
        self.thread = threading.Thread(target=self.threadStart, args=(command1, command2, command3))
       
    def threadStart(self, command1, command2, command3):
        #print self.toString()

        self.setAngle(command1)

        #print self.toString()
        self.cv.release()

         
        
    def __init__(self, deviceId, name, pwm, pwmMin, pwmMax, angleMin, angleMax, angle):
        self.name = name
        self.deviceId = deviceId
        self.pwm = pwm
        self.angle = angle
        self.dangle = angle
        self.pwmMin = pwmMin
        self.pwmMax = pwmMax
        self.angleMin = angleMin
        self.angleMax = angleMax 
        
        PWM.start(self.pwm, 0)
        self.setAngle(self.angle)
        
        self.cv = threading.Lock()
        
        
    def __del__(self):
        PWM.stop(self.pwm)
        
    def setAngle(self, angle):
        #Source http://forums.adafruit.com/viewtopic.php?f=8&t=41040
        servopos = map(angle, self.angleMin, self.angleMax, self.pwmMin, self.pwmMax)
        PWM.set_duty_cycle(self.pwm, servopos)
        self.angle = angle
        
    def reset(self):
        self.setAngle(self.dangle)
        
    def toString(self):
        return 'Servo: Id[%(id)s] Name: %(name)s Pwm: %(pwm1)s Angle: %(angle)s' %{'id':self.deviceId,'name':self.name,'pwm':self.pwm, 'angle':self.angle }
        
class ServoI2C (Servo): 
    
    def __init__(self, deviceId, name, pwm,pwmMin, pwmMax, angleMin, angleMax,  angle, i2c, channel):
        self.name = name
        self.deviceId = deviceId
        self.pwm = pwm
        self.pwmMin = pwmMin
        self.pwmMax = pwmMax
        self.angleMin = angleMin
        self.angleMax = angleMax
        
        self.angle = angle
        self.dangle = angle
        self.i2c = i2c
        self.channel = channel
        self.setAngle(self.angle)
        
        
        self.cv = threading.Lock()
        
        
    def __del__(self):
        self.reset()
        
    def setAngle(self, angle):
        servopos = map(angle, self.angleMin, self.angleMax, self.pwmMin, self.pwmMax)
        self.i2c.setDutyCycle(self.channel, servopos)
        self.angle = angle
        
        
    def reset(self):
        self.setAngle(self.dangle)
        
    def toString(self):
        return 'Servo: Id[%(id)s] Name: %(name)s I2C: %(i2c)s Channel: %(chan)s Angle: %(angle)s' %{'id':self.deviceId,'name':self.name,'i2c':self.i2c, 'chan':self.channel, 'angle':self.angle }
       
        