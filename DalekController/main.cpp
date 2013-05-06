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
	ie send back the device current values so control unit can display them.
 
 5) Add code for [Stepper,  soleniod, linear actuator]
	*control code and file load code*
	Stepper will have code similar for the Big Easy Driver board
		http://www.schmalzhaus.com/BigEasyDriver/BigEasyDriver_UserManal.pdf
	
 
 
 7) We could have additional control devices connected over usb/ethernet. In order to control these we can have each device have control device id. This will tell how to update the pin settings.
		Concerns: If we are using a delay as we increment/decrement a PWM, do we do it on this side or control device?
			I think it should be hard coded on the control side
		Conceern: Updating two motors in union
			Both should be on the same control board, and it should be updated on that control board
 
 **/
#define ADJUST_DUTY_BY_VALUE 1
#define ENABLED 0
#define DISABLED 1
#define LOW_WRITE 0
#define HIGH_WRITE 1
#define DELAY 100 /*milliseconds*/
#include <assert.h>
#include <iostream>
#include <stdio.h>
#include <map>
std::map<int, void *> *device_cache;


/*TODO echo this enum from a file*/
enum DeviceType{

	BiDirectionalMotor, //0
	UniDirectionalMotor, //1
	StepperMotor, //2
	Piston,//3
	Soleniod, //4
	PairBiDirectionalMotor, //5
	PairUniDirectionalMotor //6
};

typedef struct{
	int  id;

//State Values
	int current_speed;
	int target_speed;
	
	int direction;
	int isEnabled;
	
	//Pins
	int enablePin;
	int PWM_A;
	int PWM_B;
	int currentSenseA;
	int currentSenseB;

}BiDirectionalMotor_struct;

typedef struct{




typedef struct{
	int  id;

	//State Values
	int current_speed;
	int target_speed;

	int isEnabled;

	//Pins
	int enablePin;
	int PWM_A;
	int currentSense;

	
}UniDirectionalMotor_struct;

typedef struct{
	int  id;

	UniDirectionalMotor_struct *left_motor;
	UniDirectionalMotor_struct *right_motor;

}PairUniDirectionalMotor_struct;

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

void process_BiDirectionalMotor(int n);
void process_UniDirectionalMotor(int n);
void process_StepperMotor(int n);

int init(char * fname);
int read_next_device(FILE *file);

int init(char * fname){

	printf("Initilizing\n");

	printf("\tLoading Device list from %s file\n",fname);
	device_cache=new std::map<int, void *>();
	

	if(fname==NULL) return -1;

	FILE *fin=fopen(fname, "r");

	int count=0;
	int res;
	do{
		res=read_next_device(fin);

		if(res==-1){
			printf("Error on Line %d, line was skipped\n", count);
		}
		count++;
	}while(res !=0);


	printf("Initilization Compelete");
	return res;

}
int read_next_device(FILE *fin){
	int return_val=0;
	int id= -1; void *devicep=NULL;

	int deviceType=-1;
	int res=fscanf(fin, "%d ", &deviceType);
	if(res==0){ return 0; }

	switch(deviceType){
		case PairBiDirectionalMotor:
		{
			int leftm, rightm;
			res=fscanf(fin, "%d %d %d", &id,&leftm, &rightm);
			if(res != 3){
				return_val= -1;
			}
			else{
				devicep=malloc(sizeof(PairBiDirectionalMotor_struct));
				PairBiDirectionalMotor_struct * device=(PairBiDirectionalMotor_struct *)devicep;

				device->id=id;

				device->left_motor=(BiDirectionalMotor_struct *)getDeviceStruct(leftm);
				device->right_motor=(BiDirectionalMotor_struct *)getDeviceStruct(rightm);
				if(device->left_motor == NULL || device->right_motor ==NULL){
					printf(" Left and Right Motors Must be defined before creating a pair ");
					return -1;
				}
				
			}
		}
			break;

		case PairUniDirectionalMotor:
		{
			int leftm, rightm;
			res=fscanf(fin, "%d %d %d", &id,&leftm, &rightm);
			if(res != 3){
				return_val= -1;
			}
			else{
				devicep=malloc(sizeof(PairUniDirectionalMotor_struct));
				PairUniDirectionalMotor_struct * device=(PairUniDirectionalMotor_struct *)devicep;

				device->id=id;

				device->left_motor=(UniDirectionalMotor_struct *)getDeviceStruct(leftm);
				device->right_motor=(UniDirectionalMotor_struct *)getDeviceStruct(rightm);
				if(device->left_motor == NULL || device->right_motor ==NULL){
					printf(" Left and Right Motors Must be defined before creating a pair ");
					return -1;
				}

			}
		}
			break;
		case BiDirectionalMotor:
		{
			
			int enablePin;
			int PWM_A;
			int PWM_B;
			int currentSenseA, currentSenseB;
			
			res=fscanf(fin, "%d %d %d %d %d %d", &id, &enablePin, &PWM_A, &PWM_B, &currentSenseA, &currentSenseB);
			if(res != 6){
				return_val= -1;
			}
			else{
				devicep=malloc(sizeof(BiDirectionalMotor_struct));
				BiDirectionalMotor_struct * device=(BiDirectionalMotor_struct *)devicep;

				device->id=id;

				device->isEnabled=LOW_WRITE;
				device->direction=1;
				device->current_speed=0;
				device->target_speed=0;
				device->enablePin=enablePin;
				device->PWM_A=PWM_A;
				device->PWM_B=PWM_B;
				device->currentSenseA=currentSenseA;
				device->currentSenseB=currentSenseB;
			}

		}
			break;
		case UniDirectionalMotor:
		{

			int enablePin;
			int PWM_A;
			int currentSense;

			res=fscanf(fin, "%d %d %d %d", &id, &enablePin, &PWM_A, &currentSense);
			if(res != 4){
				return_val= -1;
			}
			else{
				devicep=malloc(sizeof(UniDirectionalMotor_struct));
				UniDirectionalMotor_struct * device=(UniDirectionalMotor_struct *)devicep;

				device->id=id;

				device->isEnabled=LOW_WRITE;
				device->current_speed=0;
				device->target_speed=0;

				device->enablePin=enablePin;
				device->PWM_A=PWM_A;
				device->currentSense=currentSense;
			}
			
			
		}
			break;
		case StepperMotor:
		{
			int stepsPerRevolution;
			int num_wires;

			res=fscanf(fin, "%d %d %d", &id, &stepsPerRevolution, &num_wires);
			if(res != 3) {
				return_val= -1;
			}
			else{
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
					if(res != 1){
						return_val= -1;
						break;
					}
					device->pins[i]=pin;
				}
			}
			
		}
			break;
			
		default:
		{
			return_val= -1;
		}
			break;

	}

	res=fscanf(fin, "\n");
	
	res=cache_device(id, devicep);
	if(return_val==-1){
		printf("Error: Device ID is already in use on line: ");
		return -1;
	}

	
	return 0;
}

int cache_device(int key, void * value){
	if(key== -1 ) return -1;
	
	//return 1 if success, -1 if failer
	if(device_cache->count(key)==0){
		device_cache->insert ( std::pair<int,void *>(key,value) );
		return 1;
	}
	else{
		return -1;
	}
};

void *getDeviceStruct(int id){
	//Todo implement a hashtable that stores ids as keys and values as the pointer to the state structs.
	std::map<int,void *>::iterator it;
	it=device_cache->find(id);

	if(it==device_cache->end()){
#ifdef DEBUG
		printf("Error: No Device found with ID: %d\n",id);
#endif
		assert(false);
		return NULL;
	}

	return it->second;
};



int main(int argc, const char * argv[])
{
	//TODO ad simulation code here
	return 0;
}

int listen_for_commands(){

	while(true){
		char deviceType=getNextByte();
		int deviceCount=getNextByte();


		switch(deviceType){
			case BiDirectionalMotor:
				process_BiDirectionalMotor(deviceCount);
				break;
			case UniDirectionalMotor:
				process_UniDirectionalMotor(deviceCount);
				break;
			case StepperMotor:
				process_StepperMotor(deviceCount);
				break;
			default:
			{
				printf("Unknown Command, please review sent log\n");
			}

		}

	}

}

char getNextByte(){
	//TODO add input stream reading here
	return 1;

};

/**
Count 'n' is passed in
 Followed by operation
	Follwed by 'n' id and operand pairs 

 **/

void process_BiDirectionalMotor(int n){
	/* OPCODE [ID Operand]^N
		[setEnabled [ID [enable | disable] ]^N | [Direction [ID [forward | reverse] ]^N ] | [Speed [ID Value] ]^N

	 setEnabled = 0, setDirection=-1/1 */
	enum OPERATION{SET_ENABLE,  SET_DIRECTION, SET_SPEED};
	char opcode=getNextByte();
	
	std::pair<BiDirectionalMotor_struct *, int> *ids=(std::pair<BiDirectionalMotor_struct *, int> *)malloc(sizeof(std::pair<BiDirectionalMotor_struct *, int> )*n);

	for(int i=0; i<n; i++){
		char id=getNextByte();
		char operand=getNextByte();

		ids[i].first=(BiDirectionalMotor_struct *)getDeviceStruct(id);;
		ids[i].second=operand;

		if(ids[i].first==NULL){
			printf("Device: %d was not found, item %d in last commnad\n",id,i);
			return;
		}
	}

	switch(opcode){
		case SET_ENABLE:
		{
			//First Stop All Motors.
			for(int i=0; i<n; i++){
				BiDirectionalMotor_struct *device=ids[i].first;
				device->target_speed=0;
			}
			bool allstopped;
			do{
				allstopped=true;
				for(int i=0; i<n; i++){
					BiDirectionalMotor_struct *device=ids[i].first;
					if(device->target_speed ==device ->current_speed){
						continue;
					}
					int new_speed=device->current_speed-ADJUST_DUTY_BY_VALUE;
					if(new_speed < 0) new_speed=0;
					
					//TODO: Send decremented value to pwm

					device->current_speed=new_speed;


					if(device->target_speed !=device ->current_speed){
						allstopped=false;
					}
				}

				//TODO PUT A SLEEP HERE

			}while(!allstopped);

			for(int i=0; i<n; i++){
				BiDirectionalMotor_struct *device=ids[i].first;
				int operand=ids[i].second;

				if(operand !=ENABLED ||  operand!=DISABLED){
					printf("Unrecognized operend code [%d]  SET_ENABLE for BiDirectionalMotor %d element\n",operand,i);
					return;
				}

				//TODO Write enable signal
				device->isEnabled=operand;

				
			}

		}//End Case Set_Enable
			break;

		case SET_DIRECTION:
		{

			//First Stop All Motors.
			for(int i=0; i<n; i++){
				BiDirectionalMotor_struct *device=ids[i].first;
				device->target_speed=0;
			}
			bool allstopped;
			do{
				allstopped=true;
				for(int i=0; i<n; i++){
					BiDirectionalMotor_struct *device=ids[i].first;
					if(device->target_speed ==device ->current_speed){
						continue;
					}
					int new_speed=device->current_speed-ADJUST_DUTY_BY_VALUE;
					if(new_speed < 0) new_speed=0;

					//TODO: Send new_speed value to pwmA

					device->current_speed=new_speed;


					if(device->target_speed !=device ->current_speed){
						allstopped=false;
					}
				}

				//TODO PUT A SLEEP HERE
			}while(!allstopped);

			for(int i=0; i<n; i++){
				BiDirectionalMotor_struct *device=ids[i].first;
				int operand=ids[i].second;

				const int forward=0; const int reverse=1;
				if(operand != forward || operand != reverse){
					printf("Unrecognized operend code [%d]  SET_DIRECTION for BiDirectionalMotor %d element\n", operand, i);
					return;
				}

				if(device->direction == operand){
					continue;
				}
				else{
					//Reverse the pins
					int temp=device->PWM_A;
					device->PWM_A=device->PWM_B;
					device->PWM_B=temp;
					device->direction=operand;
				}
				
			}

		}//End Case Set_Direction
			break;

		case SET_SPEED:
		{
			for(int i=0; i<n; i++){
				BiDirectionalMotor_struct *device=ids[i].first;
				device->target_speed=ids[i].second;
			}
			bool allToSpeed;
			do{
				allToSpeed=true;
				for(int i=0; i<n; i++){
					BiDirectionalMotor_struct *device=ids[i].first;
					int new_speed;

					if(device->target_speed  < device ->current_speed){
						new_speed=device->current_speed-ADJUST_DUTY_BY_VALUE;

						if(new_speed < device->target_speed){
							new_speed=device->target_speed;
						}
					}
					else if(device->target_speed > device-> current_speed){
						new_speed=device->current_speed+ADJUST_DUTY_BY_VALUE;

						if(new_speed > device->target_speed){
							new_speed=device->target_speed;
						}
					}
					else{
						continue;
					}
					

					//TODO: Send decremented value to pwm

					device->current_speed=new_speed;


					if(device->target_speed !=device ->current_speed){
						allToSpeed=false;
					}
				}

				//TODO PUT A SLEEP HERE
			}while(!allToSpeed);

		}//End Case Set_Speed
			break;

		default:
		{
			printf("Unrecognized operation code for BiDirectionalMotor\n");
			return;

		}//End Case Defualt

	}//End Switch

};

void process_UniDirectionalMotor(int n){
	/* OPCODE [ID Operand]^N
	 [setEnabled [ID [enable | disable] ]^N  | [Speed [ID Value] ]^N

	 setEnabled = 0, setDirection=-1/1 
	 */

	enum OPERATION{	SET_ENABLE,  SET_DIRECTION, SET_SPEED	};
	char opcode=getNextByte();

	std::pair<UniDirectionalMotor_struct *, int> *ids=(std::pair<UniDirectionalMotor_struct *, int> *)malloc(sizeof(std::pair<UniDirectionalMotor_struct *, int> )*n);

	for(int i=0; i<n; i++){
		char id=getNextByte();
		char operand=getNextByte();

		ids[i].first=(UniDirectionalMotor_struct *)getDeviceStruct(id);;
		ids[i].second=operand;

		if(ids[i].first==NULL){
			printf("Device: %d was not found, item %d in last commnad\n",id,i);
			return;
		}
	}

	switch(opcode){
		case SET_ENABLE:
		{
			//First Stop All Motors.
			for(int i=0; i<n; i++){
				UniDirectionalMotor_struct *device=ids[i].first;
				device->target_speed=0;
			}
			bool allstopped;
			do{
				allstopped=true;
				for(int i=0; i<n; i++){
					UniDirectionalMotor_struct *device=ids[i].first;
					if(device->target_speed ==device ->current_speed){
						continue;
					}
					int new_speed=device->current_speed-ADJUST_DUTY_BY_VALUE;
					if(new_speed < 0) new_speed=0;

					//TODO: Send decremented value to pwm

					device->current_speed=new_speed;


					if(device->target_speed !=device ->current_speed){
						allstopped=false;
					}
				}

				//TODO PUT A SLEEP HERE
			}while(!allstopped);

			for(int i=0; i<n; i++){
				UniDirectionalMotor_struct *device=ids[i].first;
				int operand=ids[i].second;

				if(operand !=ENABLED ||  operand!=DISABLED){
					printf("Unrecognized operend code [%d]  SET_ENABLE for BiDirectionalMotor %d element\n",operand,i);
					return;
				}

				//TODO Write enable signal
				device->isEnabled=operand;


			}

		}//End Case Set_Enable
			break;

		

		case SET_SPEED:
			{
				//First Stop All Motors.
				for(int i=0; i<n; i++){
					UniDirectionalMotor_struct *device=ids[i].first;
					device->target_speed=ids[i].second;
				}
				bool allToSpeed;
				do{
					allToSpeed=true;
					for(int i=0; i<n; i++){
						UniDirectionalMotor_struct *device=ids[i].first;
						int new_speed;

						if(device->target_speed  < device ->current_speed){
							new_speed=device->current_speed-ADJUST_DUTY_BY_VALUE;

							if(new_speed < device->target_speed){
								new_speed=device->target_speed;
							}
						}
						else if(device->target_speed > device-> current_speed){
							new_speed=device->current_speed+ADJUST_DUTY_BY_VALUE;

							if(new_speed > device->target_speed){
								new_speed=device->target_speed;
							}
						}
						else{
							continue;
						}


						//TODO: Send decremented value to pwm

						device->current_speed=new_speed;


						if(device->target_speed !=device ->current_speed){
							allToSpeed=false;
						}
					}

					//TODO PUT A SLEEP HERE

				}while(!allToSpeed);
				
			}//End Case Set_Speed
			break;
			
		default:
			{
				printf("Unrecognized operation code for BiDirectionalMotor\n");
				return;
				
				break;
			}//End Case Defualt
			
	}//End Switch
};



void process_StepperMotor(int n){
	/** [ID Direction Steps] **/
	/** Direction 1 or -1  (two commands so that the steps can be up 256, instead of 128**/
	char id=getNextByte();
	char dir=getNextByte();
	char steps=getNextByte();

	
	StepperMotor_struct * device=(StepperMotor_struct *)getDeviceStruct(id);
	if(device==NULL){
		return;
	}

	//TODO figure out wrapper for stepper
	

	device->step=device->step+ (dir*steps);


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
			//sleep(DELAY);
		}
		if(i!=new_value){
			pin_write_wrapper(pin, new_value);
			//sleep(DELAY);
		}

	}
	else{
		int i;
		for(i=current_value; i > new_value; i=i-increment_by ){
			pin_write_wrapper(pin, i);
			//sleep(DELAY);
		}
		if(i!=new_value){
			pin_write_wrapper(pin, new_value);
			//sleep(DELAY);
		}
	}

}
						  
