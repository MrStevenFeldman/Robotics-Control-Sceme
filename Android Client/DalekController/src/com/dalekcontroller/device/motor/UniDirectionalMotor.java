package com.dalekcontroller.device.motor;


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
public class UniDirectionalMotor extends MotorDevice {
	private static final int NUM_DEVICES=1;
	private int deviceID=-1; public static String deviceID_s="deviceID";
	private boolean enabled=false; private static String enabled_s="enabled";
	private int power_level; private static String power_level_s="power_level";
	
	private TextView power_level_indicator_v;
	private Button enable_disable_button;
	
	//Info Objects
	int current_speed_tv;
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// Save the current article selection in case we need to recreate the fragment
		outState.putInt(deviceID_s, deviceID);
		outState.putInt(power_level_s, power_level);
		outState.putBoolean(enabled_s, enabled);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		Bundle args = getArguments();
		if (args != null) {
			// Set article based on argument passed in
			deviceID=args.getInt(deviceID_s);
			
		} else {
			throw new ClassCastException("Need Arguements for UniDirectional Motor!");

		}
	}

	 @Override
	 public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		 if (savedInstanceState != null) {
			 deviceID=savedInstanceState.getInt(deviceID_s);
			power_level=savedInstanceState.getInt(power_level_s);
			enabled= savedInstanceState.getBoolean(enabled_s);
		 }
		 else{
			
		 }
			
		 return inflater.inflate(R.layout.layout_unimotor, container, false);
		
	}
	 
	 @Override
	public void onViewCreated(View view, Bundle savedInstanceState){
		 
		if (savedInstanceState != null) {
			deviceID=savedInstanceState.getInt(deviceID_s);
			power_level=savedInstanceState.getInt(power_level_s);
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
    	power_level=power_level+DUTY_INCREMENT;
    	
    	if(power_level > MAX_MOTOR_DUTY) 
    		power_level=MAX_MOTOR_DUTY;
    	updateMotor();
    	
    	
    }
    
    public void speedDownFunc(View view){
    	power_level=power_level-DUTY_INCREMENT;
    	
    	if(power_level <0) 
    		power_level=0;
    	
    	updateMotor();
    }
    
    public void updateMotor(){
    	Log.d("PDS","BiDirectional Motor "+deviceID+"  Power Level["+power_level+"]");

    	//sendCommand('U',left_duty,right_duty);
    	int[] command={MOTOR_DEVICE,NUM_DEVICES,SPEED_COMMAND,deviceID,power_level};
    	DalekServerConnect.static_sendCommand(getActivity(),command);
    	
    	power_level_indicator_v.setText("Current Speed: "+power_level);
    }
    
   
    public void motorToggle(View view) {
		power_level=0;
		updateMotor();
		
    	if(enabled){
    		int[] command={MOTOR_DEVICE,NUM_DEVICES,ENABLE_COMMAND,deviceID,0};
    		DalekServerConnect.static_sendCommand(getActivity(),command);
    		
    		enable_disable_button.setText("Enable");
    	}
    	else{
    		int[] command={MOTOR_DEVICE,NUM_DEVICES,ENABLE_COMMAND,deviceID,1};
    		DalekServerConnect.static_sendCommand(getActivity(),command);
    		
    		enable_disable_button.setText("Disable");
    	}
    	enabled=!enabled;
    }
    
   
 
}
