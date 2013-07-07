package dalek.server;


import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import dalek.server.contolunits.ControlUnit;


public class MotorDevice {
	final static int DEVICE_ID=1;
	
	final static int SLEEP_AMOUNT=100;//TODO CHECK
	final static int ADJUST_DUTY_BY_VALUE=5;
	
	final static int SET_ENABLE=0, SET_DIRECTION=1, SET_SPEED=2;
	final static int ENABLED=1, DISABLED=0;
	final static int FORWARD=1, REVERSE=0;
	static HashMap<Integer, MotorDevice> cache=new  HashMap<Integer, MotorDevice>();
	
	
	//Local Variables
	int id;
	
	//State Variables	
	int current_speed=0;
	int target_speed=0;
	int direction=FORWARD;
	int isEnabled=DISABLED;
	
	//HardWare info
	boolean isBiDirectional;
	int enablePin;
	int PWM_A;
	int PWM_B;
	int currentSenseA;
	int currentSenseB;
	ControlUnit cu;
	
	public String toString(){
		String res;
		if(isBiDirectional){
			res=String.format("Bidirectional Motor[%d]:\n\tEnabled?:%d\tDirection?:%d\n\tEnable Pin:%d\n\tPWM_A:%d\tPWM_B:%d\n\tCurrent Sense A: %d\tCurrent Sense B: %d\n\tCurrent Speed:%d\tTarget Speed:%d\n",
					id,isEnabled, direction, enablePin, PWM_A,PWM_B,currentSenseA, currentSenseB, current_speed, target_speed );
		}
		else{
			res=String.format("Unidirectional Motor[%d]:\n\tEnabled?:%d\tDirection?:%d\n\tEnable Pin:%d\n\tPWM_A:%d\n\tCurrent Sense A: %d\n\tCurrent Speed:%d\tTarget Speed:%d\n",id,
					isEnabled, direction, enablePin, PWM_A,currentSenseA, current_speed, target_speed );
		}
		return res;
	}
	
	static void stop_all_motors(){
		for(MotorDevice m : cache.values()){
			m.disconnect_stop();
		}
	}
	public void disconnect_stop(){
		this.target_speed=0;
		try{
			this.cu.set_pwm_duty(this.PWM_A, 0);
			this.cu.digital_write_pin(this.enablePin, DISABLED);
			this.isEnabled=DISABLED;
		} catch (Exception e) {
			System.err.printf("Error Updating PWM!\n");
			e.printStackTrace();
		}
		this.current_speed=0;

	}
	public static int process_motor_command(InputStream is){
		int count, operation;
		try {
			count = is.read();
	    	DalekServlet.LOGGER.info(String.format("Read: %d as devices count", count));
			if(count == -1){
				System.err.printf("Unexpected(1) end of input stream, bi directional motor processesing\n");
				return -1;
			}
			
			operation = is.read();
	    	DalekServlet.LOGGER.info(String.format("Read: %d as operation type", operation));
			if(operation == -1){
				System.err.printf("Unexpected(2) end of input stream, bi directional motor processesing\n");
				return -1;
			}
			
			MotorDevice [] list=new MotorDevice[count];
			int [] operands=new int[count];
			
	    	DalekServlet.LOGGER.info("Read: Devices and operands ");
			for(int i=0; i<count; i++){
				int id = is.read();
				int operand = is.read();
				if(id == -1 || operand == -1){
					System.err.printf("Unexpected end of input stream, bi directional motor processesing %dth element.\n",i);
					return -1;
				}
				list[i]=cache.get(id);
				if(list[i] == null){
					System.err.printf("Unkown ID[ID: %d Operand: %d] in input stream, bi directional motor processesing %dth element.\n",id,operand,i);
					return -1;
				}
				operands[i]=operand;
		    	DalekServlet.LOGGER.info(String.format("\tRead: ID: %d Operand:%d", id, operand));
			}
			
			if(operation == SET_ENABLE){
				//STOP MOTORS
				//First Stop All Motors, disabling means a quck stop is needed!.
				for(int i=0; i<count; i++){
					list[i].target_speed=0;
					try {
						list[i].cu.set_pwm_duty(list[i].PWM_A,  0);
					} catch (Exception e) {
						System.err.printf("Error Updating PWM!\n");
						e.printStackTrace();
						return -1;	
					}
					list[i].current_speed=0;

				}
				
				
				//Motors Stopped
				for(int i=0; i<count; i++){

					if(operands[i] !=ENABLED &&  operands[i]!=DISABLED){
						System.err.printf("Unrecognized operend code [%d]  SET_ENABLE for Motor %d element\n",operands[i],i);
						return -1;
					}

					try {
						list[i].cu.digital_write_pin(list[i].enablePin, operands[i]);
					} catch (Exception e) {
						System.err.printf("Error Updating Enable pin\n");
						e.printStackTrace();
						return -1;
					}
					
					list[i].isEnabled=operands[i];

					
				}
			}
			else if(operation == SET_DIRECTION){
			
				//STOP MOTORS
				//First Stop All Motors.
				for(int i=0; i<count; i++){
					list[i].target_speed=0;
				}
				boolean allstopped;
				do{
					allstopped=true;
					for(int i=0; i<count; i++){
						if(list[i].target_speed ==list[i].current_speed){
							continue;
						}
						int new_speed=list[i].current_speed-ADJUST_DUTY_BY_VALUE;
						if(new_speed < 0) new_speed=0;
						
						try {
							list[i].cu.set_pwm_duty(list[i].PWM_A,  new_speed);
						} catch (Exception e) {
							System.err.printf("Error Updating PWM!\n");
							e.printStackTrace();
							return -1;
							
						}


						list[i].current_speed=new_speed;


						if(list[i].target_speed !=list[i].current_speed){
							allstopped=false;
						}
					}

					 try {
							Thread.sleep(SLEEP_AMOUNT);
						} catch (InterruptedException e) {
							e.printStackTrace();
							return -1;
						}

				}while(!allstopped);
				
				
				//Operation == SET_DIRECTION
				for(int i=0; i<count; i++){
					if(!list[i].isBiDirectional){
						System.err.printf("Warning: Unidirectional motor included with bidirectional motor list is ignored [%d]", i);
						continue;
					}
					if(operands[i] != FORWARD && operands[i] != REVERSE){
						System.err.printf("Unrecognized operend code [%d]  SET_DIRECTION for Motor %d element\n", operands[i], i);
						return -1;
					}

					if(list[i].direction == operands[i]){
						continue;
					}
					else{
						//Reverse the pins
						int temp=list[i].PWM_A;
						list[i].PWM_A=list[i].PWM_B;
						list[i].PWM_B=temp;
						list[i].direction=operands[i];
					}
					
				}
			}
			else if (operation == SET_SPEED){
				for(int i=0; i<count; i++){
					list[i].target_speed=operands[i];
				}
				boolean allToSpeed;
				do{
					allToSpeed=true;
					for(int i=0; i<count; i++){
						int new_speed;

						if(list[i].target_speed  < list[i].current_speed){
							new_speed=list[i].current_speed-ADJUST_DUTY_BY_VALUE;

							if(new_speed < list[i].target_speed){
								new_speed=list[i].target_speed;
							}
						}
						else if(list[i].target_speed > list[i].current_speed){
							new_speed=list[i].current_speed+ADJUST_DUTY_BY_VALUE;

							if(new_speed > list[i].target_speed){
								new_speed=list[i].target_speed;
							}
						}
						else{
							continue;
						}
						
						try {
							list[i].cu.set_pwm_duty(list[i].PWM_A, new_speed);
						} catch (Exception e) {
							System.err.printf("Error Updating PWM!\n");
							e.printStackTrace();
							return -1;
						}
						
						list[i].current_speed=new_speed;


						if(list[i].target_speed !=list[i].current_speed){
							allToSpeed=false;
						}
					}

					 try {
						Thread.sleep(SLEEP_AMOUNT);
					} catch (InterruptedException e) {
						e.printStackTrace();
						return -1;
					}
				}while(!allToSpeed);


			}
			else{
				System.err.printf("Unkown Operation, bi directional motor\n");
				return -1;
			}
			
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return 1;
	}
	
	
	public MotorDevice(int id, int enablePin, int PWM_A, int PWM_B, int currentSenseA, int currentSenseB, int cu_id ) throws Exception{
		this.id=id;
		this.enablePin=enablePin;
		this.PWM_A=PWM_A;
		this.PWM_B=PWM_B;
		this.currentSenseA=currentSenseA;
		this.currentSenseB=currentSenseB;
		this.isBiDirectional=true;
		this.cu=DalekServlet.control_units.get(cu_id);
		
		if(cache.containsKey(id)){
			String error=String.format("MotorDevice With ID %d already exists in the system",id);
			throw new Exception(error);
		}
		
		cache.put(id, this);
		
		this.cu.init_pwm(this.PWM_A, this.PWM_B);
		this.cu.init_gpio_pin_input(this.enablePin );
		if(currentSenseA >=0)
			this.cu.init_gpio_pin_output(this.currentSenseA );
		if(currentSenseB >=0 )
			this.cu.init_gpio_pin_output(this.currentSenseB );
		
		this.cu.digital_write_pin(this.enablePin, DISABLED);
	
		

	}
		
	public MotorDevice(int id, int enablePin, int PWM_A,  int currentSenseA, int cu_id ) throws Exception{
		this.id=id;
		this.enablePin=enablePin;
		this.PWM_A=PWM_A;
		this.PWM_B=-1;
		this.currentSenseA=currentSenseA;
		this.currentSenseB=-1;
		this.isBiDirectional=false;
		this.cu=DalekServlet.control_units.get(cu_id);
		
		this.cu.init_pwm(this.PWM_A);
		this.cu.init_gpio_pin_input(this.enablePin );
		if(currentSenseA >=0)
			this.cu.init_gpio_pin_output(this.currentSenseA );
		this.cu.digital_write_pin(this.enablePin, DISABLED);

		
		if(cache.containsKey(id)){
			String error=String.format("MotorDevice With ID %d already exists in the system",id);
			throw new Exception(error);
		}
		cache.put(id, this);
		
	
	}
	public static void LOG_Device_States() {
		DalekServlet.LOGGER.info("======Begin Motor Devices==================");
		for( MotorDevice m : cache.values()){
			DalekServlet.LOGGER.info(m.toString());
		}
		DalekServlet.LOGGER.info("======End   Motor Devices==================");
	}
}
