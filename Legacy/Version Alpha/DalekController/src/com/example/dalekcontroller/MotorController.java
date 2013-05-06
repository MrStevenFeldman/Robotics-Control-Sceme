package com.example.dalekcontroller;

import com.example.dalekcontroller.CircularSeekBar.OnSeekChangeListener;
import com.example.dalekcontroller.VerticalSeekBar.OnSeekBarChangeListener;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MotorController extends Activity {

	ArduinoConnection arduinoCon;
	
	int motorDirection=1;
	final int MAX_MOTOR_DUTY=255;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_motor_controller);
       
    	com.example.dalekcontroller.CircularSeekBar dc_f=(com.example.dalekcontroller.CircularSeekBar) findViewById(R.id.forward_dir_control);
    	com.example.dalekcontroller.CircularSeekBar dc_r=(com.example.dalekcontroller.CircularSeekBar) findViewById(R.id.reverse_dir_control);
		com.example.dalekcontroller.VerticalSeekBar vsb=(com.example.dalekcontroller.VerticalSeekBar) findViewById(R.id.powerBar);

    	vsb.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){

			@Override
			public void onProgressChanged(VerticalSeekBar seekBar, int progress, boolean fromUser) {
				update_motors();
			}

			@Override
			public void onStartTrackingTouch(VerticalSeekBar seekBar) {}

			@Override
			public void onStopTrackingTouch(VerticalSeekBar seekBar) {}
    		
    	});
    	
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
    	sendCommand('U',(char)0,(char)0);

    }
    public void connectToggle(View view) {
    	EditText et=(EditText) findViewById(R.id.ipTextArea);
    	String ip=et.getText().toString(); 
    	
    	TextView b=(TextView)findViewById(R.id.statusArea);
    	arduinoCon=new ArduinoConnection(ip,b);
    	arduinoCon.start();
    	return;
	
    }
    
    public void motorToggle(View view) {
    	sendCommand('E','0','0');
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
    public void centerDomeFunc(View view){
    	com.example.dalekcontroller.CircularSeekBar dc=(com.example.dalekcontroller.CircularSeekBar) findViewById(R.id.dome_control);
    	dc.updateAngle(0);
    }
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
   
    	
    	
		com.example.dalekcontroller.VerticalSeekBar vsb=(com.example.dalekcontroller.VerticalSeekBar) findViewById(R.id.powerBar);
    	float power_level=vsb.getProgress();
    	
    	Log.d("PDS","Power Dist: ["+power_dist+"]  Left % ["+percent_to_left+"] Right % ["+percent_to_right+ "] Prower Levels["+power_level+	"]");

    	//lets say power dist
    	char left_duty=(char) (power_level*percent_to_left);
    	char right_duty=(char) (power_level*percent_to_right);
    	
    	sendCommand('U',left_duty,right_duty);
    }
    
    public void updateDir(int dir){
    	if(motorDirection==dir){
    		return;
    	}
    	else{
    		
    		com.example.dalekcontroller.VerticalSeekBar vsb=(com.example.dalekcontroller.VerticalSeekBar) findViewById(R.id.powerBar);
    		vsb.setProgress(0);
    		
    		motorDirection=0;
    		//Set power level to 0
    		if(dir==1){
    			updateReverseSeekBar(0);
    			sendCommand('D','F','F');
    		}
    		else if(dir==-1){
    			updateForwardSeekBar(0);
    			sendCommand('D','R','R');

    		}
    		else if(dir==2){
    			updateReverseSeekBar(0);
    			updateForwardSeekBar(0);
    			sendCommand('D','F','R');
    		}
    		else if(dir==-2){
    			updateReverseSeekBar(0);
    			updateForwardSeekBar(0);
    			sendCommand('D','R','F');
    		}
    		motorDirection=dir;
    		//Set both directions to  center
    		
    	}
    }

    /**This function sends commands to the arduino
     * 'U' updates the target duty level on the motors op1 is the left motor and op 2 is the right motor
     * 'R' Tells the arduino to stop and reverse moto directions op1 and op 2 are zero
     * @param c
     * @param op1
     * @param op2
     */
	private void sendCommand(char c, char op1, char op2) {
		
		
		Log.d("ArduinoMSG", "Command Char: "+c+" Op1: ["+op1+"] Op2: ["+op2+"] MotorDirection["+motorDirection+"]");
		if(arduinoCon!=null)
			arduinoCon.sendCommand(c, op1, op2);
	}

    
 
}
