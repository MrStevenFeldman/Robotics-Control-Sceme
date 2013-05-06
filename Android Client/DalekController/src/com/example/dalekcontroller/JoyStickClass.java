package com.example.dalekcontroller;

import android.app.Activity;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGestureListener;
import android.util.Log;
import android.view.MotionEvent;

public class JoyStickClass extends Activity implements OnGestureListener {
	private static final String TAG = "Steven";

	//"@+id/joystickgov"
	//joystick_variable=R.id.joystickgov;

	GestureOverlayView gov;
	
	JoyStickClass(GestureOverlayView gov2){
		
		gov=gov2;
		gov.addOnGestureListener(this); 
	}

	
	   @Override
		public void onGesture(GestureOverlayView overlay, MotionEvent event) {
			// TODO Auto-generated method stub
	    	Log.d(TAG,"continueing....");
	    	
		}

		@Override
		public void onGestureCancelled(GestureOverlayView overlay, MotionEvent event) {
			// TODO Auto-generated method stub
			Log.d(TAG,"cancelled....");
		}

		@Override
		public void onGestureEnded(GestureOverlayView overlay, MotionEvent event) {
			// TODO Auto-generated method stub
			Log.d(TAG,"ended....");
		}

		@Override
		public void onGestureStarted(GestureOverlayView overlay, MotionEvent event) {
			// TODO Auto-generated method stub
			Log.d(TAG,"started....");
			
		}

		
	
}