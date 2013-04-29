
package com.example.dalekcontroller;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import com.example.dalekcontroller.R.color;

import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

public class ArduinoConnection extends Thread {
	enum state{ Connecting, Disconnected, Connected, Error, Uninitiated}
	state state_v=state.Uninitiated;
	private String s_address;
	private Socket socket;
	private OutputStream out = null;
	private BufferedReader in = null;
	private TextView button;
	
    public ArduinoConnection(String address, TextView but) {
    	s_address=address;
    	button=but;
    }

    @Override
    public void run() {
        try {
        	state_v=state.Connecting;
        	socket = new Socket(s_address, 23);
			out =socket.getOutputStream();
			in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			String s=in.readLine();
  		  	Log.v("ArduinoMSG", "Response: "+s);
 
  		  state_v=state.Connected;
        } catch (IOException e) {
            e.printStackTrace();
            try {
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            
            state_v=state.Error;
            return;
        }
        
        
    }
    public boolean sendCommand(char c, char op1, char op2){
    	
    	if(state_v==state.Connecting){
    		Log.e("ConnectionError", "SStill Connecting");

    		button.setText("Disconnected1");
  		  	button.setBackgroundColor(color.red);
    		return false;
    		
    	}
    	if(state_v==state.Uninitiated){
    		Log.e("ConnectionError", "Socket is Unitiated.");
    		button.setText("Disconnected2");
  		  	button.setBackgroundColor(color.red);
    		return false;
    	}
    	else if(state_v==state.Disconnected){
    		Log.e("ConnectionError", "Socket is  Disconnected.");
    		button.setText("Disconnected3");
  		  	button.setBackgroundColor(color.red);
    		return false;
    	}
    	button.setText("Connected");
		  	button.setBackgroundColor(color.green);
    	
    	  try {
    		  if(!socket.isConnected()){
        		  Log.e("ConnectionError", "Socket is not connected.");
        		  button.setText("Disconnected4");
        		  	button.setBackgroundColor(color.red);
    			  return false;
    		  }

    		  out.write(c); 
    		  out.write(op1); 
    		  out.write(op2); 
    		  out.flush();
    		  
    		  String s=in.readLine();
    		  Log.v("ArduinoMSG", "Response: "+s);
    	  } catch (Exception e) {
    		  Log.e("ConnectionError", "Failed to write command to arduino."+e.getMessage());
    		  button.setText("Disconnected5");
    		  	button.setBackgroundColor(color.red);
              return false;
          }
    	  return true;
    }
    

}
