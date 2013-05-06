
package com.example.dalekcontroller;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;


import android.os.NetworkOnMainThreadException;
import android.util.Log;

public class ArduinoConnection extends Thread {
	
	public static ArduinoConnection live_connect=null;
	
	
	ConnectionState state_v=ConnectionState.Uninitiated;
	public String message="No Error";
	
	
	private String s_address;
	private Socket socket;
	private OutputStream out = null;
	private BufferedReader in = null;
	
    public ArduinoConnection(String address) {
    	s_address=address;
    }

    @Override
    public void run() {
        try {
        	state_v=ConnectionState.Connecting;
        	socket = new Socket(s_address, 9000);
			out =socket.getOutputStream();
			in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			
			
			out.write(1);
			String s=in.readLine();
  		  	Log.v("ArduinoMSG", "Response: "+s);
 
  		  state_v=ConnectionState.Connected;
  		  message="Succesfully Connected";
  		
  		  live_connect=this;
  		  
        } catch (IOException e) {
            e.printStackTrace();
            try {
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            
            state_v=ConnectionState.Error;
            return;
        }
        
        
    }
    public boolean sendCommand(int [] commands){
    	
    	if(state_v==ConnectionState.Connecting){
    		Log.e("ConnectionError", "Attempting to command while still trying to connect");

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

		  for(int i=0; i<commands.length; i++){
			  out.write(commands[i]);
		  }
		  out.flush();
		  
		  int respone=in.read();
		  Log.v("ArduinoMSG", "Response: "+respone);
		  
		  if(respone != 1){
			  Log.e("ConnectionError", "Error Signal returned from control unit.");
    		  message=("Error Signal returned from control unit.");
    		  state_v=ConnectionState.Error;
    		  return false;
		  }
    	} catch(NetworkOnMainThreadException e1){
			  
    	}  catch (Exception e) {
    		  Log.e("ConnectionError", "Failed to write command to arduino."+e.getMessage());
    		  message=("Failed to write command to arduino."+e.getMessage());
    		  state_v=ConnectionState.Error;
    		  return false;
          }
    	
    	  return true;
    }
    

}
