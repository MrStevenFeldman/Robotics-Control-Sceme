
#Todo

#Modify Devices to include Control Unit Selection
#Initilization
#Step 1: Parse XML File
#	Read Each Device
#	Store Devices into a map
#Step 2: Start Server
#Commands: ID-OpCode-Param1-Param2
#Code Shut Down.
#



import socket
from ControlDevices import *
 






controlDeviceMap = {}
sensorDeciceMap = {}

def start():
    intilizeServer()
    printState()
    exit()
    
    HOST = ''                 # Symbolic name meaning the local host
    PORT = 50003             # Arbitrary non-privileged port
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.bind((HOST, PORT))
    s.listen(1)
    
    while 1:
        printState()
        print 'Waiting for Client'
        conn, addr = s.accept()
        print 'Connected by', addr
        start = time.time()
        
        #TODO: Send String containing Devices (Id, Name, Type)
        while 1: #while connected to client
            data = conn.recv(1)
            if(len(data)==0):
                break

            
            deviceID = int(ord(data))
            
            
            if not deviceID:
                print "Not Device ID detected?"
                break
           # print 'Device ID: ', deviceID
            device = controlDeviceMap.get(deviceID,"empty")
            
            if (device == "empty"):
                print "No Device Found with Device Id", deviceID
                break
            else:
            #    print 'Before:\n\t',device.toString()
                 device.command(conn)
           #     print 'After:\n\t',device.toString()
           
           # conn.send(1)
            printState()
        elapsed = (time.time() - start)
        print "It took: ", elapsed
        
        conn.close()
        printState()
        resetServer()
       
       # break;
    
    s.close()
    


def printState():
    for e in controlDeviceMap:
        print controlDeviceMap[e].toString()

def resetServer():
    #Todo, iterate over devices and call reset
    for e in controlDeviceMap:
        print controlDeviceMap[e].reset()

def getServerInfo():
    #Todo, iterate over devices and call reset
    
    for e in controlDeviceMap:
        s="%s%s\t" %{s, controlDeviceMap[e].getType()}
        s="%s%s\t" %{s, controlDeviceMap[e].deveiceId}
        s="%s%s\t" %{s, controlDeviceMap[e].name}
    
    s='%s%c' %(s, '\n')
    
    return s

def intilizeServer():
    loadXML('HardWareInfo.xml')

def loadXML(fname):
    from xml.dom import minidom
    
    xmldoc = minidom.parse('HardWareInfo.xml')
    devicelist = xmldoc.getElementsByTagName('DeviceList')[0]

    
    for s in devicelist.childNodes :
        if (s.nodeName == "#text"):
            continue
        
        dType = s.nodeName
        dId = s.getElementsByTagName('ID').item(0).firstChild.nodeValue
        name = s.getElementsByTagName('NAME').item(0).firstChild.nodeValue
        
    
        if (dType == 'Motor'):
            motorOp = s.getElementsByTagName('MotorObj')[0]
            dpinA = motorOp.getElementsByTagName("PWMA_PIN").item(0).firstChild.nodeValue
            denableA = motorOp.getElementsByTagName("ENABLE_PIN").item(0).firstChild.nodeValue
            dcsA = motorOp.getElementsByTagName("CURRENT_PIN").item(0).firstChild.nodeValue
            dpinB = motorOp.getElementsByTagName("PWMB_PIN").item(0).firstChild.nodeValue
            
            device = Motor(dId, name, dpinA, dpinB, denableA, dcsA)
        
        
        elif (dType == 'MotorPair'):
            motorOp = s.getElementsByTagName('MotorObj')[0]
            
            dpinA = motorOp.getElementsByTagName("PWMA_PIN").item(0).firstChild.nodeValue
            denableA = motorOp.getElementsByTagName("ENABLE_PIN").item(0).firstChild.nodeValue
            dcsA = motorOp.getElementsByTagName("CURRENT_PIN").item(0).firstChild.nodeValue
            dpinB = motorOp.getElementsByTagName("PWMB_PIN").item(0).firstChild.nodeValue
            name1 = motorOp.getElementsByTagName('NAME').item(0).firstChild.nodeValue
            ddm1 = Motor(dId, name1, dpinA, dpinB, denableA, dcsA)
            
            
            motorOp = s.getElementsByTagName('MotorObj')[1]
            
            dpinA = motorOp.getElementsByTagName("PWMA_PIN").item(0).firstChild.nodeValue
            denableA = motorOp.getElementsByTagName("ENABLE_PIN").item(0).firstChild.nodeValue
            dcsA = motorOp.getElementsByTagName("CURRENT_PIN").item(0).firstChild.nodeValue
            dpinB = motorOp.getElementsByTagName("PWMB_PIN").item(0).firstChild.nodeValue
            name2 = motorOp.getElementsByTagName('NAME').item(0).firstChild.nodeValue
            ddm2 = Motor(dId, name2, dpinA, dpinB, denableA, dcsA)
            
            device = MotorPair(dId, name, ddm1, ddm2)
        
        elif (dType == 'Servo'):
            servoOp = s.getElementsByTagName('ServoObj')[0]
            pwmpin = servoOp.getElementsByTagName("PWM_PIN").item(0).firstChild.nodeValue
            pwmmax = servoOp.getElementsByTagName("PWM_MAX").item(0).firstChild.nodeValue
            pwmmin = servoOp.getElementsByTagName("PWM_MIN").item(0).firstChild.nodeValue
            maxAngle = servoOp.getElementsByTagName("ANGLE_MAX").item(0).firstChild.nodeValue
            minAngle = servoOp.getElementsByTagName("ANGLE_MIN").item(0).firstChild.nodeValue
            initAngle = servoOp.getElementsByTagName("ANGLE_INIT").item(0).firstChild.nodeValue
            name = servoOp.getElementsByTagName('NAME').item(0).firstChild.nodeValue
            
            device = Servo(dId, name, pwmpin, minAngle, maxAngle, initAngle, pwmmin, pwmmax)
        
            
        
        elif (dType == 'ServoPair'):
            servoOp = s.getElementsByTagName('ServoObj')[0]
            pwmpin = servoOp.getElementsByTagName("PWM_PIN").item(0).firstChild.nodeValue
            pwmmax = servoOp.getElementsByTagName("PWM_MAX").item(0).firstChild.nodeValue
            pwmmin = servoOp.getElementsByTagName("PWM_MIN").item(0).firstChild.nodeValue
            maxAngle = servoOp.getElementsByTagName("ANGLE_MAX").item(0).firstChild.nodeValue
            minAngle = servoOp.getElementsByTagName("ANGLE_MIN").item(0).firstChild.nodeValue
            initAngle = servoOp.getElementsByTagName("ANGLE_INIT").item(0).firstChild.nodeValue
            name = servoOp.getElementsByTagName('NAME').item(0).firstChild.nodeValue
            
            servo1 = Servo(dId, name, pwmpin, minAngle, maxAngle, initAngle, pwmmin, pwmmax)
            
            
            servoOp = s.getElementsByTagName('ServoObj')[1]
            pwmpin = servoOp.getElementsByTagName("PWM_PIN").item(0).firstChild.nodeValue
            pwmmax = servoOp.getElementsByTagName("PWM_MAX").item(0).firstChild.nodeValue
            pwmmin = servoOp.getElementsByTagName("PWM_MIN").item(0).firstChild.nodeValue
            maxAngle = servoOp.getElementsByTagName("ANGLE_MAX").item(0).firstChild.nodeValue
            minAngle = servoOp.getElementsByTagName("ANGLE_MIN").item(0).firstChild.nodeValue
            initAngle = servoOp.getElementsByTagName("ANGLE_INIT").item(0).firstChild.nodeValue
            name = servoOp.getElementsByTagName('NAME').item(0).firstChild.nodeValue
            
            servo2 = Servo(dId, name, pwmpin, minAngle, maxAngle, initAngle, pwmmin, pwmmax)
            
            device = ServoPair(dId, name, servo1, servo2)
        elif (dType == 'Stepper'):
            pass
        
        else:
            print 'Unknown device type: ', dType
            exit()
        
        controlDeviceMap[int(dId)] = device
        #print device.toString()
def getText(nodelist):
    rc = []
    for node in nodelist:
        if node.nodeType == node.TEXT_NODE:
            rc.append(node.data)
    return ''.join(rc)
        

start()