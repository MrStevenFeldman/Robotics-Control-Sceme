/*
 Chat  Server
 
 A simple server that distributes any incoming messages to all
 connected clients.  To use telnet to  your device's IP address and type.
 You can see the client's input in the serial monitor as well.
 
 This example is written for a network using WPA encryption. For 
 WEP or WPA, change the Wifi.begin() call accordingly.
 
 
 Circuit:
 * WiFi shield attached
 
 created 18 Dec 2009
 by David A. Mellis
 modified 31 May 2012
 by Tom Igoe
 
 */

const int duty_incrment=10;
const int MAX_DUTY=255;
boolean enabled=false;
int duty_left;
int EnablePin_left = 8;
int PWMPin1_left = 6;  // Timer2
int PWMPin2_left = 3;

int duty_right;
int EnablePin_right = 8;
int PWMPin1_right = 5;  // Timer2
int PWMPin2_right = 9;

const byte CPin = 0;  // analog input channel
int CRaw;      // raw A/D value
float CVal;    // adjusted Amps value


#include <SPI.h>
#include <WiFi.h>
char ssid[] = "BoomersWIFI";      //  your network SSID (name) 
char pass[] = "YellowDog";   // your network password
int keyIndex = 0;            // your network key Index number (needed only for WEP)


int status = WL_IDLE_STATUS;

WiFiServer server(23);

boolean alreadyConnected = false; // whether or not the client was connected previously

char part_one;
char part_two;


void setup() {
  part_one=0;
  part_two=0;

  //Setup motors:

  //Left Motor
  pinMode(EnablePin_left, OUTPUT);     
  pinMode(PWMPin1_left, OUTPUT);
  pinMode(PWMPin2_left, OUTPUT);
  setPwmFrequency(PWMPin1_left, 8);  // change Timer2 divisor to 8 gives 3.9kHz PWM freq
  analogWrite(PWMPin1_left, 0);
  analogWrite(PWMPin2_left, 0);

  //Right Motor
  pinMode(EnablePin_right, OUTPUT);     
  pinMode(PWMPin1_right, OUTPUT);
  pinMode(PWMPin2_right, OUTPUT);
  setPwmFrequency(PWMPin1_right, 8);  // change Timer2 divisor to 8 gives 3.9kHz PWM freq
  analogWrite(PWMPin2_right, 0);
  analogWrite(PWMPin1_right, 0);

  //Initialize serial and wait for port to open:
  Serial.begin(9600); 
  while (!Serial) {
    ; // wait for serial port to connect. Needed for Leonardo only
  }

  // check for the presence of the shield:
  if (WiFi.status() == WL_NO_SHIELD) {
    Serial.println("WiFi shield not present"); 
    // don't continue:
    while(true);
  } 

  // attempt to connect to Wifi network:
  while ( status != WL_CONNECTED) { 
    Serial.print("Attempting to connect to SSID: ");
    Serial.println(ssid);
    // Connect to WPA/WPA2 network. Change this line if using open or WEP network:    
    status = WiFi.begin(ssid, pass);

    // wait 10 seconds for connection:
    delay(10000);
  } 
  // start the server:
  server.begin();
  // you're connected now, so print out the status:
  printWifiStatus();



}


void loop() {
  // wait for a new client:
  WiFiClient client = server.available();


  // when the client sends the first byte, say hello:
  if (client) {
    if (!alreadyConnected) {
      // clear out the input buffer:
      client.flush();    
      Serial.println("DALEK COMMANDER PRESENT!");
      client.println("Hello, DAVROS!"); 
      client.print(PWMPin1_right);
      client.print(" ");
      client.print( PWMPin2_right);
      client.print( " ");
      client.print(PWMPin1_left);
      client.print( " ") ;
      client.println(PWMPin2_left);
      alreadyConnected = true;
    } 
    //Commands
    //E : Enable Motors
    //D : Disable Motors
    //B[U|D|R|S] : Both  Motors [Up | Down | Reverse | Stop]
    //R[U|D|R|S] : Right Motors [Up | Down | Reverse | Stop]
    //L[U|D|R|S] : Left  Motors [Up | Down | Reverse | Stop]
    // part_one=null;
    //part_two=null
    if (client.available() > 0) {
      char v = client.read(); 

      if(part_one==0){
        if(v=='E' || v=='D' || v=='B' || v=='R' || v=='L')
          part_one=v;
      }
      else if(part_two==0){
        part_two=v;
      }
      else{
        //Throw error
        Serial.println("Error Both parts not null");
      }
      Serial.print("[");
      Serial.print(part_one);
      Serial.print(" ");
      Serial.print(part_two);
      Serial.println("]");
      if(part_one=='E' || part_one== 'D' || part_two!=0){
        process_message();

        if(part_two=='R'){
          client.print(PWMPin1_right);
          client.print(" ");
          client.print( PWMPin2_right);
          client.print( " ");
          client.print(PWMPin1_left);
          client.print( " ") ;
          client.println(PWMPin2_left);
        }
        client.print(duty_right);
        client.print(" ");
        client.print(0);
        client.print(" ");
        client.print(duty_left);
        client.print(" ");
        client.println(0);

        part_one=0;
        part_two=0;
      }
    }//End if cleint availble

  }//End if client
  else{
    alreadyConnected=false; 
  }
}//end loop

void process_message(){
  Serial.print("Processing Message:[");
  Serial.print(part_one);
  Serial.print(part_two);
  Serial.println("]");
  if(part_one=='E'){  
    enable_motors(); 
  }
  else if(part_one=='D'){
    disable_motors(); 
  }
  else{
    adjust_motors();
  }

}//End Process motor

void enable_motors(){
  Serial.println("Enabling");
  enabled=true;
  duty_left=0;
  duty_right=0;
  analogWrite(PWMPin1_right, duty_right);
  analogWrite(PWMPin1_left, duty_left);

  digitalWrite(EnablePin_right, HIGH);
  digitalWrite(EnablePin_left, HIGH);
}

void disable_motors(){
  Serial.println("Stopping");
  enabled=false;
  duty_left=0;
  duty_right=0;
  analogWrite(PWMPin1_right, duty_right);
  analogWrite(PWMPin1_left, duty_left);

  digitalWrite(EnablePin_right, LOW);
  digitalWrite(EnablePin_left, LOW);
  delay(1000);    
}

void adjust_motors(){
  //Up, down, stop, reverse
  if(part_two=='R'){
    reverse_motors(); 
  }
  else if(part_two=='S'){
    stop_motors(); 
  }
  else{//up or down
    if(part_two=='D'){
      if(part_one=='B' || part_one=='R'){
        duty_right=duty_right-duty_incrment;
        if(duty_right<0)
          duty_right=0;

        analogWrite(PWMPin1_right, duty_right);
      }

      if(part_one=='B' || part_one=='L'){
        duty_left=duty_left-duty_incrment;
        if(duty_left<0)
          duty_left=0;

        analogWrite(PWMPin1_left, duty_left);
      }
    }
    else if(part_two=='U'){
      if(part_one=='B' || part_one=='R'){
        duty_right=duty_right+duty_incrment;
        if(duty_right>MAX_DUTY)
          duty_right=MAX_DUTY;

        analogWrite(PWMPin1_right, duty_right);
      }

      if(part_one=='B' || part_one=='L'){
        duty_left=duty_left+duty_incrment;
        if(duty_left>MAX_DUTY)
          duty_left=MAX_DUTY;

        analogWrite(PWMPin1_left, duty_left);
      }
    }
    else{
      Serial.println("Unknown Part Two");
      return;
    }

  }

}

void reverse_motors(){
  Serial.println("Reversing Motor(s)");

  //First Stop the motors
  CRaw = analogRead(CPin);
  delay(1000);
  while(true){
    duty_left=duty_left-duty_incrment;
    duty_right=duty_right-duty_incrment;
    if(duty_left >=0){
      analogWrite(PWMPin1_left, duty_left);
    }

    if(duty_right >=0){
      analogWrite(PWMPin1_right, duty_right);
    }

    if(duty_left <= 0 && duty_right <=0){
      break;
    }


    delay(20);   
  }//End While true

  //Insure dutys at zero
  duty_left=0;
  duty_right=0;

  //Motors should be stopped
  //Disable to be sure
  digitalWrite(EnablePin_right, LOW);
  digitalWrite(EnablePin_left, LOW);

  //Now Swap the motor pins
  if(part_one=='B' || part_one=='L'){
    int temp =PWMPin1_left;
    PWMPin1_left=PWMPin2_left;
    PWMPin2_left=temp;
  }
  if(part_one=='B' || part_one=='R'){
    int temp =PWMPin1_right;
    PWMPin1_right=PWMPin2_right;
    PWMPin2_right=temp;
  }
  //wrire low to the correct pins
  analogWrite(PWMPin2_right, 0);
  analogWrite(PWMPin2_left, 0);

  if(enabled){
    digitalWrite(EnablePin_left, HIGH);
    digitalWrite(EnablePin_right, HIGH);

  } 

}//end reverse motors

void stop_motors(){
  Serial.println("Stopping Motor(s)");

  CRaw = analogRead(CPin);
  delay(20);
  if(part_one=='B' || part_one=='L'){
    duty_left=0;
    analogWrite(PWMPin1_left, duty_left);

  }

  if(part_one=='B' || part_one=='R'){
    duty_right=0;
    analogWrite(PWMPin1_right, duty_right);

  }
}//end stop motors







void printWifiStatus() {
  // print the SSID of the network you're attached to:
  Serial.print("SSID: ");
  Serial.println(WiFi.SSID());

  // print your WiFi shield's IP address:
  IPAddress ip = WiFi.localIP();
  Serial.print("IP Address: ");
  Serial.println(ip);

  // print the received signal strength:
  long rssi = WiFi.RSSI();
  Serial.print("signal strength (RSSI):");
  Serial.print(rssi);
  Serial.println(" dBm");
}

void setPwmFrequency(int pin, int divisor) {
  byte mode;
  if(pin == 5 || pin == 6 || pin == 9 || pin == 10) { // Timer0 or Timer1
    switch(divisor) {
    case 1: 
      mode = 0x01; 
      break;
    case 8: 
      mode = 0x02; 
      break;
    case 64: 
      mode = 0x03; 
      break;
    case 256: 
      mode = 0x04; 
      break;
    case 1024: 
      mode = 0x05; 
      break;
    default: 
      return;
    }
    if(pin == 5 || pin == 6) { 
      TCCR0B = TCCR0B & 0b11111000 | mode; // Timer0
    } 
    else {
      TCCR1B = TCCR1B & 0b11111000 | mode; // Timer1
    }
  } 
  else if(pin == 3 || pin == 11) {
    switch(divisor) {
    case 1: 
      mode = 0x01; 
      break;
    case 8: 
      mode = 0x02; 
      break;
    case 32: 
      mode = 0x03; 
      break;
    case 64: 
      mode = 0x04; 
      break;
    case 128: 
      mode = 0x05; 
      break;
    case 256: 
      mode = 0x06; 
      break;
    case 1024: 
      mode = 0x7; 
      break;
    default: 
      return;
    }
    TCCR2B = TCCR2B & 0b11111000 | mode; // Timer2
  }
}













