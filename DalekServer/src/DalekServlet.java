import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;

import java.util.logging.Logger;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;

import dalek.server.contolunits.ArduinoUSBDriver;
import dalek.server.contolunits.BeagleBoneDriver;
import dalek.server.contolunits.ControlUnit;


public class DalekServlet {
	static HashMap<Integer, ControlUnit> control_units=new HashMap<Integer, ControlUnit>();
	
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
		
		
	}
	
	private static void Init() throws Exception{
		Init_Control_Units();
		Init_Devices();
	}
	
	private static void STOP_DEVICES() throws Exception{
		MotorDevice.stop_all_motors();
	}
	private static void Init_Control_Units() throws Exception{
		//Future: Add file read for multiple devices
		//Each Control unit other then the root running the server would be connected via network/usb.
		//Each Control unit instance will encapsulate how to connect to that particular control unit.
		//So if you had say an arduino running script then you would communicate with it, telling it which pins to adjust
		ControlUnit cu=new BeagleBoneDriver();
		control_units.put(0, cu );
		

		//For Arduino
		
		control_units.put(1, new ArduinoUSBDriver() );
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private static void Init_Devices() throws Exception{
		try {
			Scanner in=new Scanner(new File(config_file));
			int line_num=0;
			while(in.hasNextLine())
			{
				String s=in.nextLine();
				if(s.isEmpty()){
					continue;
				}
				Scanner in2=new Scanner(s);
				int device=in2.nextInt();
				if(device==MotorDevice.DEVICE_ID){
					try {
						MotorDevice m =MotorDevice.init_motor_device(in2);
				    	DalekServlet.LOGGER.info(String.format("Line Number: %d Loaded:\n%s",line_num,m.toString()));

					} catch (Exception e) {
						System.err.printf("Line Number %d holds an illformed device description\n",line_num);
						throw e;
					}
				}
				else{
					System.err.printf("Line Number %d holds an unknown device id\n",line_num);
				}
				line_num++;
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			
			return;
		}
		
		
	}
	
	private static void LOG_Device_States(){
		MotorDevice.LOG_Device_States();
	}
}
