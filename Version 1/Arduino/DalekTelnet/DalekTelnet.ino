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
char ssid[] = "BlackMesa";      //  your network SSID (name) 
char pass[] = "E04368fa75";   // your network password
int keyIndex = 0;            // your network key Index number (needed only for WEP)


int status = WL_IDLE_STATUS;

WiFiServer server(23);

boolean alreadyConnected = false; // whether or not the client was connected previously

void setup() {
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
    status = WiFi.begin(ssid,0, pass);

    // wait 10 seconds for connection:
    delay(10000);
  } 
  // start the server:
  server.begin();
  // you're connected now, so print out the status:
  printWifiStatus();



}

char side;
char dir;

void loop() {
  // wait for a new client:
  WiFiClient client = server.available();


  // when the client sends the first byte, say hello:
  if (client) {
    if (!alreadyConnected) {
      // clead out the input buffer:
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

    char v;
    while(true){
      if (client.available() > 0) {
        v = client.read();
        //   Serial.println(side);
      }//End Availble
      if(v!='R' && v!='L'  && v!='B' && v!='B' && v!='A'){
        continue;
      }
      else if(v !='A'){
        side=v;
        break;
      }
      else{
        break;
      }
    }


    if(v!='A'){
      while(true){
        if (client.available() > 0) {
          dir = client.read();
          //   Serial.println(dir);

        }//End Availble
        if(dir!='U' && dir!='D'  && dir!='E' && dir!='S' && dir!='R'){
          continue;
        }
        else{
          break;
        }
      }
    }


    if(dir=='E' || dir=='S'){
      side='B'; 
    }
    else if( dir!='R'){
      if(!enabled){
        Serial.println("Attempted to Modify Speed when not enabled");
        return;
      } 
    }

    Serial.print("[");
    Serial.print(side);
    Serial.print(" ");
    Serial.print(dir);
    Serial.println("]");


    if(side=='R'){
      if(dir=='D'){
        duty_right=duty_right-duty_incrment;
      }
      else if(dir=='R'){
        Serial.println("Reversing");

        CRaw = analogRead(CPin);
        delay(2000);
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
        duty_left=0;
        duty_right=0;
        //Motors should be stopped
        digitalWrite(EnablePin_right, LOW);
        digitalWrite(EnablePin_left, LOW);
        int temp=PWMPin1_right;
        PWMPin1_right=PWMPin2_right;
        PWMPin2_right=temp;


        analogWrite(PWMPin2_right, 0);
        analogWrite(PWMPin2_left, 0);

        if(enabled){
          digitalWrite(EnablePin_right, HIGH);
          digitalWrite(EnablePin_left, HIGH);
        } 



      }//End Reverse

      else{
        duty_right=duty_right+duty_incrment; 
      }
      if(duty_right<0)
        duty_right=0;
      if(duty_right>MAX_DUTY)
        duty_right=MAX_DUTY;

      analogWrite(PWMPin1_right, duty_right);

    }
    else if(side=='L'){
      if(dir=='D'){
        duty_left=duty_left-duty_incrment;
      }
      else if(dir=='R'){
        Serial.println("Reversing Right");

        CRaw = analogRead(CPin);
        delay(2000);
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
        duty_left=0;
        duty_right=0;
        //Motors should be stopped
        digitalWrite(EnablePin_right, LOW);
        digitalWrite(EnablePin_left, LOW);
        int temp =PWMPin1_left;
        PWMPin1_left=PWMPin2_left;
        PWMPin2_left=temp;

        analogWrite(PWMPin2_right, 0);
        analogWrite(PWMPin2_left, 0);

        if(enabled){
          digitalWrite(EnablePin_left, HIGH);
          digitalWrite(EnablePin_right, HIGH);

        } 



      }//End Reverse

      else{
        duty_left=duty_left+duty_incrment;
      }

      if(duty_left<0)
        duty_left=0;
      if(duty_left>MAX_DUTY)
        duty_left=MAX_DUTY;

      analogWrite(PWMPin1_left, duty_left);

    }

    else{
      if(dir=='S'){
        Serial.println("Stopping");
        enabled=false;
        duty_left=0;
        duty_right=0;
        analogWrite(PWMPin1_right, duty_right);
        analogWrite(PWMPin1_left, duty_left);

        digitalWrite(EnablePin_right, LOW);
        digitalWrite(EnablePin_left, LOW);
        delay(2000);   
      }
      else if(dir=='E'){
        Serial.println("Enabling");
        enabled=true;
        duty_left=0;
        duty_right=0;
        analogWrite(PWMPin1_right, duty_right);
        analogWrite(PWMPin1_left, duty_left);

        digitalWrite(EnablePin_right, HIGH);
        digitalWrite(EnablePin_left, HIGH);
      }
      else if(dir=='R'){
        Serial.println("Reversing");

        CRaw = analogRead(CPin);
        delay(2000);
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
        duty_left=0;
        duty_right=0;
        //Motors should be stopped
        digitalWrite(EnablePin_right, LOW);
        digitalWrite(EnablePin_left, LOW);
        int temp=PWMPin1_right;
        PWMPin1_right=PWMPin2_right;
        PWMPin2_right=temp;

        temp=PWMPin1_left;
        PWMPin1_left=PWMPin2_left;
        PWMPin2_left=temp;

        analogWrite(PWMPin2_right, 0);
        analogWrite(PWMPin2_left, 0);

        if(enabled){
          digitalWrite(EnablePin_right, HIGH);
          digitalWrite(EnablePin_left, HIGH);
        } 



      }//End Reverse
      else{
        if(dir=='D'){
          duty_left=duty_left-duty_incrment;
          duty_right=duty_right-duty_incrment;
        }
        else{
          duty_left=duty_left+duty_incrment;
          duty_right=duty_right+duty_incrment; 
        }

        if(duty_left<0)
          duty_left=0;
        if(duty_left>MAX_DUTY)
          duty_left=MAX_DUTY;

        if(duty_right<0)
          duty_right=0;
        if(duty_right>MAX_DUTY)
          duty_right=MAX_DUTY;

        analogWrite(PWMPin1_left, duty_left);
        analogWrite(PWMPin1_right, duty_right);

      }

    }//End else both


    Serial.print("Left:");
    Serial.print("Duty: ");
    Serial.print(duty_left);
    Serial.print(" Pin1: ");
    Serial.print(PWMPin1_left);
    Serial.print(" Pin2: ");
    Serial.println(PWMPin2_left);

    Serial.print("Right:");
    Serial.print("Duty: ");
    Serial.print(duty_right);
    Serial.print(" Pin1: ");
    Serial.print(PWMPin1_right);
    Serial.print(" Pin2: ");
    Serial.println(PWMPin2_right);

    delay(100);

    client.print(PWMPin1_right);
    client.print(" ");
    client.print(duty_right);
    client.print(" ");
    client.print(PWMPin1_left);
    client.print(" ");
    client.println(duty_left);

  }//end Client

}//end loop function


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








