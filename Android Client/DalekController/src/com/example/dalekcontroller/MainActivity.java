package com.example.dalekcontroller;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {
	private ArduinoConnection arduinoCon;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		update_conneciton_display();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public void connectToggle(View view) {
		EditText et=(EditText) findViewById(R.id.ipTextArea);
    	String ip=et.getText().toString(); 
    	
		Button button_con=(Button)findViewById(R.id.connectToHost_button);
    	button_con.setEnabled(false);
    	
    	arduinoCon = new ArduinoConnection(ip);
    	arduinoCon.start();
    	
		update_conneciton_display();
	}
	public void update_conneciton_display(){
    	
    	Button man_control_button=(Button)findViewById(R.id.man_control_button);
    	
    	TextView connection_error_field=(TextView)findViewById(R.id.connection_error_field);
    	
		Button button_con=(Button)findViewById(R.id.connectToHost_button);

    	
		ConnectionState state_v;
    	while(true){
    		if(arduinoCon==null){
    			state_v=ConnectionState.Disconnected;
    			connection_error_field.setText("No Errors");
    		}else{
    			state_v=arduinoCon.state_v;
    			connection_error_field.setText(arduinoCon.message);
    		}
    		
    		if(state_v == ConnectionState.Uninitiated ||state_v == ConnectionState.Connecting){
    			continue;
    		}
    		else if(state_v == ConnectionState.Connected){
    			man_control_button.setEnabled(true);
    			break;
    		}
    		else if(state_v == ConnectionState.Disconnected){
    			button_con.setEnabled(true);
    			man_control_button.setEnabled(false);
    			break;
    		}
    		else if(state_v == ConnectionState.Error){
    			button_con.setEnabled(true);
    			man_control_button.setEnabled(false);
    			break;
    		}
    		
    		
    		
    	}
    	
    	
    	return;
	
    }
	
	public void switchToManualControlPage(View view){
		Intent intent = new Intent(this, MotorController.class);
		startActivity(intent);
		
	}
	
}
