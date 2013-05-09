package com.example.dalekcontroller;

import com.example.dalekcontroller.CircularSeekBar.OnSeekChangeListener;
import com.example.dalekcontroller.VerticalSeekBar.OnSeekBarChangeListener;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
public class MotorController extends Activity {

	DalekServerConnect dcu_con;
	
	boolean both_enabled=false;
	int motorDirection=1;
	final int MAX_MOTOR_DUTY=255;
	final int DUTY_INCREMENT=5;
	private int power_level;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dcu_con=DalekServerConnect.live_connect;
        
        setContentView(R.layout.activity_motor_controller);
       
    	com.example.dalekcontroller.CircularSeekBar dc_f=(com.example.dalekcontroller.CircularSeekBar) findViewById(R.id.forward_dir_control);
    	com.example.dalekcontroller.CircularSeekBar dc_r=(com.example.dalekcontroller.CircularSeekBar) findViewById(R.id.reverse_dir_control);
	//	com.example.dalekcontroller.VerticalSeekBar vsb=(com.example.dalekcontroller.VerticalSeekBar) findViewById(R.id.powerBar);

//    	vsb.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
//
//			@Override
//			public void onProgressChanged(VerticalSeekBar seekBar, int progress, boolean fromUser) {
//				update_motors();
//			}
//
//			@Override
//			public void onStartTrackingTouch(VerticalSeekBar seekBar) {}
//
//			@Override
//			public void onStopTrackingTouch(VerticalSeekBar seekBar) {}
//    		
//    	});
    	
    	dc_f.setSeekBarChangeListener(new OnSeekChangeListener(){

			@Override
			public void onProgressChange(CircularSeekBar view, int newProgress) {
				if(motorDirection!=0){
					updateDir(1);
					update_motors();
				}
			}
    		
    	});
    	dc_r.setSeekBarChangeListener(new OnSeekChangeListener(){

			@Override
			public void onProgressChange(CircularSeekBar view, int newProgress) {
				if(motorDirection!=0){
					updateDir(-1);
					update_motors();
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
    	int[] command={1,2,2,1,0,2,0};
		sendCommand(command);

		
		com.example.dalekcontroller.CircularSeekBar dc=(com.example.dalekcontroller.CircularSeekBar) findViewById(R.id.forward_dir_control);
    	dc.setProgressPercent(0);
    	dc.setProgress(0);
    	dc.refreshDrawableState();
    	
    	TextView power_level_indicator_v=(TextView)findViewById(R.id.power_level_indicator);
    	power_level_indicator_v.setText("Current Speed: "+power_level);
    }
    
    public void speedUpFunc(View view){
    	power_level=power_level+DUTY_INCREMENT;
    	
    	if(power_level > MAX_MOTOR_DUTY) power_level=MAX_MOTOR_DUTY;
    	update_motors();
    	TextView power_level_indicator_v=(TextView)findViewById(R.id.power_level_indicator);
    	power_level_indicator_v.setText("Current Speed: "+power_level);
    	
    }
    
    public void speedDownFunc(View view){
    	power_level=power_level-DUTY_INCREMENT;
    	
    	if(power_level <0) power_level=0;
    	update_motors();
    	
    	TextView power_level_indicator_v=(TextView)findViewById(R.id.power_level_indicator);
    	power_level_indicator_v.setText("Current Speed: "+power_level);

    }
    
    public void disconnect_func(View view){
    	stopFunc(view);
    	dcu_con.close_connection();
    	dcu_con=null;
    	
    	Button but1=(Button)findViewById(R.id.connectToHost_button);
    	but1.setEnabled(true);
    	
    	but1=(Button)findViewById(R.id.man_control_button);
    	but1.setEnabled(false);
    	
    	Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		
    	
    }
    
   
    public void motorToggle(View view) {
    	
    //	sendCommand('E','0','0');
    	//Enables
    	
    	
    	if(both_enabled){
    		
    		int[] command={1,2,0,1,0,2,0};
    		sendCommand(command);
    		
    		TextView enable_disable_button=(TextView)findViewById(R.id.motorEnableButton);
    		enable_disable_button.setText("Motors are Disabled, Click to Enable");
    	}
    	else{
    		int[] command={1,2,0,1,1,2,1};
    		sendCommand(command);
    		TextView enable_disable_button=(TextView)findViewById(R.id.motorEnableButton);
    		enable_disable_button.setText("Motors are Enabled, Click to Disable");
    	}
    	both_enabled=!both_enabled;
		
    	com.example.dalekcontroller.CircularSeekBar dc=(com.example.dalekcontroller.CircularSeekBar) findViewById(R.id.forward_dir_control);
    	dc.setProgressPercent(0);
    	
    	power_level=0;
    	TextView power_level_indicator_v=(TextView)findViewById(R.id.power_level_indicator);
    	power_level_indicator_v.setText("Current Speed: "+power_level);

    	
    	

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
//    public void centerDomeFunc(View view){
//    	com.example.dalekcontroller.CircularSeekBar dc=(com.example.dalekcontroller.CircularSeekBar) findViewById(R.id.dome_control);
//    	dc.updateAngle(0);
//    }
    public void updateForwardSeekBar(int position){
    	com.example.dalekcontroller.CircularSeekBar dc=(com.example.dalekcontroller.CircularSeekBar) findViewById(R.id.forward_dir_control);
    	dc.updateAngle(position);
    }
    public void updateReverseSeekBar( int position){
    	com.example.dalekcontroller.CircularSeekBar dc=(com.example.dalekcontroller.CircularSeekBar) findViewById(R.id.reverse_dir_control);
    	dc.updateAngle(position);
    	
    }
    public void update_motors(){
    	float power_dist, percent_to_left, percent_to_right;
    	
    	if(motorDirection==1){
        	com.example.dalekcontroller.CircularSeekBar dc=(com.example.dalekcontroller.CircularSeekBar) findViewById(R.id.forward_dir_control);
        	power_dist=dc.getProgressPercent();
    	}
    	else if(motorDirection==-1){
        	com.example.dalekcontroller.CircularSeekBar dc=(com.example.dalekcontroller.CircularSeekBar) findViewById(R.id.reverse_dir_control);
        	power_dist=dc.getProgressPercent();
    	}
    	else if(motorDirection!=-2 || motorDirection!=2){
    		power_dist=0;
    	}
    	else{
    		power_dist=0;
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
   
    	
    	
	//	com.example.dalekcontroller.VerticalSeekBar vsb=(com.example.dalekcontroller.VerticalSeekBar) findViewById(R.id.powerBar);
    //	float power_level=vsb.getProgress();
    	
    	Log.d("PDS","Power Dist: ["+power_dist+"]  Left % ["+percent_to_left+"] Right % ["+percent_to_right+ "] Prower Levels["+power_level+	"]");

    	//lets say power dist
    	char left_duty=(char) (power_level*percent_to_left);
    	char right_duty=(char) (power_level*percent_to_right);
    	
    	//sendCommand('U',left_duty,right_duty);
    	int[] command={1,2,2,1,left_duty,2,right_duty};
		sendCommand(command);
    }
    
    public void updateDir(int dir){
    	if(motorDirection==dir){
    		return;
    	}
    	else{
    		
//    		com.example.dalekcontroller.VerticalSeekBar vsb=(com.example.dalekcontroller.VerticalSeekBar) findViewById(R.id.powerBar);
//    		vsb.setProgress(0);
    		power_level=0;
    		
    		motorDirection=0;
    		//Set power level to 0
    		if(dir==1){
    			updateReverseSeekBar(0);
    			//sendCommand('D','F','F');
    			int[] command={1,2,1,1,1,2,1};
    			sendCommand(command);
    		}
    		else if(dir==-1){
    			updateForwardSeekBar(0);
    			//sendCommand('D','R','R');
    			int[] command={1,2,1,1,0,2,0};
    			sendCommand(command);

    		}
    		else if(dir==2){
    			updateReverseSeekBar(0);
    			updateForwardSeekBar(0);
//    			sendCommand('D','F','R');
    			int[] command={1,2,1,1,1,2,0};
    			sendCommand(command);
    		}
    		else if(dir==-2){
    			updateReverseSeekBar(0);
    			updateForwardSeekBar(0);
    			//sendCommand('D','R','F');
    			int[] command={1,2,1,1,0,2,1};
    			sendCommand(command);
    		}
    		motorDirection=dir;
    		//Set both directions to  center
    		
    		com.example.dalekcontroller.CircularSeekBar dc=(com.example.dalekcontroller.CircularSeekBar) findViewById(R.id.forward_dir_control);
        	dc.setProgressPercent(0);
    	}
    }

    /**This function sends commands to the dalek control unit **/

	private void sendCommand(int [] commands) {
		
		
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
