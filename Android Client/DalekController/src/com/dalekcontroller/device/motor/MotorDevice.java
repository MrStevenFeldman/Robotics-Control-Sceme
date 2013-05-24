package com.dalekcontroller.device.motor;

import com.example.dalekcontroller.ConnectionState;
import com.example.dalekcontroller.DalekServerConnect;
import com.example.dalekcontroller.MainActivity;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

public abstract class MotorDevice extends Activity {
	static DalekServerConnect dcu_con;
	
	static int MOTOR_DEVICE=1;
	final int MAX_MOTOR_DUTY=255;
	final int DUTY_INCREMENT=5;
	final int FORWARD=1;
	final int REVERSE=0;
	final int SPEED_COMMAND=2;
	final int ENABLE_COMMAND=0;
	final int DIRECTION_COMMAND=1;
	
	 /**This function sends commands to the dalek control unit **/

	public void sendCommand(int [] commands) {
			
			
			String log_str="Command Char: ";
			
		
			
			for(int i=0; i<commands.length; i++){
				log_str+=" "+(int)commands[i];
			}
			Log.d("DCU_MSG",log_str);
			
			if(dcu_con==null || (dcu_con.state_v != ConnectionState.Connected) ){
				Intent intent = new Intent(this, MainActivity.class);
				startActivity(intent);
				
			}
			boolean res=dcu_con.sendCommand(commands);
			
			if(!res){
				Intent intent = new Intent(this, MainActivity.class);
				startActivity(intent);
				
			}
		}
	
}