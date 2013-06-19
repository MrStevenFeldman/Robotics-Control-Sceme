package com.dalekcontroller.device.motor;

import android.support.v4.app.Fragment;

public abstract class MotorDevice extends Fragment {
	
	static int MOTOR_DEVICE=1;
	final int MAX_MOTOR_DUTY=255;
	final int DUTY_INCREMENT=5;
	final int FORWARD=1;
	final int REVERSE=0;
	final int SPEED_COMMAND=2;
	final int ENABLE_COMMAND=0;
	final int DIRECTION_COMMAND=1;
	
	 /**This function sends commands to the dalek control unit **/

	public static void StopAll(){
		//TODO iterator over all motor device instances and call stop
	}
	
}
