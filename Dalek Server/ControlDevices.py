#TODO:
    #Code I2C communication
    #Code Stepper/Servo Translation
    #Code Audio Device
    #Code RGB light PWM Class
    #code: locks, threads

from DalekUtilities import *
from functools import partial
from AccelStepper import AccelStepper
import time
import socket
import sys
import threading
import struct

SPEED_DELAY = .01
MAX_SPEED = 100
SPEED_INCREMENT = 5

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
    #i2c =  Adafruit_I2C(0x70, 1 , False)
    #i2c.write8(PCA9685_MODE1, 0x01)  
    #time.sleep(0.05)
    
    
def destroy_devices():
    PWM.cleanup()
    setI2CFrequency(100)


class Motor:
    def getType():
        return "Motor"
    def command(self, sock):
        data = sock.recv(1)
        command1 = int(ord(data[0]))
 
        if(command1 != SETENABLE):
            data = sock.recv(4)
            (command2,) = struct.unpack( "!f", data[0:4] )
            #Send New Task
               
            self.threadStop = True
               
            while (self.thread and self.thread.isAlive() ):
                pass
             
            self.threadStop = False
            self.thread = threading.Thread(target=self.updateSpeed, args=(command2))
            
            self.thread.start()
        else:
            data = sock.recv(1)
            command2 = int(ord(data[0]))
            self.setEnable(command2)
        
        return
        
    def __init__(self, deviceId, name, pinA, pinB, enable, currentSenseA):
        self.deviceId = deviceId
        self.name = name
        #pins
        self.pinA = pinA
        self.currentSenseA = currentSenseA
        self.pinB = pinB
        self.enable = enable
        
        
        self.enabled = DISABLE
        self.currentSpeed = 0
        self.targetSpeed = 0
        self.direction = FORWARD
        
        self.threadStop = False
        self.thread = threading.Thread()
        
        
        #PWM.start(channel, duty, freq=2000)
        #duty values are valid 0-100
       
        self.enableFun=getGPIOWriteFun(self.enable)
        self.enableFun(GPIO.LOW)
        
        self.pwmfunA=getPWMWriteFun(self.pinA)
        self.pwmfunB=getPWMWriteFun(self.pinB)

         
        self.pwmfunA(0)
        self.pwmfunB(0)
        
        
    def __del__(self):
        print 'Stoping Motor ', self.deviceId, ' pA', self.pinA, ' pB', self.pinB
        self.reset()
        
        
    def toString(self):
        return 'Motor: Id: [%(self.deviceId)s] pinA: %(self.pinA)s pinB: %(self.pinB)s currentSenseA: %(self.currentSenseA)s enabled: %(self.enabled)s Current Direction: %(self.direction)s currentSpeed: %(self.currentSpeed)s targetSpeed: %(self.targetSpeed)s' %{'self.deviceId': self.deviceId,'self.pinA':self.pinA,'self.pinB':self.pinB,'self.currentSenseA':self.currentSenseA, 'self.enabled':self.enabled, 'self.direction':self.direction, 'self.currentSpeed':self.currentSpeed, 'self.targetSpeed':self.targetSpeed }
    
    def reset(self):
        self.threadStop = 'true'
           
        while (self.thread.isAlive() ):
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
                print 'PWN:PA',self.currentSpeed
               # PWM.set_duty_cycle(self.pinA,  self.currentSpeed)
                self.pwmfunA(self.currentSpeed)
                if(self.pwmfunB):
                    #PWM.set_duty_cycle(self.pinB, 0)
                    self.pwmfunB(0)
                    
                
            else:
                if(self.pinB):
                    print 'PWN:PB',-1*self.currentSpeed
                    #PWM.set_duty_cycle(self.pinB, (-1*self.currentSpeed))
                    #PWM.set_duty_cycle(self.pinA, 0)
                    self.pwmfunA(0)
                    self.pwmfunB(-1*self.currentSpeed)
            
            if(self.threadStop == 'true'):
                return
                
            time.sleep(SPEED_DELAY)
        
    def setEnable(self, enable):
        self.targetSpeed = 0
        self.currentSpeed = 0
        
        #PWM.set_duty_cycle(self.pinB, 0)
        #PWM.set_duty_cycle(self.pinA, 0)
        self.pwmfunA(0)
        self.pwmfunB(0)
        if(enable != self.enabled):
            self.enabled = enable
            if(enable == ENABLE):
                self.enabledFun(GPIO.HIGH)
            else:
                self.enabledFun(GPIO.LOW)
        
    def setEnableNoStop(self, enable): 
        if(enable != self.enabled):
            self.enabled = enable
            if(enable == ENABLE):
                self.enabledFun(GPIO.HIGH)
            else:
                self.enabledFun(GPIO.LOW)


class MotorPair:
    def getType():
        return "MotorPair"
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
            
        if(self.motor1.targetSpeed < -1*MAX_SPEED):
            self.motor1.targetSpeed = -1*MAX_SPEED
        if(self.motor2.targetSpeed < -1*MAX_SPEED):
            self.motor2.targetSpeed = -1*MAX_SPEED
        
        # print 'Set Speed: ', speed1, ' and ', speed2
   #      print 'TS: ', self.motor1.targetSpeed, ' and ',self.motor2.targetSpeed
   #      print 'C: ', self.motor1.currentSpeed, ' and ',self.motor2.currentSpeed
        
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
                print 'PWN:M1A',self.motor1.currentSpeed
                #PWM.set_duty_cycle(self.motor1.pinA, (self.motor1.currentSpeed) )
                #PWM.set_duty_cycle(self.motor1.pinB, 0 )
                self.motor1.pwmfunA(self.motor1.currentSpeed)
                self.motor1.pwmfunB(0)
                
            else:
                print 'PWN:M1B',-1*self.motor1.currentSpeed
                #PWM.set_duty_cycle(self.motor1.pinB, (-1*self.motor1.currentSpeed))
                #PWM.set_duty_cycle(self.motor1.pinA, 0 )
                self.motor1.pwmfunB(-1*self.motor1.currentSpeed)
                self.motor1.pwmfunA(0)
                
            if(self.motor2.currentSpeed >0):
                print 'PWN:M2A',self.motor2.currentSpeed
               # PWM.set_duty_cycle(self.motor2.pinA, (self.motor2.currentSpeed))
               # PWM.set_duty_cycle(self.motor2.pinB, 0)
                self.motor2.pwmfunA(self.motor2.currentSpeed)
                self.motor2.pwmfunB(0)
            else:
                print 'PWN:M2B',-1*self.motor2.currentSpeed
                #PWM.set_duty_cycle(self.motor2.pinB, (-1*self.motor2.currentSpeed) )
                #PWM.set_duty_cycle(self.motor2.pinA, 0 )
                self.motor2.pwmfunB(-1*self.motor2.currentSpeed)
                self.motor2.pwmfunA(0)
                
            
            if(self.threadStop == 'true'):
              #  print "I yeiled"
                break
            time.sleep(SPEED_DELAY)
        
    def setEnable(self, enable):
        self.motor1.targetSpeed = 0
        self.motor2.targetSpeed = 0
        self.motor1.currentSpeed = 0
        self.motor2.currentSpeed = 0
        #PWM.set_duty_cycle(self.motor1.pinB, 0)
        #PWM.set_duty_cycle(self.motor1.pinA, 0)
        #PWM.set_duty_cycle(self.motor2.pinA, 0)
        #PWM.set_duty_cycle(self.motor2.pinB, 0)
        
        self.motor1.pwmfunA(0)
        self.motor1.pwmfunB(0)
        self.motor2.pwmfunA(0)
        self.motor2.pwmfunB(0)
        
        if(self.motor1.enabled != enable or self.motor2.enabled != enable):       
            self.motor1.setEnableNoStop(enable)
            self.motor2.setEnableNoStop(enable)

            
class Stepper:
    def getType():
        return "Stepper"
        
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


    
class Servo:
    def getType():
        return "Servo"
        
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
        self.thread = threading.Thread(target=self.threadStart, args=(command1))
       
    def threadStart(self, command1):
        #print self.toString()

        self.setAngle(command1)

        #print self.toString()
        self.cv.release()

         
        
    def __init__(self, deviceId, name, pwm, angleMin, angleMax, angle, pwmMin, pwmMax):
        self.name = name
        self.deviceId = deviceId
        self.pwm = pwm
        self.angle = angle
        self.dangle = angle
        self.angleMin = angleMin
        self.angleMax = angleMax 
        self.pwmMin = pwmMin
        self.pwmMax = pwmMax
        
        self.pwmfun=getPWMWriteFun(self.pwm)
        self.setAngle(self.angle)
        self.cv = threading.Lock()
        
        
        
    def setAngle(self, angle):
        #Source http://forums.adafruit.com/viewtopic.php?f=8&t=41040
        #https://mail.python.org/pipermail/tutor/2013-August/097291.html
        def arduino_map(x, in_min, in_max, out_min, out_max):
            return (x - in_min) * (out_max - out_min) // (in_max - in_min) + out_min
            
        servopos = arduino_map(int(angle), int(self.angleMin), int(self.angleMax), int(self.pwmMin), int(self.pwmMax))
        #PWM.set_duty_cycle(self.pwm, servopos)
        self.pwmfun(servopos)
        self.angle = angle
        
    def reset(self):
        self.setAngle(self.dangle)
        
    def toString(self):
        return 'Servo: Id[%(id)s] Name: %(name)s Pwm: %(pwm)s Angle: %(angle)s Minpwm: %(mpwm)s Maxpwm: %(mxpwm)s' %{'id':self.deviceId,'name':self.name,'pwm':self.pwm, 'angle':self.angle, 'mpwm':self.pwmMin, 'mxpwm':self.pwmMax }


    
class ServoPair:
    def getType():
        return "ServoPair"
        
    def command(self, sock):
        data = sock.recv(2)
        command1 = int(ord(data[0]))
        command2 = int(ord(data[1]))
      
        print 'ServoPair: Set Angle to',command1,' and ',command2,'.'
        
        #First Stop any thread not disabling the motor
        self.threadStop = True
        #Wait for it to finish
        self.cv.acquire()
        self.threadStop = False
       
        #Send New Task
        self.thread = threading.Thread(target=self.threadStart, args=(command1, command2))
       
    def threadStart(self, command1, command2):
        #print self.toString()

        self.servo1.setAngle(command1)
        self.servo2.setAngle(command1)
        #print self.toString()
        self.cv.release()

         
        
    def __init__(self, deviceId, name,s1, s2):
        self.name = name
        self.deviceId = deviceId
        self.servo1 = s1
        self.servo2 = s2
        
        self.cv = threading.Lock()
        
    def reset(self):
        self.servo1.setAngle(self.dangle)
        self.servo2.setAngle(self.dangle)
        
    def toString(self):
        return 'Servo: Id[%s] Name: %s\n\tServo1: %s\n\tServo2: %s' %(self.deviceId, self.name,self.servo1.toString(), self.servo2.toString())

class Stepper:
    def getType():
        return "Stepper"
    
    def command(self, sock):
        data = sock.recv(1)
        command1 = int(ord(data[0]))
        
        print 'Stepper: Take ',command1,' Steps.'
        
        #First Stop any thread not disabling the motor
        self.threadStop = True
        #Wait for it to finish
        self.cv.acquire()
        self.threadStop = False
       
        #Send New Task
        self.thread = threading.Thread(target=self.threadStart, args=(command1))

    def threadStart(self, command1):
        #print self.toString()
        if(command1 >= self.maxPos):
            command1=self.maxPos
        elif(command1 <= self.minPos):
            command1=self.minPos

        self.stepper.moveTo(command1)
        self.stepper.run()
        #print self.toString()
        self.cv.release()

         
        
    def __init__(self, deviceId, name,maxSpeed, initPos,maxPos, minPos, acceleration,enablePin,minWidth, pins, pin1, pin2, pin3, pin4):
        self.name = name
        self.deviceId=deviceId
        self.maxPos=maxPos
        self.minPos=minPos
        self.initPos=initPos
        
        self.acceleration=acceleration
        self.enablePin=enablePin
        self.minWidth=minWidth
        
        self.deviceId = deviceId
        self.stepper= AccelStepper(pins,pin1,pin2, pin3, pin4)
        self.stepper.setMaxSpeed(maxSpeed)
        self.stepper.setAcceleeration(acceleration)
#        self.stepper.setEnablePin(enablePin)
        self.stepper.setMinPulseWidth(minWidth)
        
        self.cv = threading.Lock()
        
        self.stepper.moveTo(self.initPos)
        
    def reset(self):
        self.stepper.moveTo(self.initPos)
        
    def toString(self):
        return 'Stepper: Id[%s] Name: %s\n' %(self.deviceId, self.name)

import pygame
from os import listdir
from os.path import isfile, join

class Audio:
    def getType():
        return "Audio"
    def __init__(self, deviceId, name, folderName):
        self.deviceId = deviceId
        self.name = name
        self.folderName = folderName
        self.audioFiles = [ f for f in listdir(self.folderName) if isfile(join(self.folderName,f)) ]
        pygame.mixer.init()
        
        self.thread = threading.Thread()
        self.cv = threading.Lock()
        
    def __del__(self):
        pass
        
    def command(self, sock):
        data = sock.recv(1)
        command1 = int(ord(data[0]))
        
        print 'Audio: Does ',command1
        
        #Wait for it to finish
        self.cv.acquire()
       
        #Send New Task
        self.thread = threading.Thread(target=self.threadStart, args=(command1))

    def threadStart(self, command1):
        #print self.toString()
        if(command1  == -1):
           pygame.mixer.stop() 
        else:
            pygame.mixer.music.load(self.audioFiles[command1])
            pygame.mixer.music.play()
            
            
        self.cv.release()
    
    def toString(self):
        return 'Audio[%d][%s]: Files Loaded from %s\n' %(self.deviceId, self.name, self.folderName)
    