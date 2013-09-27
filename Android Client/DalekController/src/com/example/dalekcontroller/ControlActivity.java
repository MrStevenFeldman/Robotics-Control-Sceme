package com.example.dalekcontroller;

import com.dalekcontroller.device.DomeControlls;
import com.dalekcontroller.device.Servo;
import com.dalekcontroller.device.ServoPair;
import com.dalekcontroller.device.motor.BiDirectionalMotor;
import com.dalekcontroller.device.motor.ZPTMotorPair;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;

public class ControlActivity extends FragmentActivity  {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_control);
		
		 // However, if we're being restored from a previous state,
        // then we don't need to do anything and should return or else
        // we could end up with overlapping fragments.
		if (savedInstanceState != null) {
            return;
        }
		
		//TODO get all info from config file
		
		
		ZPTMotorPair zpt = new ZPTMotorPair();
        Bundle bundle1=new Bundle();
		bundle1.putByte(ZPTMotorPair.deviceID_s, (byte) 1);
		zpt.setArguments(bundle1);
		getSupportFragmentManager().beginTransaction()
			.add(R.id.zpt_container, zpt).commit();
		
		ServoPair gunPair = new ServoPair();
		Bundle bundle2=new Bundle();
		bundle2.putByte(ServoPair.deviceID_s, (byte) 2);
		gunPair.setArguments(bundle2);
		getSupportFragmentManager().beginTransaction()
				.add(R.id.gun_container, gunPair).commit();
	
		ServoPair plungerPair = new ServoPair();
		Bundle bundle3=new Bundle();
		bundle3.putByte(ServoPair.deviceID_s, (byte) 3);
		plungerPair.setArguments(bundle3);
		getSupportFragmentManager().beginTransaction()
				.add(R.id.plunger_container, plungerPair).commit();
		
		
		DomeControlls domeTest = new DomeControlls();
		Bundle outState=new Bundle();
		//Dome
				outState.putByte(DomeControlls.dome_id_s, (byte)-1);
				outState.putInt(DomeControlls.maxSteps_s, -1);
				outState.putInt(DomeControlls.minSteps_s, -1);
				
				//Eye Stalk Servo	
				outState.putByte(DomeControlls.stalk_id_s, (byte)-1);
				outState.putInt(DomeControlls.maxAngle_s, 270);
				outState.putInt(DomeControlls.minAngle_s, 180);
				
				
				//Eye Iris
				outState.putByte(DomeControlls.iris_id_s, (byte)-1);

				//Eye Led
				outState.putByte(DomeControlls.led_id_s, (byte)-1);
				

		domeTest.setArguments(outState);
		getSupportFragmentManager().beginTransaction()
				.add(R.id.dome_container, domeTest).commit();
	
		
            
	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_control, menu);
		return true;
	}


    public void disconnect_func(View view){
    	DalekServerConnect.live_connect.close_connection();
    	Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		
    	
    }
    
    
}
