
package com.example.dalekcontroller;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;


import android.app.Activity;
import android.content.Intent;
import android.os.NetworkOnMainThreadException;
import android.util.Log;

public class DalekServerConnect extends Thread {
	
	public static DalekServerConnect live_connect=null;
	
	
	public ConnectionState state_v=ConnectionState.Uninitiated;
	public String message="No Error";
	private final int timeout=10000;
	private int port = 50003;
	private String s_address;
	private Socket socket;
	private OutputStream out = null;
	private BufferedReader in = null;
	
    public DalekServerConnect(String address) {
    	s_address=address;
    }

    @Override
    public synchronized void run() {
        try {
        	state_v=ConnectionState.Connecting;
        	socket = new Socket();
        	socket.setSoTimeout(timeout);
        	socket.connect(new InetSocketAddress(s_address,port), timeout);
        	
        	if(!socket.isConnected()){
        		state_v=ConnectionState.Error;
        		 message="Unable to reach host after timout of "+timeout+"ms";
        		return;
        	}
        	else{
				out =socket.getOutputStream();
				in = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
				
				
			//	out.write(1);
			//	String s=in.readLine();
	  		 // 	Log.v("DCU_MSG", "Response: "+s);
	 
		  		 state_v=ConnectionState.Connected;
		  		 message="Succesfully Connected";
		  		live_connect=this;
        	}
  		
        	
        	//TODO use this thread to read input from the dalek.
        	//Should be syncronized with the syncrhonization below
  		  
  		  
        } catch (IOException e) {
            e.printStackTrace();
            try {
            	if(socket.isConnected())
            		socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            
            state_v=ConnectionState.Error;
            message="Unable to reach host after timout of "+timeout+"ms"+e.getMessage();
        }
        
        
    }
    public synchronized boolean sendCommand(byte [] buffer){
    	
    	if(state_v==ConnectionState.Connecting){
    		Log.e("ConnectionError", "Attempting to send command while still trying to connect");

    		message=("Already Connecting");
    		return false;
    		
    	}
    	if(state_v==ConnectionState.Uninitiated){
    		Log.e("ConnectionError", "Attempting to command while Socket is Unitiated.");
    		return false;
    	}
    	else if(state_v==ConnectionState.Disconnected){
    		Log.e("ConnectionError", "Attempting to command while Socket is  Disconnected.");
    		return false;
    	}
    
    	try {
		  if(!socket.isConnected()){
    		  Log.e("ConnectionError", "Attempting to command while Socket is not connected.");
    		  message=("Connection Terminated");
    		  
    		  state_v=ConnectionState.Disconnected;
    		  return false;
		  }

		  out.write(buffer);
		  out.flush();
		  
//		  int respone=in.read();
//		  Log.v("DCU_MSG", "Response: "+respone);
//		  
//		  if(respone != 1){
//			  Log.e("ConnectionError", "Error Signal returned from control unit.");
//    		  message=("Error Signal returned from control unit.");
//    		  state_v=ConnectionState.Error;
//    		  return false;
//		  }
    	} catch(NetworkOnMainThreadException e1){
			  
    	}  catch (Exception e) {
    		  Log.e("ConnectionError", "Failed to write command to dalek control unit."+e.getMessage());
    		  message=("Failed to write command to dalek control unit."+e.getMessage());
    		  state_v=ConnectionState.Error;
    		  return false;
          }
    	
    	  return true;
    }

	public synchronized void close_connection() {
		try {
			socket.close();
		} catch (IOException e) {
			Log.w("ConnectionError", "Failed to close socket."+e.getMessage());
  		 /// message=("Failed to close socket"+e.getMessage());
  		 // state_v=ConnectionState.Error;
  		  //e.printStackTrace();
		}
		
		live_connect=null;
		
	}

	public static void static_sendCommand(Activity activity, byte [] commands) {
		
		
		String log_str="Command Char: ";

		for(int i=0; i<commands.length; i++){
			log_str+=" "+(int)commands[i];
		}
		Log.d("DCU_MSG",log_str);
		
		boolean res=false;
		if(live_connect==null){
			return;
		}
		if(live_connect.state_v == ConnectionState.Connected){
			res= live_connect.sendCommand(commands);
		}
		
		
		if(!res){
			Intent intent = new Intent(activity, MainActivity.class);
			activity.startActivity(intent);
			
		}
		
	}
    

}
