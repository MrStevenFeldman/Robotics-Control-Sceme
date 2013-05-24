package com.dalekcontroller.device.motor;

import com.example.dalekcontroller.CircularSeekBar; 
import com.example.dalekcontroller.DalekServerConnect;
import com.example.dalekcontroller.MainActivity;
import com.example.dalekcontroller.R;
import com.example.dalekcontroller.CircularSeekBar.OnSeekChangeListener;
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
	
	public void init(int deviceA, int deviceB){
		deviceID_A=deviceA;
		deviceID_B=deviceB;
	}
	
	private boolean both_enabled=false;
	private int motorDirection=1;
	private int power_level=0;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dcu_con=DalekServerConnect.live_connect;
        
        setContentView(R.layout.activity_motor_controller);
       
    	com.example.dalekcontroller.CircularSeekBar dc_f=(com.example.dalekcontroller.CircularSeekBar) findViewById(R.id.forward_dir_control);
    	com.example.dalekcontroller.CircularSeekBar dc_r=(com.example.dalekcontroller.CircularSeekBar) findViewById(R.id.reverse_dir_control);
	
    	dc_f.setSeekBarChangeListener(new OnSeekChangeListener(){

			@Override
			public void onProgressChange(CircularSeekBar view, int newProgress) {
				if(motorDirection!=0){
					updateMotors(0);
					updateDir(1);
				}
			}
    		
    	});
    	dc_r.setSeekBarChangeListener(new OnSeekChangeListener(){

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

		
		com.example.dalekcontroller.CircularSeekBar dc=(com.example.dalekcontroller.CircularSeekBar) findViewById(R.id.forward_dir_control);
    	dc.setProgressPercent(0);
    	dc.setProgress(0);
    	dc.refreshDrawableState();
    	
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
		
    	com.example.dalekcontroller.CircularSeekBar dc=(com.example.dalekcontroller.CircularSeekBar) findViewById(R.id.forward_dir_control);
    	dc.setProgressPercent(0);
    	

    }
    
    public void leftRotateButton(View view){
    	updateDir(2);
    }
    public void rightRotateButton(View view){
    	updateDir(-2);
    }
    
    public void setCenterReverseFunc(View view){
    	updateReverseSeekBar(0);
    	
    	if(motorDirection!=0){
			updateDir(-1);
		}
    }
    public void setRightReverseFunc(View view){
    	updateReverseSeekBar( 270);
    }
    public void setLeftReverseFunc(View view){
    	updateReverseSeekBar(90);
    }
    
    public void setCenterForwardFunc(View view){
    	updateForwardSeekBar(0);
    	
    	if(motorDirection!=0){
			updateDir(1);
		}
    }
    public void setRightForwardFunc(View view){
    	updateForwardSeekBar(90);
    }
    public void setLeftForwardFunc(View view){
    	updateForwardSeekBar(270);
    }

    public void updateForwardSeekBar(int position){
    	com.example.dalekcontroller.CircularSeekBar dc=(com.example.dalekcontroller.CircularSeekBar) findViewById(R.id.forward_dir_control);
    	dc.updateAngle(position);
    }
    public void updateReverseSeekBar( int position){
    	com.example.dalekcontroller.CircularSeekBar dc=(com.example.dalekcontroller.CircularSeekBar) findViewById(R.id.reverse_dir_control);
    	dc.updateAngle(position);
    	
    }
    
    public void updateMotors(int pl){
    	power_level=pl;
    			
    	float power_dist, percent_to_left, percent_to_right;
    	
    	if(motorDirection==1){
        	com.example.dalekcontroller.CircularSeekBar dc=(com.example.dalekcontroller.CircularSeekBar) findViewById(R.id.forward_dir_control);
        	power_dist=dc.getProgressPercent();
    	}
    	else if(motorDirection==-1){
        	com.example.dalekcontroller.CircularSeekBar dc=(com.example.dalekcontroller.CircularSeekBar) findViewById(R.id.reverse_dir_control);
        	power_dist=dc.getProgressPercent();
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
    
    public void updateDir(int dir){
    	if(motorDirection==dir){
    		return;
    	}
    	else{
    		
    		motorDirection=0;
    		//Set power level to 0
    		int dirA;
    		int dirB;
    		if(dir==1){
    			updateReverseSeekBar(0);
    			dirA=FORWARD;
    			dirB=FORWARD;
    			//sendCommand('D','F','F');

    		}
    		else if(dir==-1){
    			updateForwardSeekBar(0);
    			dirA=REVERSE;
    			dirB=REVERSE;

    		}
    		else if(dir==2){
    			updateReverseSeekBar(0);
    			updateForwardSeekBar(0);
    			dirA=FORWARD;
    			dirB=REVERSE;
    		}
    		else if(dir==-2){
    			updateReverseSeekBar(0);
    			updateForwardSeekBar(0);
    			dirA=REVERSE;
    			dirB=FORWARD;
    		}
    		else{
    			updateReverseSeekBar(0);
    			dirA=FORWARD;
    			dirB=FORWARD;
    		}
    		
			int[] command={MOTOR_DEVICE,NUM_DEVICES,DIRECTION_COMMAND,deviceID_A,dirA,deviceID_B,dirB};
			sendCommand(command);
    		motorDirection=dir;
    		//Set both directions to  center
    		
    		com.example.dalekcontroller.CircularSeekBar dc=(com.example.dalekcontroller.CircularSeekBar) findViewById(R.id.forward_dir_control);
        	dc.setProgressPercent(0);
    	}
    }

}