package com.dalekcontroller.device.motor;


import com.example.dalekcontroller.DalekServerConnect;
import com.example.dalekcontroller.MainActivity;
import com.example.dalekcontroller.R;

import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
public class BiDirectionalMotor extends MotorDevice {
	private int deviceID=-1;
	private boolean enabled=false;
	private int motorDirection=FORWARD;
	private int power_level;
	
	//Button ID
	
	int stop_button;
	int speed_inc_button;
	int speed_dec_button;
	int reverse_button;
	int enable_button;
	
	//Info Objects
	int current_speed_tv;

	public void init(int did){
		deviceID=did;
		enabled=false;
		motorDirection=FORWARD;
	}
	public void init(int did, boolean enable){
		deviceID=did;
		enabled=enable;
		motorDirection=FORWARD;
	}
	public void init(int did, int md){
		deviceID=did;
		enabled=false;
		if(md<1)
			motorDirection=	REVERSE;
		else
			motorDirection= FORWARD; 
	}
	

	
	
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dcu_con=DalekServerConnect.live_connect;     
        setContentView(R.layout.activity_motor_controller);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_motor_controller, menu);
        return true;
    }
    
    
    public void stopFunc(View view) {
    	power_level=0;
    	
    	updateMotor();
    }
    
    public void speedUpFunc(View view){
    	power_level=power_level+DUTY_INCREMENT;
    	
    	if(power_level > MAX_MOTOR_DUTY) 
    		power_level=MAX_MOTOR_DUTY;
    	
    	
    	updateMotor();
    	
    	
    }
    
    public void speedDownFunc(View view){
    	power_level=power_level-DUTY_INCREMENT;
    	
    	if(power_level <0) 
    		power_level=0;
    		//sendCommand('U',left_duty,right_duty);
    	
    	updateMotor();
    }
    
    public void updateMotor(){
    	Log.d("PDS","BiDirectional Motor "+deviceID+"  Power Level["+power_level+"]");

    	//sendCommand('U',left_duty,right_duty);
    	int[] command={MOTOR_DEVICE,1,SPEED_COMMAND,1,power_level};
		sendCommand(command);
		
		TextView power_level_indicator_v=(TextView)findViewById(current_speed_tv);
    	power_level_indicator_v.setText("Current Speed: "+power_level);
    }
    
    public void disconnect_func(View view){
    	stopFunc(view);
    	dcu_con.close_connection();
    	dcu_con=null;
    	
    	Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		
    	
    }
    
   
    public void motorToggle(View view) {
		TextView enable_disable_button=(TextView)findViewById(enable_button);

		
    	power_level=0;
		updateMotor();
		
    	if(enabled){
    		int[] command={MOTOR_DEVICE,1,ENABLE_COMMAND,deviceID,0};
    		sendCommand(command);
    		
    		enable_disable_button.setText("Motors are Disabled, Click to Enable");
    	}
    	else{
    		int[] command={MOTOR_DEVICE,1,0,deviceID,1};
    		sendCommand(command);
    		
    		enable_disable_button.setText("Motors are Enabled, Click to Disable");
    	}
    	enabled=!enabled;
    }
    
   
    
    public void directionToggle(View view){
    	power_level=0;
		updateMotor();
		
    	if(motorDirection==FORWARD){
			int[] command={MOTOR_DEVICE,1,DIRECTION_COMMAND,1,REVERSE};
			sendCommand(command);
    		motorDirection=REVERSE;
    	}
    	else{
			int[] command={MOTOR_DEVICE,1,DIRECTION_COMMAND,1,FORWARD};
			sendCommand(command);
    		motorDirection=FORWARD;
    	}
    }

   
    
 
}
