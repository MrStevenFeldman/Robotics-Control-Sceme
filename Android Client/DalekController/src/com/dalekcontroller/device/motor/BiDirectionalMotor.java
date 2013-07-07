package com.dalekcontroller.device.motor;

import java.nio.ByteBuffer;

import com.example.dalekcontroller.DalekServerConnect;
import com.example.dalekcontroller.R;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
public class BiDirectionalMotor extends MotorDevice {
	private byte deviceID=-1; public static String deviceID_s="deviceID";
	private boolean enabled=false; public static String enabled_s="enabled";
	private int motorDirection=FORWARD;  public static String motorDirection_s="motorDirection";
	private float power_level; public static String power_level_s="power_level";
	

	//Info Objects
	int current_speed_tv;
	private TextView power_level_indicator_v;
	private Button enable_disable_button;
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// Save the current article selection in case we need to recreate the fragment
		outState.putInt(deviceID_s, deviceID);
		outState.putInt(motorDirection_s, motorDirection);
		outState.putFloat(power_level_s, power_level);
		outState.putBoolean(enabled_s, enabled);
	}
	
	@Override
	public void onStart() {
		super.onStart();

		// During startup, check if there are arguments passed to the fragment.
		// onStart is a good place to do this because the layout has already been
		// applied to the fragment at this point so we can safely call the method
		// below that sets the article text.
		Bundle args = getArguments();
		if (args != null) {
			// Set article based on argument passed in
			deviceID=args.getByte(deviceID_s);
			
		} else {
			throw new ClassCastException("Need Arguements for BiDirectional Motor!");

		}
	}

	 @Override
	 public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		 if (savedInstanceState != null) {
			 deviceID=savedInstanceState.getByte(deviceID_s);
				motorDirection=savedInstanceState.getInt(motorDirection_s);
				power_level=savedInstanceState.getFloat(power_level_s);
				enabled= savedInstanceState.getBoolean(enabled_s);
		 }
		 else{
			
		 }
			
		 return inflater.inflate(R.layout.layout_bimotor, container, false);
		
	}
	 
	 @Override
	public void onViewCreated(View view, Bundle savedInstanceState){
		 
		if (savedInstanceState != null) {
			deviceID=savedInstanceState.getByte(deviceID_s);
			motorDirection=savedInstanceState.getInt(motorDirection_s);
			power_level=savedInstanceState.getFloat(power_level_s);
			enabled= savedInstanceState.getBoolean(enabled_s);
		}
		else{
			
		}

		Button speedUp=(Button)view.findViewById(R.id.speedUpButton);
			
		speedUp.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view){
				speedUpFunc(view);
			}
		});
		Button speedDown=(Button)view.findViewById(R.id.speedDownButton);
		speedDown.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view){
				speedDownFunc(view);
			}
		});
		Button stopButton=(Button)view.findViewById(R.id.stopButton);
		stopButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view){
				stopFunc(view);
			}
		});
		
		Button reverse=(Button)view.findViewById(R.id.reverseButton);
		reverse.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view){
				directionToggle(view);
			}
		});
		
		
		enable_disable_button=(Button)view.findViewById(R.id.enable_button);
		if(enabled){
			enable_disable_button.setText("Disable");
		}
		else{
			enable_disable_button.setText("Enable");
		}
		enable_disable_button.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view){
				motorToggle(view);
			}
		});
			
		power_level_indicator_v=(TextView)getView().findViewById(R.id.power_level_indicator);
				
			
	}
	
    
	public void stopFunc(View view) { stopFunc();}
		public void stopFunc() {
    	power_level=0;
    	
    	updateMotor();
    }
    
    public void speedUpFunc(View view){
    	power_level= (power_level+DUTY_INCREMENT);
    	
    	if(power_level > MAX_MOTOR_DUTY) 
    		power_level=MAX_MOTOR_DUTY;
    	
    	
    	updateMotor();
    	
    	
    }
    
    public void speedDownFunc(View view){
    	power_level= (power_level-DUTY_INCREMENT);
    	
    	if(power_level <0) 
    		power_level=0;
    	
    	updateMotor();
    }
    
    public void updateMotor(){
    	Log.d("PDS","BiDirectional Motor "+deviceID+"  Power Level["+power_level+"]");

    	byte [] command = new byte [6];
    	ByteBuffer bos = ByteBuffer.wrap(command);
		bos.put(deviceID);
		bos.put(SPEED_COMMAND);
		bos.putFloat(power_level);
		
    	DalekServerConnect.static_sendCommand(getActivity(),command);
		
		power_level_indicator_v.setText("Current Speed: "+power_level);
    }
    
    
   
    public void motorToggle(View view) {
		power_level=0;
		updateMotor();
		
    	if(enabled){
    		byte[] command={deviceID,ENABLE_COMMAND,0};
    		DalekServerConnect.static_sendCommand(getActivity(),command);
    		
    		enable_disable_button.setText("Enable");
    	}
    	else{
    		byte[] command={deviceID,ENABLE_COMMAND,1};
    		DalekServerConnect.static_sendCommand(getActivity(),command);
    		
    		enable_disable_button.setText("Disable");
    	}
    	enabled=!enabled;
    }
    
   
    
    public void directionToggle(View view){
    	power_level=0;
		updateMotor();
		
    	if(motorDirection==FORWARD){
    		byte[] command={deviceID,DIRECTION_COMMAND,REVERSE};
			DalekServerConnect.static_sendCommand(getActivity(),command);
    		motorDirection=REVERSE;
    	}
    	else{
    		byte[] command={deviceID,DIRECTION_COMMAND,FORWARD};
			DalekServerConnect.static_sendCommand(getActivity(),command);
    		motorDirection=FORWARD;
    	}
    }

   
    
 
}
