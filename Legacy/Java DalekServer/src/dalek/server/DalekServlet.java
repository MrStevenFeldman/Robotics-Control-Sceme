package dalek.server;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Scanner;

import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import dalek.server.contolunits.SerialUSBDriver;
import dalek.server.contolunits.BeagleBoneDriver;
import dalek.server.contolunits.ControlUnit;


public class DalekServlet {
	static HashMap<Integer, ControlUnit> control_units=new HashMap<Integer, ControlUnit>();
	final static boolean MANUAL_MODE=false;
	
	final static String config_file="Device.list";
	public static Logger LOGGER = Logger.getLogger("InfoLogging");
	
	public static void main(String []args ){
		DalekServlet.LOGGER.info("Initilizing Devie List");
		try {
			Init();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			DalekServlet.LOGGER.info("Unable to Initilize Servlet, Servlet will Now Exit");
			return;
			
		}
		
    	DalekServlet.LOGGER.info("Servlet Ready");
    	if(MANUAL_MODE){
    		Scanner in=new Scanner(System.in);
    		int fbyte=in.nextByte();
	    	LOGGER.info(String.format("First Byte: %d\n", fbyte));
		    
		    while(true){
		    	int res=in.nextByte();
			   
		    	LOGGER.info(String.format("Read: %d as Device Type Id", res));
		    	
		    	if(res ==  MotorDevice.DEVICE_ID){
		    		int res2= MotorDevice.process_motor_command(System.in);
		    		
		    		if(res2==-1){
		    			System.err.println("Error in proc process_motor_command closing the connection\n");
		    		}
		    		
		    	}
		    	else{
		    		System.err.println("Unexpected Device ID or Byte Value in input stream closing the connection\n");
	
		    	}

		    	LOG_Device_States();
		    	
		    }
	
    		
    		
    	}
    	else{
	    	try {
	    		InetAddress ip = InetAddress.getLocalHost();
	    		System.out.println("Current IP address : " + ip.getHostAddress());
	    
	   	  	} catch (UnknownHostException e) {
	   	  		e.printStackTrace();
	   	  	}
			try {
			    int port = 9000;
			   // ServerSocketFactory ssocketFactory = SSLServerSocketFactory.getDefault();
			   // ServerSocket ssocket = ssocketFactory.createServerSocket(port);
		
			    ServerSocket ssocket = new ServerSocket(port);
			  
			    // Listen for connections
			    while(true){
				    Socket socket = ssocket.accept();
			
				    DalekServlet.LOGGER.info("Client has connected");
				    
				    // Create streams to securely send and receive data to the client
				    InputStream in = socket.getInputStream();
				    OutputStream out = socket.getOutputStream();
			
				    PrintWriter pout=new PrintWriter(out);
				    int fbyte=in.read();
			    	LOGGER.info(String.format("First Byte: %d\n", fbyte));
	
				    pout.println("Controller has been connected to the Control Unit");
				    pout.flush();
				    
				    while(	true){
				    	LOGGER.info("Waiting For Command");

				    	int res;
				    	try{
					    	res=in.read();
					    	if(res==-1){
					    		break;
					    	}
				    	}catch(Exception e){
				    		LOGGER.info("Device Disconnected\n");
				    		break;
				    	}
				    	LOGGER.info(String.format("Read: %d as Device Type Id", res));
				    	
				    	if(res ==  MotorDevice.DEVICE_ID){
				    		int res2= MotorDevice.process_motor_command(in);
				    		
				    		if(res2==-1){
				    			System.err.println("Error in proc process_motor_command closing the connection\n");
					    		
					    		out.write( -1);
					    		
					    		in.close();
					    		out.close();
					    		break;
				    		}
				    		
				    	}
				    	else{
				    		System.err.println("Unexpected Byte Value in input stream closing the connection\n");
				    		
				    		out.write( -1);
				    		break;
				    	}
		
				    	LOG_Device_States();
				    	
				    	 try{
				    		 out.write(1);
				    	 } catch (Exception e) {
				    		 break;
						 }
				    }
			
				    // Close the socket
				    try{
				    	in.close();
				    	out.close();
				    } catch (Exception e) {
				    	
				    }
				    try{
						STOP_DEVICES();
					} catch (Exception e) {
						try {
							STOP_DEVICES();
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						e.printStackTrace();
					}
				    
			    }
			} catch(IOException e) {
				
				e.printStackTrace();
			}finally{
				try {
					STOP_DEVICES();
				} catch (Exception e) {
					try {
						STOP_DEVICES();
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					e.printStackTrace();
				}
			}
	    	LOGGER.info("CompletedCommand");
    	}
		
		
	}
	
	private static void Init() throws Exception{
		try {

			File stocks = new File("HardWareInfo.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(stocks);
			doc.getDocumentElement().normalize();

			NodeList nodelist1 = doc.getElementsByTagName("ControlDevice");
			NodeList nodelist2 = doc.getElementsByTagName("Device");

			Init_Control_Units(nodelist1);
			Init_Devices(nodelist2);
			
		}catch (Exception e){
			e.printStackTrace();
			throw e;
			
		}
		
	}
	
	
	private static void Init_Control_Units(NodeList nodelist) throws Exception{
		//Future: Add file read for multiple devices
		//Each Control unit other then the root running the server would be connected via network/usb.
		//Each Control unit instance will encapsulate how to connect to that particular control unit.
		//So if you had say an arduino running script then you would communicate with it, telling it which pins to adjust
		
		for(int i=0; i<nodelist.getLength(); i++){
			Node n=nodelist.item(i);
			
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				 
				Element eElement = (Element) n;
				//eElement.getElementsByTagName("").item(0).getTextContent());
			
				String temp=eElement.getElementsByTagName("ID").item(0).getTextContent();
				int id=Integer.parseInt(temp);
				
				Element conn_node=(Element)eElement.getElementsByTagName("ConnectionType").item(0);
				temp=conn_node.getTextContent();
				
				if(temp.equals("HostDevice")){
					conn_node=(Element)eElement.getElementsByTagName("name").item(0);

					temp=conn_node.getTextContent();
				
					if(temp.equals("BeagleBoardBlack")){
						/*<devicetype>BeagleBoardBlack</devicetype>
						<magicCapeId>9</magicCapeId>
						<magicFolderId>15</magicFolderId>
						<pwm_period>50000</pwm_period>
						*/
						conn_node=(Element)eElement.getElementsByTagName("magicCapeId").item(0);
						int mci=Integer.parseInt(conn_node.getTextContent());

						
						conn_node=(Element)eElement.getElementsByTagName("pwm_period").item(0);
						int per=Integer.parseInt(conn_node.getTextContent());
						
						ControlUnit cu=new BeagleBoneDriver(mci, per);
						control_units.put(id, cu );
					}
					else{
						throw new Exception ("Unknown Host Device");
					}
				}
				else if(temp.equals("SerialDevice")){
					
					//control_units.put(1, new SerialUSBDriver("/dev/tty.usbmodem1421",9600) );
					
					String port=conn_node.getAttribute("PORT");
					String br_s=conn_node.getAttribute("BAUD_RATE");
					int br=Integer.parseInt(br_s);
					
					try{
						control_units.put(id, new SerialUSBDriver(port,br) );
					}catch(Exception e){
						throw e;
					}
					//TODO Finish
				}
				else if(temp.equals("NetworkDevice")){
					throw new Exception("NetworkDevice control unit has not been coded yet");
				}
				else{
					throw new Exception("Unknown Control unit type");
				}
			}
		}
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private static void Init_Devices(NodeList nodelist) throws Exception{
		
		
		for(int i=0; i<nodelist.getLength(); i++){
			Node n=nodelist.item(i);
			
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				 
				Element eElement = (Element) n;
				//eElement.getElementsByTagName("").item(0).getTextContent());
			
				/**<Device type=MotorDevice>
						<Name>leftmotor</Name>
						<ID>1</ID>
						<ControlDeviceID>2</ControlDeviceID>
						
						<Type>BiDirectional</Type>
						<PWMA_PIN>8</PWMA_PIN>
						<PWMB_PIN>9</PWMB_PIN>
						<ENABLE_PIN>10</ENABLE_PIN>
						<CURRENTA>2</CURRENTA>
						<CURRENTB>2</CURRENTB>
			
					</Device> **/
				
				String type=eElement.getAttribute("type");
				
				if(type.equals("MotorDevice")){
					try {
						String name_s=eElement.getElementsByTagName("Name").item(0).getTextContent();
						String id_s=eElement.getElementsByTagName("ID").item(0).getTextContent();
						String cd_s=eElement.getElementsByTagName("ControlDeviceID").item(0).getTextContent();
						String type_s=eElement.getElementsByTagName("Type").item(0).getTextContent();
						
						boolean isBi=false;
						if(type_s.equals("BiDirectional")){
							isBi=true;
						}
						else if(type_s.equals("UniDirectional")){
							isBi=false;
						}
						else{
							throw new Exception("Unknown MotorDevice Type");
						}
						
						String pwma_s=eElement.getElementsByTagName("PWMA_PIN").item(0).getTextContent();	
						String enable_s=eElement.getElementsByTagName("ENABLE_PIN").item(0).getTextContent();
						String currentA_s=eElement.getElementsByTagName("CURRENTA").item(0).getTextContent();						
					
						int id_i=Integer.parseInt(id_s);
						int cd_i=Integer.parseInt(cd_s);
						int enable_i=Integer.parseInt(enable_s);
						int pwma_i=Integer.parseInt(pwma_s);
						int currentA_i=Integer.parseInt(currentA_s);
						//	public MotorDevice(int id, int enablePin, int PWM_A, int PWM_B, int currentSenseA, int currentSenseB, int cu_id ) throws Exception{

						MotorDevice m;
						if(isBi){
							
							String pwmb_s=eElement.getElementsByTagName("PWMB_PIN").item(0).getTextContent();
							String currentB_s=eElement.getElementsByTagName("CURRENTB").item(0).getTextContent();
							int pwmb_i=Integer.parseInt(pwmb_s);
							int currentB_i=Integer.parseInt(currentB_s);
							
							
							m =new MotorDevice(id_i, enable_i,pwma_i,pwmb_i,currentA_i,currentB_i, cd_i);
						}
						else{
							m =new MotorDevice(id_i, enable_i,pwma_i,currentA_i, cd_i);

						}
				    	DalekServlet.LOGGER.info(String.format("Loaded Motor %s with id %d. %s \n",name_s,id_i,m.toString()));

					} catch (Exception e) {
						throw e;
					}
				}
			}
		}
		
		
		
		
	}
	
	private static void STOP_DEVICES() throws Exception{
		MotorDevice.stop_all_motors();
	}
	
	private static void LOG_Device_States(){
		MotorDevice.LOG_Device_States();
	}
}
