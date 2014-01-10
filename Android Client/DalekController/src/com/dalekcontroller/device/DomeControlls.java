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
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class DomeControlls  extends Fragment{
	
	//Dome Rotation
	private byte dome_id; public final static String dome_id_s="dome_id";
	private float currentSteps=0; public final static String currentSteps_s="currentSteps";
	private int maxSteps=90; public final static String maxSteps_s="maxSteps";
	private int minSteps=-90; public final static String minSteps_s="minSteps";
	
	//Eye Stalk Servo
	private byte stalk_id; public final static String stalk_id_s="stalk_id";
	private float currentAngle=0; public final static String currentAngle_s="currentAngle";
	private int maxAngle=90; public final static String maxAngle_s="maxAngle";
	private int minAngle=-90; public final static String minAngle_s="minAngle";
	
	//Eye Iris
	private byte iris_id; public final static String iris_id_s="iris_id";
	
	//Eye Led
	private byte led_id; public final static String led_id_s="led_id";
	
	CircularSeekBar eyeStalk;
	CircularSeekBar domeSeekBar;
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		//Dome
		outState.putByte(dome_id_s, dome_id);
		outState.putFloat(currentSteps_s, currentSteps);
		outState.putInt(maxSteps_s, maxSteps);
		outState.putInt(minSteps_s, minSteps);
		
		//Eye Stalk Servo	
		outState.putByte(stalk_id_s, stalk_id);
		outState.putFloat(currentAngle_s, currentAngle);
		outState.putInt(maxAngle_s, maxAngle);
		outState.putInt(minAngle_s, minAngle);
		
		
		//Eye Iris
		outState.putByte(iris_id_s, iris_id);

		//Eye Led
		outState.putByte(led_id_s, led_id);

		


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
			
			//Dome
			dome_id=args.getByte(dome_id_s);
			currentSteps=0;
			maxSteps=args.getInt(maxSteps_s,-1);
			minSteps=args.getInt(minSteps_s,-1);
			
			//Eye Stalk Servo	
			stalk_id=args.getByte(stalk_id_s);
			currentAngle=180;
			maxAngle=args.getInt(maxAngle_s,270);
			minAngle=args.getInt(minAngle_s,180);
			
			
			
			//Eye Iris
			iris_id=args.getByte(iris_id_s);

			//Eye Led
			led_id=args.getByte(led_id_s);
			
		} 
		else {
			throw new ClassCastException("Need Arguements for Dome Items!");

		}
		
	}

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle args) {
		if (args != null) {
			//Dome
			dome_id=args.getByte(dome_id_s);
			currentSteps=0;
			maxSteps=args.getInt(maxSteps_s,-1);
			minSteps=args.getInt(minSteps_s,-1);
			
			//Eye Stalk Servo	
			stalk_id=args.getByte(stalk_id_s);
			currentAngle=180;
			maxAngle=args.getInt(maxAngle_s,270);
			minAngle=args.getInt(minAngle_s,180);
			
			
			
			//Eye Iris
			iris_id=args.getByte(iris_id_s);

			//Eye Led
			led_id=args.getByte(led_id_s);
			
		}
		else{
			
		}
			
		return inflater.inflate(R.layout.layout_domecontrolls, container, false);
		
	}
	
	@Override
	public void onViewCreated(View view, Bundle args){
		
		if (args != null) {
			//Dome
			dome_id=args.getByte(dome_id_s);
			currentSteps=0;
			maxSteps=args.getInt(maxSteps_s,-1);
			minSteps=args.getInt(minSteps_s,-1);
			
			//Eye Stalk Servo	
			stalk_id=args.getByte(stalk_id_s);
			currentAngle=180;
			maxAngle=args.getInt(maxAngle_s,270);
			minAngle=args.getInt(minAngle_s,180);
			
			
			
			//Eye Iris
			iris_id=args.getByte(iris_id_s);

			//Eye Led
			led_id=args.getByte(led_id_s);
			
		}
		else{
			
		}
		
		try{
				domeSeekBar=(CircularSeekBar) view.findViewById(R.id.servoDome);
				domeSeekBar.setSeekBarChangeListener(new OnSeekChangeListener(){
					@Override
					public void onProgressChange(CircularSeekBar dome, float newProgress) {
						currentAngle=dome.getAngle();
						
						byte [] command = new byte [5];
				    	ByteBuffer bos = ByteBuffer.wrap(command);
						bos.put(dome_id);
						bos.putFloat(currentAngle);
						
						
						DalekServerConnect.static_sendCommand(getActivity(),command);
					}
		
					
				});
				
		}catch(Exception e){
			e.printStackTrace();
		}
		
		try{
			CircularSeekBar eyeStalk2=(CircularSeekBar) view.findViewById(R.id.servoEyeStalk);
				eyeStalk2.setSeekBarChangeListener(new OnSeekChangeListener(){
					@Override
					public void onProgressChange(CircularSeekBar l_eyeStalk, float newProgress) {
						currentAngle=l_eyeStalk.getAngle();
						
						byte [] command = new byte [5];
				    	ByteBuffer bos = ByteBuffer.wrap(command);
						bos.put(stalk_id);
						bos.putFloat(currentAngle);
						
						
						DalekServerConnect.static_sendCommand(getActivity(),command);
					}
		
					
				});
				eyeStalk=eyeStalk2;
		}catch(Exception e){
			e.printStackTrace();
		}
		
		SeekBar ledBar = (SeekBar)view.findViewById(R.id.eyeLED);
		ledBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				byte [] command = new byte [5];
		    	ByteBuffer bos = ByteBuffer.wrap(command);
				bos.put(led_id);
				bos.putFloat(progress);
				
				
				DalekServerConnect.static_sendCommand(getActivity(),command);
				
				
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			
		});
				
		SeekBar irisBar = (SeekBar)view.findViewById(R.id.eyeIRIS);
		irisBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				byte [] command = new byte [5];
		    	ByteBuffer bos = ByteBuffer.wrap(command);
				bos.put(iris_id);
				bos.putFloat(progress);
				
				
				DalekServerConnect.static_sendCommand(getActivity(),command);
				
				
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				
			}
			
		});
		
	}
	
}
