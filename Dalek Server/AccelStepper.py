from DalekUtilities import *

class AccelStepper:
    
    def __init__(self, pins, pin1, pin2, pin3, pin4):
        self.pins = pins
        self.currentPos = 0
        self.targetPos = 0
        self.speed = 0.0
        self.maxSpeed = 1.0
        self.acceleration = 1.0
        self.stepInterval = 0
        self.minPulseWidth = 1
        self.dirInverted = False
        self.stepInverted = False
        self.enablePin = 0xff
        self.lastStepTime = 0
        self.pin1 = pin1
        self.pin2 = pin2
        self.pin3 = pin3
        self.pin4 = pin4
        #_stepInterval = 20000
        #_speed = 50.0
        #_lastRunTime = 0xffffffff - 20000
        #_lastStepTime = 0xffffffff - 20000 - 10000
        enableOutputs()
    
    def __init(self, forward, backward):
        self.pins = 0
        self.currentPos = 0
        self.targetPos = 0
        self.speed = 0.0
        self.maxSpeed = 1.0
        self.acceleration = 1.0
        self.stepInterval = 0
        self.minPulseWidth = 1
        self.dirInverted = false
        self.stepInverted = false
        self.enablePin = 0xff
        self.lastStepTime = 0
        self.pin1 = 0
        self.pin2 = 0
        self.pin3 = 0
        self.pin4 = 0
        self.forward = forward
        self.backward = backward
    
    def __del__(self):
        self.disableOutput()
    
    def moveTo(self, absolute):
        self.targetPos = absolute
        self.computeNewSpeed()
    
    def move(self, relative):
        self.moveTo(self.currentPos + relative)
    
    def runSpeed(self):
        if(self.speed == 0.0):
            return False
        
        time = micros()
        
        nextStepTime = self.lastStepTime + self.stepInterval
        
        if (   ((nextStepTime >= self.lastStepTime) and ((time >= nextStepTime) or (time < self.lastStepTime)))
        or ((nextStepTime < self.lastStepTime) and ((time >= nextStepTime) and (time < self.lastStepTime)))):
            if(self.speed > 0.0):
               self.currentPos +=1
            elif (self.speed < 0.0):
               self.currentPos -=1
            
            step(self.currentPos & 0x7)
            
            self.lastStepTime = time
            return true
        else:
            return false
    
    def distanceToGo(self):
        return self.targetPos - self.currentPos
    
    def targetPosition(self):
        return self.targetPos
    def currentPosition(self):
        return self.currentPos
    def setCurrentPosition(self, position):
        self.targetPos=self.currentPos=pos
        self.computeNewSpeed()
    def computeNewSpeed(self):
        self.setSpeed(self.desiredSpeed())
    
    def desiredSpeed(self):
        distanceTo = self.distanceToGo() # +ve is clockwise from curent location
         
        if (distanceTo == 0):
            return 0.0 # We're there
         
         # sqrSpeed is the signed square of self.speed.
        sqrSpeed = self.speed*(self.speed)
         
        if (self.speed < 0.0):
             sqrSpeed = -sqrSpeed

        twoa = 2.0 * self.acceleration # 2ag
        # if v^^2/2as is the the left of target, we will arrive at 0 speed too far -ve, need to accelerate clockwise
        if ((sqrSpeed / twoa) < distanceTo):
            # Accelerate clockwise
            # Need to accelerate in clockwise direction
            if (self.speed == 0.0):
               requiredSpeed = sqrt(twoa)
            else:
               requiredSpeed = self.speed + fabs(self.acceleration / self.speed)
            if (requiredSpeed > self.maxSpeed):
               requiredSpeed = self.maxSpeed
        
        else:
            # Decelerate clockwise, accelerate anticlockwise
            # Need to accelerate in clockwise direction
            if (self.speed == 0.0):
               requiredSpeed = -sqrt(twoa)
            else:
               requiredSpeed = self.speed - fabs(self.acceleration / self.speed)
            
            if (requiredSpeed < -self.maxSpeed):
               requiredSpeed = -self.maxSpeed
        
        return requiredSpeed
    def run(self):
        if(self.targetPos == self.currentPos):
            return False
        else:
            if (self.runSpeed()):
               self.computeNewSped()
            return True

    
    def setMaxSpeed(self, speed):
        self.maxSpeed =speed
        self.computeNewSpeed()
    
    def setAcceleeration(self, acceleration):
        self.acceleration = acceleration
        self.computeNewSpeed()
    
    def setSpeed(self, speed):
        if(speed == self.speed):
            return
        if( (speed > 0.0) and (speed > self.maxSpeed) ):
            self.speed=self.maxSpeed
        elif( (speed < 0.0) and (speed < -1*self.maxSpeed)):
            self.speed= -1*self.maxSpeed
        else:
            self.speed=speed
        
        self.stepInterval =fabs(1000000.0 / self.speed)
    
    def speed(self):
        return self.speed
    
    def step(self, step):
        if(self.pins==0):
            step0()
        elif(self.pins==1):
            step1(step)
        elif(self.pins==2):
            step2(step)
        elif(self.pins==4):
            step4(step)
        elif(self.pins==8):
            step8(step)
    
    # 0 pin step function (ie for functional usage)
    def step0(self):
        if(self.speed >0):
            self.forward()
        else:
            self.backward

# 1 pin step function (ie for stepper drivers)
# This is passed the current step number (0 to 7)
# Subclasses can override
    def step1(self, step):
        self.pin2Write( (self.speed > 0) ^ self.dirInverted)
        
        # Caution 200ns setup time
        self.pin1Write(GPIO.HIGH ^ self.stepInverted)
        # Delay the minimum allowed pulse width
        delayMicroseconds(_minPulseWidth)
        self.pin1Write(GPIO.LOW ^ self.stepInverted)


# 2 pin step function
# This is passed the current step number (0 to 7)
# Subclasses can override
    def step2(self, step):
        cond=(step & 0x3)
        if(cond ==0): # /* 01 */
            self.pin1Write(GPIO.LOW)
            self.pin2Write(GPIO.HIGH)
        elif(cond == 1): # /* 11 */
            self.pin1Write(GPIO.HIGH)
            self.pin2Write(GPIO.HIGH)
        elif(cond == 2): # /* 10 */
            self.pin1Write(GPIO.HIGH)
            self.pin2Write(GPIO.LOW)
        elif(cond == 3): # /* 00 */
            self.pin1Write(GPIO.LOW)
            self.pin2Write(GPIO.LOW)


# 4 pin step function for half stepper
# This is passed the current step number (0 to 7)
# Subclasses can override
    def step4(self, step):
        cond=(step & 0x3)
        
        if(cond == 0): # 1010
            self.pin1Write( GPIO.HIGH)
    	    self.pin2Write( GPIO.LOW)
    	    self.pin3Write( GPIO.HIGH)
    	    self.pin4Write( GPIO.LOW)
        
        elif(cond == 1):    # 0110
    	    self.pin1Write( GPIO.LOW)
    	    self.pin2Write( GPIO.HIGH)
    	    self.pin3Write( GPIO.HIGH)
    	    self.pin4Write( GPIO.LOW)
        
        elif(cond == 2):    #0101
    	    self.pin1Write( GPIO.LOW)
    	    self.pin2Write( GPIO.HIGH)
    	    self.pin3Write( GPIO.LOW)
    	    self.pin4Write( GPIO.HIGH)
        
        elif(cond ==  3):    #1001
    	    self.pin1Write( GPIO.HIGH)
    	    self.pin2Write( GPIO.LOW)
    	    self.pin3Write( GPIO.LOW)
    	    self.pin4Write( GPIO.HIGH)

# 4 pin step function
# This is passed the current step number (0 to 7)
# Subclasses can override
    def step8(self, step):
        cond= (step & 0x7)
    
        if(cond== 0):    # 1000
            self.pin1Write( GPIO.HIGH)
            self.pin2Write( GPIO.LOW)
            self.pin3Write( GPIO.LOW)
            self.pin4Write( GPIO.LOW)
        
        elif(cond==  1):    # 1010
            self.pin1Write( GPIO.HIGH)
            self.pin2Write( GPIO.LOW)
            self.pin3Write( GPIO.HIGH)
            self.pin4Write( GPIO.LOW)
	
        elif(cond==  2):    # 0010
            self.pin1Write( GPIO.LOW)
            self.pin2Write( GPIO.LOW)
            self.pin3Write( GPIO.HIGH)
            self.pin4Write( GPIO.LOW)
            
        
        elif(cond==  3):    # 0110
            self.pin1Write( GPIO.LOW)
            self.pin2Write( GPIO.HIGH)
            self.pin3Write( GPIO.HIGH)
            self.pin4Write( GPIO.LOW)
	
        elif(cond==  4):    # 0100
            self.pin1Write( GPIO.LOW)
            self.pin2Write( GPIO.HIGH)
            self.pin3Write( GPIO.LOW)
            self.pin4Write( GPIO.LOW)
        
        elif(cond==  5):    #0101
            self.pin1Write( GPIO.LOW)
            self.pin2Write( GPIO.HIGH)
            self.pin3Write( GPIO.LOW)
            self.pin4Write( GPIO.HIGH)
        
        elif(cond==  6):    # 0001
            self.pin1Write( GPIO.LOW)
            self.pin2Write( GPIO.LOW)
            self.pin3Write( GPIO.LOW)
            self.pin4Write( GPIO.HIGH)
        
        elif(cond==  7):    #1001
            self.pin1Write( GPIO.HIGH)
            self.pin2Write( GPIO.LOW)
            self.pin3Write( GPIO.LOW)
            self.pin4Write( GPIO.HIGH)


# Prevents power consumption on the outputs
    def disableOutputs(self):
        if (not self.pins):
            return
    
        if (self.pins == 1):
            # Invert only applies for stepper drivers.
            self.pin1Write( GPIO.LOW ^ self.stepInverted)
            self.pin2Write( GPIO.LOW ^ self.dirInverted)
        else:
             self.pin1Write( GPIO.LOW)
             self.pin2Write( GPIO.LOW)
	
        if (self.pins == 4 or self.pins == 8):
            self.pin3Write( GPIO.LOW)
            self.pin4Write( GPIO.LOW)
        if (self.enablePin != 0xff):
            self.enablePinWrite( GPIO.LOW ^ _enableInverted)
  
    def enableOutputs(self):
        if (not self.pins):
	        return
    
        self.pin1Write=getGPIOWriteFun(self.pin1)
        self.pin1Write(GPIO.LOW)
        
        self.pin2Write=getGPIOWriteFun(self.pin2)
        self.pin2Write(GPIO.LOW)
        
        if (self.pins == 4 or self.pins == 8):
        
            self.pin3Write=getGPIOWriteFun(self.pin3)
            self.pin3Write(GPIO.LOW)
        
            self.pin4Write=getGPIOWriteFun(self.pin4)
            self.pin4Write(GPIO.LOW)
        
    	if (self.enablePin != 0xff):
            self.enablePinWrite=getGPIOWriteFun(self.enablePin)
            self.enablePinWrite(GPIO.HIGH ^ _enableInverted)
    
    def setMinPulseWidth(self, minWidth):
        self.minPulseWidth=minWidth
    
    def setEnablePin(enablePin):
        self.enablePin = enablePin
        # This happens after construction, so init pin now.
        if (self.enablePin != 0xff):
            self.enablePinWrite=getGPIOWriteFun(enablePin)
            self.enablePinWrite(GPIO.HIGH ^ _enableInverted)
            

    
    def setPinsInverted(self, direction, step, enable):
        self.dirInverted=direction
        self.stepInverted=step
        self.enableInverted = enable
    
    def runToPosition(self):
        while (True):
            run()
    
    def runSpeedToPosition(self):
        if (self.targetPos >self.currentPos):
            self.speed = fabs(self.speed)
        else:
	        self.speed = -fabs(self.speed)
        
        if (self.targetPos!=self.currentPos):
            self.runSpeed()
        else:
            False
    
    # Blocks until the new target position is reached
    def runToNewPosition(self, position):
        self.moveTo(position)
        self.runToPosition()
