package com.dalekcontroller.device;

import java.nio.ByteBuffer;

import com.dalekcontroller.gui.CircularSeekBar;
import com.dalekcontroller.gui.CircularSeekBar.OnSeekChangeListener;
import com.example.dalekcontroller.DalekServerConnect;
import com.example.dalekcontroller.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class Servo  extends Fragment{
	protected static final int SERVO_DEVICE=2;
	//Has an ID
	private byte id; public final static String id_s="id";
	private float currentAngle=0; public final static String currentAngle_s="currentAngle";
	private int maxAngle=90; public final static String maxAngle_s="maxAngle";
	private int minAngle=-90; public final static String minAngle_s="minAngle";
	
	private CircularSeekBar servoArch;
	
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// Save the current article selection in case we need to recreate the fragment
		outState.putInt(id_s, id);
		outState.putFloat(currentAngle_s, currentAngle);
		outState.putInt(maxAngle_s, maxAngle);
		outState.putInt(minAngle_s, minAngle);

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
			id=args.getByte(id_s);
			currentAngle=args.getFloat(currentAngle_s, 0);
			maxAngle=args.getInt(maxAngle_s, 90);
			minAngle=args.getInt(minAngle_s, -90);
			
		} 
		else {
			throw new ClassCastException("Need Arguements for Servo Motor Pair!");

		}
		
		servoArch=(CircularSeekBar) getView().findViewById(R.id.servoArchC);
		servoArch.setSeekBarChangeListener(new OnSeekChangeListener(){
			@Override
			public void onProgressChange(CircularSeekBar view, float newProgress) {
				updateServo();
			}

			
		});
		
		
	}

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			id=savedInstanceState.getByte(id_s);
			currentAngle=savedInstanceState.getFloat(currentAngle_s, (short)0);
			maxAngle=savedInstanceState.getInt(maxAngle_s, 90);
			minAngle=savedInstanceState.getInt(minAngle_s, -90);
		}
		else{
			
		}
			
		return inflater.inflate(R.layout.layout_servo, container, false);
		
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState){
		
		if (savedInstanceState != null) {
			id=savedInstanceState.getByte(id_s);
			currentAngle=savedInstanceState.getFloat(currentAngle_s, (short)0);
			maxAngle=savedInstanceState.getInt(maxAngle_s, 90);
			minAngle=savedInstanceState.getInt(minAngle_s, -90);
		}
		else{
			
		}
		
try{
	CircularSeekBar servoArch2=(CircularSeekBar) view.findViewById(R.id.servoArchC);
		servoArch2.setSeekBarChangeListener(new OnSeekChangeListener(){
			@Override
			public void onProgressChange(CircularSeekBar view, float newProgress) {
				updateServo();
			}

			
		});
		servoArch=servoArch2;
}catch(Exception e){
	e.printStackTrace();
}
		
		ImageButton centerButton=(ImageButton)view.findViewById(R.id.centerButton);
		centerButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view){	
				servoArch.updateAngle(0);
			}
		});
		
		ImageButton leftButton=(ImageButton)view.findViewById(R.id.leftButton);
		leftButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view){	
				servoArch.updateAngle(270);
			}
		});
		
		ImageButton rightButton=(ImageButton)view.findViewById(R.id.rightButton);
		rightButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view){	
				servoArch.updateAngle(90);
			}
		});
		
	}
	
	public void updateServo() {
		currentAngle=servoArch.getAngle();
		
		byte [] command = new byte [5];
    	ByteBuffer bos = ByteBuffer.wrap(command);
		bos.put(id);
		bos.putFloat(currentAngle);
		
		
		DalekServerConnect.static_sendCommand(getActivity(),command);

	}	
	
}
