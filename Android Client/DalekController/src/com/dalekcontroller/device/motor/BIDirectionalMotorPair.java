package com.dalekcontroller.device.motor;

import com.dalekcontroller.gui.CircularSeekBar;
import com.dalekcontroller.gui.CircularSeekBar.OnSeekChangeListener;
import com.example.dalekcontroller.DalekServerConnect;
import com.example.dalekcontroller.R;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class BIDirectionalMotorPair extends MotorDevice {
	
	public BIDirectionalMotorPair(){}
	
	private final int NUM_DEVICES=2;
	private int deviceID_A=-1; public final static String deviceID_A_s="deviceID_A";
	private int deviceID_B=-1; public final static String deviceID_B_s="deviceID_B";
	private boolean both_enabled=false; public final static String both_enabled_s="both_enabled";
	private int motorDirection=1; public final static String motorDirection_s="motorDirection";
	private int power_level=0; public final static String power_level_s="power_level";
	
	private com.dalekcontroller.gui.CircularSeekBar forwardArc;
	private com.dalekcontroller.gui.CircularSeekBar reverseArc;
	private TextView power_level_indicator_v;
	private Button enable_disable_button;
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// Save the current article selection in case we need to recreate the fragment
		outState.putInt(deviceID_B_s, deviceID_B);
		outState.putInt(deviceID_A_s, deviceID_A);
		outState.putInt(motorDirection_s, motorDirection);
		outState.putInt(power_level_s, power_level);
		outState.putBoolean(both_enabled_s, both_enabled);
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
			deviceID_A=args.getInt(deviceID_A_s);
			deviceID_B=args.getInt(deviceID_B_s);
			
		} 
		else {
			throw new ClassCastException("Need Arguements for BiDirectional Motor Pair!");

		}
		
		forwardArc=(com.dalekcontroller.gui.CircularSeekBar) getView().findViewById(R.id.forward_dir_control);
		reverseArc=(com.dalekcontroller.gui.CircularSeekBar) getView().findViewById(R.id.reverse_dir_control);
	
		forwardArc.setSeekBarChangeListener(new OnSeekChangeListener(){ @Override
			public void onProgressChange(CircularSeekBar view, int newProgress) {
				updateDir(1); updateMotors();
			}
			
		});
		reverseArc.setSeekBarChangeListener(new OnSeekChangeListener(){
			@Override
			public void onProgressChange(CircularSeekBar view, int newProgress) {
				updateDir(-1); updateMotors(); 		
			}
			
		});
	}

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			deviceID_B=savedInstanceState.getInt(deviceID_B_s );
			deviceID_A=savedInstanceState.getInt(deviceID_A_s);
			motorDirection=savedInstanceState.getInt(motorDirection_s);
			power_level=savedInstanceState.getInt(power_level_s);
			both_enabled= savedInstanceState.getBoolean(both_enabled_s);
		}
		else{
			
		}
			
		return inflater.inflate(R.layout.layout_bimotorpair, container, false);
		
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState){
		forwardArc=(com.dalekcontroller.gui.CircularSeekBar) view.findViewById(R.id.forward_dir_control);
		reverseArc=(com.dalekcontroller.gui.CircularSeekBar) view.findViewById(R.id.reverse_dir_control);
		forwardArc.setSeekBarChangeListener(new OnSeekChangeListener(){
			@Override
			public void onProgressChange(CircularSeekBar view, int newProgress) {
				updateDir(1);		updateMotors();
			}	
		});
		reverseArc.setSeekBarChangeListener(new OnSeekChangeListener(){
			@Override
			public void onProgressChange(CircularSeekBar view, int newProgress) {
				updateDir(-1);		updateMotors();
			}
		});
			
		
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
		ImageButton forCenter=(ImageButton)view.findViewById(R.id.centerForwardButton);
		forCenter.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view){
				updateDir(1);		
				forwardArc.updateAngle(0);
			}
		});
		ImageButton forLeft=(ImageButton)view.findViewById(R.id.leftForwardButton);
		forLeft.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view){
				updateDir(1);		
				forwardArc.updateAngle(270);
			}
		});
		ImageButton forRight=(ImageButton)view.findViewById(R.id.rightForwardButton);
		forRight.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view){
				updateDir(1);		
				forwardArc.updateAngle(90);
			}
		});
		ImageButton revCenter=(ImageButton)view.findViewById(R.id.centerReverseButton);
		revCenter.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view){
				updateDir(-1);		
				reverseArc.updateAngle(0);
			}
		});
		ImageButton revLeft=(ImageButton)view.findViewById(R.id.leftReverseButton);
		revLeft.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view){
				updateDir(-1);		
				reverseArc.updateAngle(90);
			}
		});
		ImageButton revRight=(ImageButton)view.findViewById(R.id.rightReverseButton);
		revRight.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view){
				updateDir(-1);		
				reverseArc.updateAngle(270);
			}
		});
		Button rotateLeft=(Button)view.findViewById(R.id.leftRotateButton);
		rotateLeft.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view){
				updateDir(2);
			}
		});
		Button rotateRight=(Button)view.findViewById(R.id.rightRotateButton);
		rotateRight.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view){
				updateDir(-2);
			}
		});
			
		
		power_level_indicator_v=(TextView)getView().findViewById(R.id.power_level_indicator);
		
	}
	
	public void stopFunc(View view) { stopFunc();}
	
	public void stopFunc() {
		//sendCommand('U',(char)0,(char)0);
		power_level=0;
		int[] command={MOTOR_DEVICE,NUM_DEVICES,SPEED_COMMAND,deviceID_A,0,deviceID_B,0};
		DalekServerConnect.static_sendCommand(getActivity(),command);
		
		forwardArc.setProgressPercent(0);
		reverseArc.setProgressPercent(0);;
		
		reverseArc.refreshDrawableState();
		forwardArc.refreshDrawableState();
		
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
	

   
	public void motorToggle(View view) {
		
		//Insure Default State
		forwardArc.setProgressPercent(0);
		reverseArc.setProgressPercent(0);
		updateMotors(0);
		
		if(both_enabled){
			
			int[] command={MOTOR_DEVICE,NUM_DEVICES,ENABLE_COMMAND,deviceID_A,0,deviceID_B,0};
			DalekServerConnect.static_sendCommand(getActivity(),command);
			
			enable_disable_button.setText("Enable");
		}
		else{
			int[] command={MOTOR_DEVICE,NUM_DEVICES,ENABLE_COMMAND,deviceID_A,1,deviceID_B,1};
			DalekServerConnect.static_sendCommand(getActivity(),command);
			enable_disable_button.setText("Disable");
		}
		both_enabled=!both_enabled;

	}
	
   

	public synchronized void updateMotors(int pl){
		power_level=pl;
		updateMotors();
	}
	public synchronized void updateMotors(){
			
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
		DalekServerConnect.static_sendCommand(getActivity(),command);
		
		power_level_indicator_v.setText("Current Speed: "+power_level);
	}
	
	public synchronized void updateDir(int dir){
		if(motorDirection==dir){
			return;
		}
		else{
			motorDirection=dir;
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
			reverseArc.updateAngle(0);
			forwardArc.updateAngle(0);
			
			int[] command={MOTOR_DEVICE,NUM_DEVICES,DIRECTION_COMMAND,deviceID_A,dirA,deviceID_B,dirB};
			DalekServerConnect.static_sendCommand(getActivity(),command);
		}
	}

}
