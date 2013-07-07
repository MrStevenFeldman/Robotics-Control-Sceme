package com.example.dalekcontroller;

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
		
		if (findViewById(R.id.zpt_container) != null) {

            ZPTMotorPair zpt = new ZPTMotorPair();
            Bundle bundle1=new Bundle();
			bundle1.putByte(ZPTMotorPair.deviceID_s, (byte) 1);
			zpt.setArguments(bundle1);
			getSupportFragmentManager().beginTransaction()
				.add(R.id.zpt_container, zpt).commit();
		}
			
//            BiDirectionalMotor biMoto = new BiDirectionalMotor();
//			Bundle bundle1=new Bundle();
//			bundle1.putInt(BiDirectionalMotor.deviceID_s, 1);
//            biMoto.setArguments(bundle1);
//
//            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.bidirectionalContainer, biMoto).commit();
            
	
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
