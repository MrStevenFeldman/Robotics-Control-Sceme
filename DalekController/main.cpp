//
//  main.cpp
//  DalekController
//
//  Created by Steven Feldman on 4/28/13.
//  Copyright (c) 2013 Steven FELDMAN. All rights reserved.
//

/**WARNING THIS CODE HAS NOT BEEN TESTED! **/
/**USE AT YOUR OWN RISK! **/

/**
 Lots of TODOS

 
 2) Add special code for controlling motors in union

 3) Add actual input stream reading.

 4) Add response messages
	ie send back the motor values so control unit can display them.
 
 5) Add code for Stepper, piston, soleniod control code and file load code
 
 6) Add proper error an debugging messages
 
 **/

#define LOW_WRITE 0
#define HIGH_WRITE 1
#define DELAY 100 /*milliseconds*/

#include <iostream>
#include <stdio.h>


/*TODO echo this enum from a file*/
enum DeviceType{
	BiDirectionalMotor,
	UniDirectionalMotor,
	StepperMotor,
	Piston,
	Soleniod
};

typedef struct{
	int  id;

//State Values
	int speed;
	int direction;
	int isEnabled;
	
	//Pins
	int enablePin;
	int PWM_A;
	int PWM_B;
	int currentSense;

}BiDirectionalMotor_struct;

typedef struct{
	int  id;

	//State Values
	int speed;
	int isEnabled;

	//Pins
	int enablePin;
	int PWM_A;
	int currentSense;

	
}UniDirectionalMotor_struct;

typedef struct{
	int id;

	int step;

	int stepsPerRevolution;
	int num_wires;

	
	int pins[8];
	
}StepperMotor_struct;

char getNextByte();
void pin_write_wrapper(int pin, int value);


void pin_delay_write_wrapper(int pin, int new_value, int current_value);

void *getDeviceStruct(int id);
int cache_device(int key, void * value);

void process_BiDirectionalMotor();
void process_UniDirectionalMotor();
void process_StepperMotor();
void process_Piston();
void procss_Soleniod();

int init(char * fname);
int read_next_device(FILE *file);

int init(char * fname){
	if(fname==NULL) return -1;

	FILE *fin=fopen(fname, "r");

	int res;
	do{
		res=read_next_device(fin);
	}while(res!=-1 || res !=0);

	return res;

}
int read_next_device(FILE *fin){
	int id= -1; void *devicep=NULL;

	int deviceType=-1;
	int res=fscanf(fin, "%d ", &deviceType);
	if(res==0){ return 0; }

	switch(deviceType){
		case BiDirectionalMotor:
		{
			
			int enablePin;
			int PWM_A;
			int PWM_B;
			int currentSense;
			
			res=fscanf(fin, "%d %d %d %d %d\n", &id, &enablePin, &PWM_A, &PWM_B, &currentSense);
			if(res != 5) return -1;

			devicep=malloc(sizeof(BiDirectionalMotor_struct));
			BiDirectionalMotor_struct * device=(BiDirectionalMotor_struct *)devicep;

			device->id=id;

			device->isEnabled=LOW_WRITE;
			device->direction=1;
			device->speed=0;

			device->enablePin=enablePin;
			device->PWM_A=PWM_A;
			device->PWM_B=PWM_B;
			device->currentSense=currentSense;
			

		}
			break;
		case UniDirectionalMotor:
		{

			int enablePin;
			int PWM_A;
			int currentSense;

			res=fscanf(fin, "%d %d %d %d\n", &id, &enablePin, &PWM_A, &currentSense);
			if(res != 4) return -1;

			devicep=malloc(sizeof(UniDirectionalMotor_struct));
			UniDirectionalMotor_struct * device=(UniDirectionalMotor_struct *)devicep;

			device->id=id;

			device->isEnabled=LOW_WRITE;
			device->speed=0;

			device->enablePin=enablePin;
			device->PWM_A=PWM_A;
			device->currentSense=currentSense;
			
			
		}
			break;
		case StepperMotor:
		{
			int stepsPerRevolution;
			int num_wires;

			res=fscanf(fin, "%d %d %d", &id, &stepsPerRevolution, &num_wires);
			if(res != 3) return -1;

			devicep=malloc(sizeof(StepperMotor_struct));
			StepperMotor_struct * device=(StepperMotor_struct *)devicep;

			device->id=id;
			device->step=0;
			device->stepsPerRevolution=stepsPerRevolution;
			device->num_wires=num_wires;

			if(num_wires >8) return -1;
			
			for(int i=0; i<num_wires; i++){
				int pin;
				res=fscanf(fin, "%d", &pin);
				if(res != 1) return -1;

				device->pins[i]=pin;
			}
			res=fscanf(fin, "\n");
			
		}
			break;
		case Piston:
			//TODO add piston data read
			break;
			
		case Soleniod:
			//TODO add soleniod data read in
			break;
		default:
			return -1;

	}

	
	res=cache_device(id, devicep);
	if(res==-1){
		//TODO: output error, probally duplicate id.
		return -1;
	}
	
	return 0;
}

int cache_device(int key, int value){
	if(key== -1 ) return -1;
	
	//TODO implement hash table
	//return 1 if success, -1 if failer

	return 1;
};

int main(int argc, const char * argv[])
{
	//TODO ad simulation code here
	return 0;
}

int listen_for_commands(){

	while(true){
		char res=getNextByte();


		switch(res){
			case BiDirectionalMotor:
				process_BiDirectionalMotor();
				break;
			case UniDirectionalMotor:
				process_UniDirectionalMotor();
				break;
			case StepperMotor:
				process_StepperMotor();
				break;
			case Piston:
				process_Piston();
				break;
			case Soleniod:
				procss_Soleniod();
				break;
			default:
				return -1;

		}

	}

}

char getNextByte(){
	//TODO add input stream reading here
	return 1;

};

void process_BiDirectionalMotor(){
	/*[ID] [setEnabled | setDirection] [Speed | Enable/Diabled] */
	/* setEnabled = 0, setDirection=-1/1 */
	char id=getNextByte();
	char opcode=getNextByte();
	char operand=getNextByte();

	BiDirectionalMotor_struct * device=(BiDirectionalMotor_struct *)getDeviceStruct(id);
	
	if(opcode == 0){
		//setEnabled
		/*if operand == 0 then disabled, else enable*/
		/* either way set speed to 0 */



		if(operand !=0) operand = HIGH_WRITE;
		else operand = LOW_WRITE;

		pin_delay_write_wrapper(device->PWM_A, device->speed,  0);
		pin_write_wrapper(device->enablePin, operand);

		//These two lines don't change value of pins, and are only for knowing what everything is set to
		device->isEnabled=operand;
		device->speed=0;
		///////
		

	}
	else{
		if(device->direction == opcode){
			//Just need to update the speed
			pin_write_wrapper(device->PWM_A, operand);
			device->speed=operand;

		}
		else{
			device->direction = opcode;
			//Need to perform a full stop
			pin_delay_write_wrapper(device->PWM_A, device->speed,  0);

			//Swap direction
			int temp=device->PWM_A;
			device->PWM_A=device->PWM_B;
			device->PWM_B=temp;

			

			//write new speed
			pin_delay_write_wrapper(device->PWM_A, device->speed, operand);
			device->speed=operand;
		}


	}

};

void process_UniDirectionalMotor(){
	/*[ID] [1 Speed] | [0] */
	/* setEnabled = 0, setDirection=-1/1 */
	char id=getNextByte();
	char enabled=getNextByte();

	UniDirectionalMotor_struct * device=(UniDirectionalMotor_struct *)getDeviceStruct(id);

	if(enabled == 0){
		//Disable the Motor
		
		pin_delay_write_wrapper(device->PWM_A, device->speed,  0);
		pin_write_wrapper(device->enablePin, LOW_WRITE);

		//These two lines don't change value of pins, and are only for knowing what everything is set to
		device->isEnabled=LOW_WRITE;
		device->speed=0;
		///////


	}
	else{
		char speed=getNextByte();
		//Just need to update the speed
		pin_write_wrapper(device->PWM_A, speed);
		device->speed=speed;
	}
};

void *getDeviceStruct(int id){
//Todo implement a hashtable that stores ids as keys and values as the pointer to the state structs.

	return NULL;
};


void process_StepperMotor(){
	/** [ID Direction Steps] **/
	/** Direction 1 or -1  (two commands so that the steps can be up 256, instead of 128**/
	char id=getNextByte();
	char dir=getNextByte();
	char steps=getNextByte();

	
	StepperMotor_struct * device=(StepperMotor_struct *)getDeviceStruct(id);

	//TODO figure out wrapper for stepper


	device->step=device->step+ (dir*steps);


};

void process_Piston(){

	//TODO do piston
};

void process_Solenoid(){

	//TODO do solenoid
};

void pin_write_wrapper(int pin, int value){
//TODO
};

void pin_delay_write_wrapper(int pin, int new_value, int current_value){
	int increment_by=5;

	if( current_value < new_value){
		int i;
		for(i=current_value; i< new_value; i=i+increment_by ){
			pin_write_wrapper(pin, i);
			sleep(DELAY);
		}
		if(i!=new_value){
			pin_write_wrapper(pin, new_value);
			sleep(DELAY);
		}

	}
	else{
		int i;
		for(i=current_value; i > new_value; i=i-increment_by ){
			pin_write_wrapper(pin, i);
			sleep(DELAY);
		}
		if(i!=new_value){
			pin_write_wrapper(pin, new_value);
			sleep(DELAY);
		}
	}

}
						  
