package com.dalekcontroller.device.motor;

import android.support.v4.app.Fragment;

public abstract class MotorDevice extends Fragment {
	
	final short MAX_MOTOR_DUTY=100;
	final short DUTY_INCREMENT=5;
	final byte FORWARD=1;
	final byte REVERSE=0;
	final byte SPEED_COMMAND=2;
	final byte ENABLE_COMMAND=0;
	final byte DIRECTION_COMMAND=1;
	
	 /**This function sends commands to the dalek control unit **/

	public static void StopAll(){
		//TODO iterator over all motor device instances and call stop
	}
	
}
