package com.dalekcontroller.device.motor;

import java.nio.ByteBuffer;

import com.dalekcontroller.gui.DotInCircle;
import com.dalekcontroller.gui.DotInCircle.DotChangeListener;
import com.example.dalekcontroller.DalekServerConnect;
import com.example.dalekcontroller.R;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class ZPTMotorPair extends MotorDevice {

	public ZPTMotorPair(){};
	
	
	

	private byte deviceID=-1; 
	public final static String deviceID_s="deviceID";
	private boolean both_enabled=false; public final static String both_enabled_s="both_enabled";
	
	private short power_levelr=0; public final static String power_levelr_s="power_levelr";
	private short power_levell=0; public final static String power_levell_s="power_levell";

	
	private TextView power_level_indicator_right, power_level_indicator_left;
	private Button enable_disable_button;
	private DotInCircle joystick;
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// Save the current article selection in case we need to recreate the fragment
		outState.putByte(deviceID_s, deviceID);
		outState.putInt(power_levelr_s, power_levelr);
		outState.putInt(power_levell_s, power_levell);

		outState.putBoolean(both_enabled_s, both_enabled);
	}
	
	@Override
	public void onStart() {
		super.onStart();		
		
		Bundle args = getArguments();
		if (args != null) {
			// Set article based on argument passed in
			deviceID=args.getByte(deviceID_s);
						
		} 
		else {
			throw new ClassCastException("Need Arguements for ZPT Motor Pair!");

		}
		
	}

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			deviceID=savedInstanceState.getByte(deviceID_s );
			power_levell=savedInstanceState.getShort(power_levell_s);
			power_levelr=savedInstanceState.getShort(power_levelr_s);
			both_enabled= savedInstanceState.getBoolean(both_enabled_s);
			
		}
		else{
			
		}
			
		return inflater.inflate(R.layout.layout_zptmotorpair, container, false);
		
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState){
		
		
		enable_disable_button=(Button)view.findViewById(R.id.enable_button);
		if(both_enabled){
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
		

		joystick = (DotInCircle)getView().findViewById(R.id.joystick);
		joystick.setDotChangeListener(new DotChangeListener(){
			@Override
			public void onChange() {
				updateMotors();
				
			}
			
		});
		
	
		
		power_level_indicator_left=(TextView)getView().findViewById(R.id.power_level_indicator_left);
		power_level_indicator_right=(TextView)getView().findViewById(R.id.power_level_indicator_right);

		
	}
	
	public void stopFunc(View view) { stopFunc();}
	
	public void stopFunc() {
		//sendCommand('U',(char)0,(char)0);
		power_levelr=0;
		power_levell=0;
		
		byte [] command = new byte [10];
		
		ByteBuffer bos = ByteBuffer.wrap(command);
		bos.put(deviceID);
		bos.put(SPEED_COMMAND);
		bos.putFloat(0);
		bos.putFloat(0);
		
		Log.i("ZPT", "STOP FUNCTION");
		DalekServerConnect.static_sendCommand(getActivity(),command);

		power_level_indicator_left.setText("LM: "+power_levell);
		power_level_indicator_right.setText("RM: "+power_levelr);

	}
	
	

   
	public void motorToggle(View view) {
		
		//Insure Default State
		stopFunc();
		
		
		if(both_enabled){
			
			byte[] command={deviceID,ENABLE_COMMAND,0};
			DalekServerConnect.static_sendCommand(getActivity(),command);
			enable_disable_button.setText("Enable");
		}
		else{
			byte[] command={deviceID,ENABLE_COMMAND,1};
			DalekServerConnect.static_sendCommand(getActivity(),command);
			
			enable_disable_button.setText("Disable");
		}
		joystick.resetDot();

		both_enabled=!both_enabled;
		
		Log.i("ZPT", "ENABLE/DISABLE FUNCTION");
	}
	
	
	public synchronized void updateMotors(){
		
		double angle=joystick.angle;
		double power_level=(joystick.distance/joystick.outerRadius)*MAX_MOTOR_DUTY;
		
		double right_dist;
		double left_dist;
		if(angle <= 90){
		
			left_dist= +1;
			if(angle<45.0){
				right_dist= -1*((45.0-angle)/45.0);
			}
			else{
				right_dist= 1*( (angle-45.0)/45.0);
			}
		}
		else if(angle <= 180){
			angle = angle - 90;
			right_dist= +1;
			if(angle<45.0){
				left_dist= 1*((45.0-angle)/45.0);
			}
			else{
				left_dist= -1*( (angle-45.0)/45.0);
			}
		}
		else if(angle <= 270){
			angle = angle - 180;
			left_dist= -1;
			if(angle<45.0){
				right_dist= 1*((45.0-angle)/45.0);
			}
			else{
				right_dist= -1*( (angle-45.0)/45.0);
			}
		}
		else{ // if(angle <= 360){
			angle = angle - 270;
			right_dist= -1;
			if(angle<45.0){
				left_dist= -1 *((45.0-angle)/45.0);
			}
			else{
				left_dist= 1*( (angle-45.0)/45.0);
			}
		}
		
		
		//Need to Convert It to X/ 1024
		float left_duty= (float) (power_level*left_dist);
		float right_duty=(float) (power_level*right_dist);
		String s= String.format("Update Speed: Angle: %.2f, Power: %.2f, LD: %.2f RD: %.2f ", joystick.angle, power_level, left_duty, right_duty);
		Log.i("ZPT", s);
		byte [] command = new byte [10];
		
		ByteBuffer bos = ByteBuffer.wrap(command);
		bos.put(deviceID);
		bos.put(SPEED_COMMAND);
		bos.putFloat(left_duty);
		bos.putFloat(right_duty);
		DalekServerConnect.static_sendCommand(getActivity(),command);

		power_level_indicator_left.setText("LM: "+left_duty);
		//power_level_indicator_left.invalidate();
		power_level_indicator_right.setText("RM: "+right_duty);
		//power_level_indicator_right.invalidate();
	}
	

}
