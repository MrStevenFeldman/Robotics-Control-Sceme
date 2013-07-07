
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
from Device import *

 



 

deviceMap = {}
def start():
    intilizeServer()
    
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
       
        #TODO: Send XML containing Devices (Id, Name, Type)
        while 1: #while connected to client
            data = conn.recv(1)
            if(len(data)==0):
                break
                

            deviceID = int(ord(data))
            
            
            if not deviceID:
                print "Not Device ID detected?"
                break
           # print 'Device ID: ', deviceID
            device = deviceMap.get(deviceID,"empty")
        
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
    for e in deviceMap:
        print deviceMap[e].toString()
        
def resetServer():
    #Todo, iterate over devices and call reset 
    for e in deviceMap:
        print deviceMap[e].reset()
    
def intilizeServer():
    loadXML('HardWareInfo.xml')
    
def loadXML(fname):
    from xml.dom import minidom

    xmldoc = minidom.parse('HardWareInfo.xml')
    itemlist = xmldoc.getElementsByTagName('Device') 
    print 'Loading ',len(itemlist), ' devices'

    for s in itemlist : 
        dType = s.attributes['type'].value
        dId = s.getElementsByTagName('ID').item(0).firstChild.nodeValue
        name = s.getElementsByTagName('NAME').item(0).firstChild.nodeValue
        
        if (dType == 'BiMotor'):
            dpinA = s.getElementsByTagName("PWMA_PIN").item(0).firstChild.nodeValue
            denableA = s.getElementsByTagName("ENABLE_PIN").item(0).firstChild.nodeValue
            dcsA = s.getElementsByTagName("CURRENTA").item(0).firstChild.nodeValue
            dpinB = s.getElementsByTagName("PWMB_PIN").item(0).firstChild.nodeValue
           
            device = BiMotor(dId, name, dpinA, dpinB, denableA, dcsA)
            
        
        elif (dType == 'MotorPair'):
            motorList = s.getElementsByTagName("Motor1")
            
            dpinA = motorList.item(0).getElementsByTagName("PWMA_PIN").item(0).firstChild.nodeValue
            denableA = motorList.item(0).getElementsByTagName("ENABLE_PIN").item(0).firstChild.nodeValue
            dcsA = motorList.item(0).getElementsByTagName("CURRENTA").item(0).firstChild.nodeValue
            dpinB = motorList.item(0).getElementsByTagName("PWMB_PIN").item(0).firstChild.nodeValue
            name2 = motorList.item(0).getElementsByTagName('NAME').item(0).firstChild.nodeValue
            ddm1 = BiMotor(dId, name2, dpinA, dpinB, denableA, dcsA)
            
            motorList = s.getElementsByTagName("Motor2") 
            dpinA = motorList.item(0).getElementsByTagName("PWMA_PIN").item(0).firstChild.nodeValue
            denableA = motorList.item(0).getElementsByTagName("ENABLE_PIN").item(0).firstChild.nodeValue
            dcsA = motorList.item(0).getElementsByTagName("CURRENTA").item(0).firstChild.nodeValue
            dpinB = motorList.item(0).getElementsByTagName("PWMB_PIN").item(0).firstChild.nodeValue
            name2 = motorList.item(0).getElementsByTagName('NAME').item(0).firstChild.nodeValue
            ddm2 = BiMotor(dId, name2, dpinA, dpinB, denableA, dcsA)
            
            device = MotorPair(dId, name, ddm1, ddm2)
        
            
            
        else:
            print 'Unknown device type: ', dType
            exit()
    
        deviceMap[int(dId)] = device
        #print device.toString()
def getText(nodelist):
    rc = []
    for node in nodelist:
        if node.nodeType == node.TEXT_NODE:
            rc.append(node.data)
    return ''.join(rc)
        
        
start()