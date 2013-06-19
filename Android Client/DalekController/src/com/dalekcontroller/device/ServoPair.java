package com.dalekcontroller.device;

import com.example.dalekcontroller.DalekServerConnect;
import com.example.dalekcontroller.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ServoPair  extends Servo{
	//Has an ID
	private int id_A, id_B; 
		public final static String id_A_s="idA"; public final static String id_B_s="idB";
	private int currentAngle_A=0;  private int currentAngle_B=0; 
		public final static String currentAngleA_s="currentAngleA"; public final static String currentAngle_B_s="currentAngleB";
	
	private int maxAngle_A=90; public final static String maxAngle_A_s="maxAngle_A";
	private int minAngle_A=-90; public final static String minAngle_A_s="minAngle_A";
	
	private int maxAngle_B=90; public final static String maxAngle_B_s="maxAngle_B";
	private int minAngle_B=-90; public final static String minAngle_B_s="minAngle_B";
	
	private int x_A, y_A, x_B, y_B;//Location on the circle where the steppers are
	private int Y_min, Y_max, X_min, X_max;
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// Save the current article selection in case we need to recreate the fragment
		outState.putInt(id_A_s, id_A);
		outState.putInt(currentAngle_s, currentAngle_A);
		outState.putInt(maxAngle_A_s, maxAngle_A);
		outState.putInt(minAngle_A_s, minAngle_A);
		
		outState.putInt(id_B_s, id_B);
		outState.putInt(currentAngle_B_s, currentAngle_B);
		outState.putInt(maxAngle_B_s, maxAngle_B);
		outState.putInt(minAngle_B_s, minAngle_B);

	}
	
	@Override
	public void onStart() {
		try{
		super.onStart();

		}catch(Exception e){
			e.printStackTrace();
		}
		// During startup, check if there are arguments passed to the fragment.
		// onStart is a good place to do this because the layout has already been
		// applied to the fragment at this point so we can safely call the method
		// below that sets the article text.
		Bundle args = getArguments();
		if (args != null) {
			// Set article based on argument passed in
			id_A=args.getInt(id_A_s);
			currentAngle_A=args.getInt(currentAngle_s, 0);
			maxAngle_A=args.getInt(maxAngle_A_s,90 );
			minAngle_A=args.getInt(minAngle_A_s, -90);
			
			id_B=args.getInt(id_B_s);
			currentAngle_B=args.getInt(currentAngle_B_s,0 );
			maxAngle_B=args.getInt(maxAngle_B_s, 90);
			minAngle_B=args.getInt(minAngle_B_s, -90);
			
		} 
		else {
			throw new ClassCastException("Need Arguements for Servo Motor Pair!");

		}
		
		//TODO GUI Part
		
	}

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			id_A=savedInstanceState.getInt(id_A_s);
			currentAngle_A=savedInstanceState.getInt(currentAngle_s, 0);
			maxAngle_A=savedInstanceState.getInt(maxAngle_A_s,90 );
			minAngle_A=savedInstanceState.getInt(minAngle_A_s, -90);
			
			id_B=savedInstanceState.getInt(id_B_s);
			currentAngle_B=savedInstanceState.getInt(currentAngle_B_s,0 );
			maxAngle_B=savedInstanceState.getInt(maxAngle_B_s, 90);
			minAngle_B=savedInstanceState.getInt(minAngle_B_s, -90);
		}
		else{
			
		}
			
		return inflater.inflate(R.layout.layout_servopair, container, false);
		
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState){
		
		if (savedInstanceState != null) {
			id_A=savedInstanceState.getInt(id_A_s);
			currentAngle_A=savedInstanceState.getInt(currentAngle_s, 0);
			maxAngle_A=savedInstanceState.getInt(maxAngle_A_s,90 );
			minAngle_A=savedInstanceState.getInt(minAngle_A_s, -90);
			
			id_B=savedInstanceState.getInt(id_B_s);
			currentAngle_B=savedInstanceState.getInt(currentAngle_B_s,0 );
			maxAngle_B=savedInstanceState.getInt(maxAngle_B_s, 90);
			minAngle_B=savedInstanceState.getInt(minAngle_B_s, -90);
		}
		else{
			
		}
		
		
		//TODO Gui componet
		
	}
	

	public void centerStick(){
		updateServo(0, 0);
		updateServo(0, 0);
	}
	public void shakeVertical(){
		updateServo(0, Y_min);
		updateServo(0, Y_max);
	}
	
	public void shakeHorizontal(){
		updateServo(X_min, 0);
		updateServo(X_max, 0);
	}
	
	
	public void circleClockwise(){
		for(int i=0; i<360; i=i+5){
			//(5cos(36¡), 5sin(36¡)
			
			int x=(int)(X_max*Math.cos(i));
			int y=(int)(Y_max*Math.sin(i));
			updateServo(x,y);
		}
		
		int x=(int)(X_max*Math.cos(360));
		int y=(int)(Y_max*Math.sin(360));
		updateServo(x,y);
		
	}
	
	public void circleCounterClockwise(){
		for(int i=360; i>0; i=i-5){
			//(5cos(36¡), 5sin(36¡)
			
			int x=(int)(X_max*Math.cos(i));
			int y=(int)(Y_max*Math.sin(i));
			updateServo(x,y);
		}
		
		int x=(int)(X_max*Math.cos(0));
		int y=(int)(Y_max*Math.sin(0));
		updateServo(x,y);
		
	}
	
	public void updateServo(int p_x, int p_y) {
		
		
		
		float currentAngle_t=getAngle(p_x,p_y,	x_A, y_A);
		currentAngle_A=(int)((currentAngle_t/180.00)*255.00);
		
		currentAngle_t=getAngle(p_x,p_y,	x_B, y_B);
		currentAngle_B=(int)((currentAngle_t/180.00)*255.00);
		
		int[] command={SERVO_DEVICE,2,id_A,currentAngle_A, id_B,currentAngle_B};
		DalekServerConnect.static_sendCommand(getActivity(),command);

	}
	public float getAngle(int x1, int y1, int x2, int y2) {
	    float angle = (float) Math.toDegrees(Math.atan2(x1 - x2, y1 - y2));

	    if(angle < 0){
	        angle += 360;
	    }

	    return angle;
	}
	
}
