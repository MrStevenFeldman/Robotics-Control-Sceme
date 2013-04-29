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
 
 1) Code to read in Device Struct objects from a file
	File Format is each line holds a device object
		Each line consists of a set of numbers that are space seperated
		First Number is the device ID
			Followed by Pins.
 
 2) Add special code for controlling motors in union

 3) Add actual input stream reading.

 4) Add response messages
	ie send back the motor values so control unit can display them.
 
 
 **/

#define LOW_WRITE 0
#define HIGH_WRITE 1
#define DELAY 100 /*milliseconds*/

#include <iostream>


/*TODO echo this enum from a file*/
enum DeviceType{
	BiDirectionalMotor,
	UniDirectionalMotor,
	StepperMotor,
	Piston, /*Not Done */
	Soleniod /*Not Done */
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

	
	int pin1;
	int pin2;
	int pin3;
	int pin4;
	int pin5;
	int pin6;
	int pin7;
	int pin8;

}StepperMotor_struct;

char getNextByte();
void pin_write_wrapper(int pin, int value);


void pin_delay_write_wrapper(int pin, int new_value, int current_value);

void *getDeviceStruct(int id);

void process_BiDirectionalMotor();
void process_UniDirectionalMotor();
void process_StepperMotor();
void process_Piston();


int main(int argc, const char * argv[])
{

	// insert code here...
	std::cout << "Hello, World!\n";
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

		}

	}

}

char getNextByte(){
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
						  
