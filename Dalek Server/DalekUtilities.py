import Adafruit_BBIO.GPIO as GPIO
import Adafruit_BBIO.ADC as ADC
import Adafruit_BBIO.PWM as PWM
import Adafruit_I2C  as I2C

i2cMap={}

#I2C

PCA9685_MODE1 = 0x0
PCA9685_PRESCALE = 0xFE

#end I2C

pwm_freq = 2000 #Todo calibrarte to motors

def getPWMWriteFun(pin):
    if(pin[0]=='P'):
        PWM.start(pin, 0)
        PWM.set_frequency(pin, pwm_freq)
        return partial(PWM.set_duty_cycle,pin)
    elif(pin[0]=='I'):
        l=self.pinB.rsplit("_")
        if ( l[2] in i2cMap):
            i2c=i2cMap[l[2]]
        else:
            i2c=Adafruit_I2C(l[2],1 , False)
            i2c.write8(PCA9685_MODE1, 0x01)
            time.sleep(0.05)
            i2cMap[l[2]]=i2c
        return partial(setI2CDutyCycle, i2c, l[3])
            
    elif (pin[0]=='D'):
        def funt(d):
            return d
        return partial(funt)        
    else
        exit(-1)
    
def getGPIOWriteFun(pin):
    if(pin[0]=='P'):
        GPIO.setup(pin, GPIO.OUT)
        return partial(GPIO.output(),pin)
        
    elif(pin[0]=='I'):
        l=self.pinB.rsplit("_")
        if ( l[2] in i2cMap):
            i2c=i2cMap[l[2]]
        else:
            i2c=Adafruit_I2C(l[2],1 , False)
            i2c.write8(PCA9685_MODE1, 0x01)
            time.sleep(0.05)
            i2cMap[l[2]]=i2c
        
        return partial(setI2CDutyCycle, i2c, l[3])
    elif (pin[0]=='D'):
        def funt(d):
            return d
        return partial(funt)
    else
      exit(-1)  
    
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


def setI2CDutyCycle(i2c, channel, cycle):
	off = min(1, cycle)
	off = int(cycle*4095)
	bytes = [0x00, 0x00, off & 0xFF, off >> 8]
	i2c.writeList(0x06+(4*channel), bytes)

                 