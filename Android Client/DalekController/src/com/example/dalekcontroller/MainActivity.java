package com.example.dalekcontroller;


import android.os.Bundle;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends FragmentActivity  {
	private DalekServerConnect dcu_con;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		update_connection_display();
		
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
    	
    	dcu_con = new DalekServerConnect(ip);
    	dcu_con.start();
    	
		update_connection_display();
	}
	public void update_connection_display(){
    	
    	Button man_control_button=(Button)findViewById(R.id.man_control_button);
    	
    	TextView connection_error_field=(TextView)findViewById(R.id.connection_error_field);
    	
		Button button_con=(Button)findViewById(R.id.connectToHost_button);

    	
		ConnectionState state_v;
    	while(true){
    		if(dcu_con==null){
    			state_v=ConnectionState.Disconnected;
    			connection_error_field.setText("No Errors");
    			return ;
    		}

    		state_v=dcu_con.state_v;
  
    		if(state_v == ConnectionState.Uninitiated ||state_v == ConnectionState.Connecting){
    	    	connection_error_field.setText("Connecting...");

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
    	connection_error_field.setText(dcu_con.message);
    	
    	return;
	
    }
	
	public void switchToManualControlPage(View view){
		Intent intent = new Intent(this, ControlActivity.class);
		startActivity(intent);
		
	}
	
}
