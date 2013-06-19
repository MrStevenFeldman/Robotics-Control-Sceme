package com.example.dalekcontroller;

import com.dalekcontroller.device.Servo;
import com.dalekcontroller.device.ServoPair;
import com.dalekcontroller.device.motor.BIDirectionalMotorPair;
import com.dalekcontroller.device.motor.BiDirectionalMotor;
import com.dalekcontroller.device.motor.UniDirectionalMotor;

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
		
		if (findViewById(R.id.bidirectionalContainer) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            BiDirectionalMotor biMoto = new BiDirectionalMotor();
			Bundle bundle1=new Bundle();
			bundle1.putInt(BiDirectionalMotor.deviceID_s, 1);
            biMoto.setArguments(bundle1);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.bidirectionalContainer, biMoto).commit();
            
            
            // Create an instance of BIDirectionalMotorPair
            BIDirectionalMotorPair biMotoPair = new BIDirectionalMotorPair();
            
            // pass the Intent's extras to the fragment as arguments
            Bundle bundle2=new Bundle();
            bundle2.putInt(BIDirectionalMotorPair.deviceID_A_s, 1);
            bundle2.putInt(BIDirectionalMotorPair.deviceID_B_s, 2);
            biMotoPair.setArguments(bundle2);
            
            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.bidirectionalpairContainer, biMotoPair).commit();
            
 UniDirectionalMotor uniMotoPair = new UniDirectionalMotor();
            
            // pass the Intent's extras to the fragment as arguments
            Bundle bundle3=new Bundle();
            bundle3.putInt(UniDirectionalMotor.deviceID_s, 1);
            uniMotoPair.setArguments(bundle3);
            
            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.unidirectionalContainer, uniMotoPair).commit();
            
            
            ServoPair servo = new ServoPair();
            
            // pass the Intent's extras to the fragment as arguments
            Bundle bundle4=new Bundle();
            bundle4.putInt(ServoPair.id_A_s, 3);
            bundle4.putInt(ServoPair.id_B_s, 4);
            servo.setArguments(bundle4);
            
            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.servoContainer, servo).commit();
           
        }
    
	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_control, menu);
		return true;
	}


    public void disconnect_func(View view){
    	//TODO: Stop all Motors
    	//TODO: Disconnect from DCU
    	
    	Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		
    	
    }
    
    
}
