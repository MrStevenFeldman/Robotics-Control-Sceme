package com.dalekcontroller.device.motor;

import com.dalekcontroller.gui.CircularSeekBar;
import com.dalekcontroller.gui.CircularSeekBar.OnSeekChangeListener;
import com.example.dalekcontroller.DalekServerConnect;
import com.example.dalekcontroller.MainActivity;
import com.example.dalekcontroller.R;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
public class BIDirectionalMotorPair extends MotorDevice {
	private final int NUM_DEVICES=2;
	private int deviceID_A=-1;
	private int deviceID_B=-1;
	
	//TODO: Generate a layout that includes all buttons
	//Then have that added dynamically
	
	
	public void init(int deviceA, int deviceB){
		deviceID_A=deviceA;
		deviceID_B=deviceB;
	}
	
	private boolean both_enabled=false;
	private int motorDirection=1;
	private int power_level=0;
	
	private com.dalekcontroller.gui.CircularSeekBar forwardArc;
	private com.dalekcontroller.gui.CircularSeekBar reverseArc;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_motor_controller);
        
        init(1,2);
        dcu_con=DalekServerConnect.live_connect;
        
      
        
        forwardArc=(com.dalekcontroller.gui.CircularSeekBar) findViewById(R.id.forward_dir_control);
        reverseArc=(com.dalekcontroller.gui.CircularSeekBar) findViewById(R.id.reverse_dir_control);
	
        forwardArc.setSeekBarChangeListener(new OnSeekChangeListener(){

			@Override
			public void onProgressChange(CircularSeekBar view, int newProgress) {
				if(motorDirection!=0){
					updateMotors(0);
					updateDir(1);
				}
			}
    		
    	});
    	reverseArc.setSeekBarChangeListener(new OnSeekChangeListener(){

			@Override
			public void onProgressChange(CircularSeekBar view, int newProgress) {
				if(motorDirection!=0){
					updateMotors(0);
					updateDir(-1);
					
				}			
			}
    		
    	});
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_motor_controller, menu);  
        return true;
    }
    
    
    public void stopFunc(View view) {
    	//sendCommand('U',(char)0,(char)0);
    	power_level=0;
    	int[] command={MOTOR_DEVICE,NUM_DEVICES,SPEED_COMMAND,deviceID_A,0,deviceID_B,0};
		sendCommand(command);

		
		forwardArc.setProgressPercent(0);
		reverseArc.setProgressPercent(0);;
		
		reverseArc.refreshDrawableState();
    	forwardArc.refreshDrawableState();
    	
    	TextView power_level_indicator_v=(TextView)findViewById(R.id.power_level_indicator);
    	power_level_indicator_v.setText("Current Speed: "+power_level);
    }
    
    public void speedUpFunc(View view){
    	int np=power_level+DUTY_INCREMENT;
    	
    	if(np > MAX_MOTOR_DUTY) 
    		np=MAX_MOTOR_DUTY;
    	updateMotors(np);
    }
    
    public void speedDownFunc(View view){
    	int np=power_level-DUTY_INCREMENT;  	
    	if(np <0) 
    		np=0;
    	updateMotors(np);
    }
    
    public void disconnect_func(View view){
    	stopFunc(view);
    	dcu_con.close_connection();
    	dcu_con=null;
    	
    	Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
    }
    
   
    public void motorToggle(View view) {
    	
    	//Insure Default State
    	forwardArc.setProgressPercent(0);
    	reverseArc.setProgressPercent(0);
    	updateMotors(0);
    	
    	if(both_enabled){
    		
    		int[] command={MOTOR_DEVICE,NUM_DEVICES,ENABLE_COMMAND,deviceID_A,0,deviceID_B,0};
    		sendCommand(command);
    		
    		TextView enable_disable_button=(TextView)findViewById(R.id.motorEnableButton);
    		enable_disable_button.setText("Motors are Disabled, Click to Enable");
    	}
    	else{
    		int[] command={MOTOR_DEVICE,NUM_DEVICES,ENABLE_COMMAND,deviceID_A,1,deviceID_B,1};
    		sendCommand(command);
    		TextView enable_disable_button=(TextView)findViewById(R.id.motorEnableButton);
    		enable_disable_button.setText("Motors are Enabled, Click to Disable");
    	}
    	both_enabled=!both_enabled;

    }
    
    public void leftRotateButton(View view){
    	updateDir(2);
    }
    public void rightRotateButton(View view){
    	updateDir(-2);
    }
    
    public void setCenterReverseFunc(View view){
    	reverseArc.updateAngle(0);
    	
    	if(motorDirection!=0){
			updateDir(-1);
		}
    }
    public void setRightReverseFunc(View view){
    	reverseArc.updateAngle(270);
    }
    public void setLeftReverseFunc(View view){
    	reverseArc.updateAngle(90);
    }
    
    public void setCenterForwardFunc(View view){
    	forwardArc.updateAngle(0);

    	if(motorDirection!=0){
			updateDir(1);
		}
    }
    public void setRightForwardFunc(View view){
    	forwardArc.updateAngle(90);

    }
    public void setLeftForwardFunc(View view){
    	forwardArc.updateAngle(270);
    }

    public synchronized void updateMotors(int pl){
    	power_level=pl;
    			
    	float power_dist, percent_to_left, percent_to_right;
    	
    	if(motorDirection==1){
        	power_dist=forwardArc.getProgressPercent();
    	}
    	else if(motorDirection==-1){
        	power_dist=reverseArc.getProgressPercent();
    	}
    	else{
    		power_dist= 0;
    	}
    	
    	if(power_dist==0){
    		percent_to_left=(float) 1.0;
    		percent_to_right=(float) 1.0;
    	}
    	else{
    		power_dist=power_dist-50;
	    	if( power_dist < 0 ){
	    		power_dist=(float) (-1.0+(power_dist/-25.0));
	    		
	    		percent_to_right=(float) 1.0;
	    		percent_to_left =power_dist;
	    	}
	    	else{
	    		power_dist=(float) (-1.0+(power_dist/25.0));
	    		
	    		percent_to_right=power_dist;
	    		percent_to_left=(float) 1.0;
	    	}
    	}
   	
    	Log.d("PDS","Power Dist: ["+power_dist+"]  Left % ["+percent_to_left+"] Right % ["+percent_to_right+ "] Prower Levels["+power_level+	"]");

    	//lets say power dist
    	char left_duty=(char) (power_level*percent_to_left);
    	char right_duty=(char) (power_level*percent_to_right);
    	
    	//sendCommand('U',left_duty,right_duty);
    	int[] command={MOTOR_DEVICE,NUM_DEVICES, DIRECTION_COMMAND,deviceID_A,left_duty,deviceID_B,right_duty};
		sendCommand(command);
		
    	TextView power_level_indicator_v=(TextView)findViewById(R.id.power_level_indicator);
    	power_level_indicator_v.setText("Current Speed: "+power_level);
    }
    
    public synchronized void updateDir(int dir){
    	if(motorDirection==dir){
    		return;
    	}
    	else{
    		
    		motorDirection=0;
    		//Set power level to 0
    		int dirA;
    		int dirB;
    		if(dir==1){
    			dirA=dirB=FORWARD;
    		}
    		else if(dir==-1){
    			dirA=dirB=REVERSE;
    		}
    		else if(dir==2){
    			dirA=FORWARD;
    			dirB=REVERSE;
    		}
    		else if(dir==-2){
    			dirA=REVERSE;
    			dirB=FORWARD;
    		}
    		else{
    			dirA=dirB=FORWARD;
    		}
    		reverseArc.setProgressPercent(0);
    		forwardArc.setProgressPercent(0);
    		
			int[] command={MOTOR_DEVICE,NUM_DEVICES,DIRECTION_COMMAND,deviceID_A,dirA,deviceID_B,dirB};
			sendCommand(command);
    		motorDirection=dir;
    	}
    }

}
