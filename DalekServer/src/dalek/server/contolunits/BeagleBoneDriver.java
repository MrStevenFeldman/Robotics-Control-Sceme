package dalek.server.contolunits;

import dalek.server.DalekServlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class BeagleBoneDriver implements ControlUnit {
	static int magic_cape_id=9;
	static int period; 

	//TODO: Car Duty file for PWM and if not equal to assigned throw error
	
	public BeagleBoneDriver(int mci,  int per) throws IOException{
		magic_cape_id=mci;
		period=per;
    	DalekServlet.LOGGER.info("BBK Driver: MCI "+magic_cape_id+"  P: "+period);

		String command="echo am33xx_pwm > /sys/devices/bone_capemgr."+magic_cape_id+"/slots";
		executeCommand(command);
		

		return;
	}
	public void init_pwm(int pinA, int pinB) throws Exception {
		init_pwm(pinA);
		init_pwm(pinB);
	}

	@Override
	public void init_pwm(int pin) throws Exception {
		//echo bone_pwm_P8_13 > /sys/devices/bone_capemgr.9/slots
		int p=8;
		if(pin < 0){
			p=9;
			pin= -pin;
		}
		String command_one = "echo bone_pwm_P"+p+"_"+pin + " > /sys/devices/bone_capemgr."+magic_cape_id+"/slots";
		//# echo 20000000 > /sys/devices/ocp.2/pwm_test_P8_13.15/period

		String command_three = "echo "+period+" >  /sys/devices/ocp.2/pwm_test_P"+p+"_"+pin+"."+"*"+"/duty";
		String command_two = "echo "+period+" >  /sys/devices/ocp.2/pwm_test_P"+p+"_"+pin+"."+"*"+"/period";

		executeCommand(command_one);
		executeCommand(command_two);
		executeCommand(command_three);
	}

	@Override
	public void set_pwm_duty(int pin, int duty) throws Exception {
		//# echo 10000000 > /sys/devices/ocp.2/pwm_test_P8_13.15/duty
		int p=8;
		if(pin < 0){
			p=9;
			pin= -pin;
		}
		double nduty=duty;
		nduty=(nduty/255.00)*period;
		duty=period-(int)nduty;
		String command_one = "echo "+duty+" >  /sys/devices/ocp.2/pwm_test_P"+p+"_"+pin+"."+"*"+"/duty";
    
		executeCommand(command_one);
	}

	@Override
	public void init_gpio_pin_input(int pin) throws Exception {
		int gpio=getGPIO(pin);
		String command_one ="echo "+gpio+" > /sys/gpio/export";
		String command_two="echo in >  /sys/class/gpio/gpio"+gpio+"/direction";
		String command_three="echo 1 >  /sys/class/gpio/gpio"+gpio+"/value";

		executeCommand(command_one);
		executeCommand(command_two);
		executeCommand(command_three);
	}

	@Override
	public void init_gpio_pin_output(int pin) throws Exception {
		int gpio=getGPIO(pin);
		String command_one ="echo "+gpio+" > /sys/gpio/export";
		String command_two="echo out >  /sys/class/gpio/gpio"+gpio+"/direction";
		String command_three="echo 1 >  /sys/class/gpio/gpio"+gpio+"/value";
		executeCommand(command_one);
		executeCommand(command_two);
		executeCommand(command_three);
	}

	@Override
	public int analog_read_pin(int pin) throws Exception {
		throw new Exception("Not valid function with BeagleBone Black");
	}

	@Override
	public int digital_read_pin(int pin) throws Exception {
		int gpio=getGPIO(pin);
		String command_one="cat /sys/class/gpio/gpio"+gpio+"/value";
		
		
		try{
			String res=executeCommand(command_one);
			int resV=Integer.parseInt(res);
			
			return resV;
		}catch(Exception e){
			//TODO Log error
			return -1;
		}
	}

	@Override
	public void analog_write_pin(int pin, int value) throws Exception {
		throw new Exception("Not valid function with BeagleBone Black");
	}

	@Override
	public void digital_write_pin(int pin, int value) throws Exception {
		int gpio=getGPIO(pin);
		String command_one="echo "+value+" > /sys/class/gpio/gpio"+gpio+"/value";
		executeCommand(command_one);
	}
	
	private static String executeCommand(String cmdLine) throws IOException {
        String output = "";
        
    	DalekServlet.LOGGER.info("Read: PWM "+cmdLine);

        try {
        	String [] commands={"bash","-c",cmdLine};
            Process p = Runtime.getRuntime().exec(	commands);
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                output += (line + '\n');
            }
            input.close();
        } catch(IOException ex) {
            System.out.println(ex.getStackTrace());
            System.out.println(output);
            throw ex;
        }
        return output;
}  
	

	public int getGPIO(int pin){
		switch(pin){
		case -11:
			return 30;
		case -12:
			return 60;
		case -13:
			return 31;
		case -14:
			return 40;
		case -15:
			return 48;
		case -16:
			return 51;
		case -17:
			return 4;
		case -18:
			return 5;
		case -19:
			return 13;
		case -20:
			return 12;
		case -21:
			return 3;
		case -22:
			return 2;
		case -23:
			return 49;
		case -24:
			return 15;
		case -25:
			return 117;
		case -26:
			return 14;
		case -27:
			return 125;
		case -28:
			return 113;
		case -29:
			return 111;
		case -30:
			return 112;
		case -31:
			return 110;
		case -41:
			return 20;
		case -42:
			return 7;
			
		case 3:
			return 38;
		case 4:
			return 39;
		case 5:
			return 34;
		case 6:
			return 35;
		case 7:
			return 66;
		case 8:
			return 67;
		case 9:
			return 69;
		case 10:
			return 68;
		case 11:
			return 45;
		case 12:
			return 44;
		case 13:
			return 23;
		case 14:
			return 26;
		case 15:
			return 47;
		case 16:
			return 46;
		case 17:
			return 27;
		case 18:
			return 65;
		case 19:
			return 22;
		case 20:
			return 63;
		case 21:
			return 62;
		case 22:
			return 37;
		case 23:
			return 36;
		case 24:
			return 33;
		case 25:
			return 32;
		case 26:
			return 61;
		case 27:
			return 86;
		case 28:
			return 88;
		case 29:
			return 87;
		case 30:
			return 89;
		case 31:
			return 10;
		case 32:
			return 11;
		case 33:
			return 9;
		case 34:
			return 81;
		case 35:
			return 8;
		case 36:
			return 80;
		case 37:
			return 78;
		case 38:
			return 79;
		case 39:
			return 76;
		case 40:
			return 77;
		case 41:
			return 74;
		case 42:
			return 75;
		case 43:
			return 72;
		case 44:
			return 73;
		case 45:
			return 70;
		case 46:
			return 71;
		
		default:
			return -1;
		}
	}
}
