void setup(){
  Serial.begin(9600);
  //  Serial.println("Commencing");
  //  Serial.println(OUTPUT); 1
  //  Serial.println(INPUT); 0
  //  Serial.println(INPUT_PULLUP); 2
  //  Serial.println(PWM_MODE); 3
  //  Serial.println(HIGH); 1
  //  Serial.println(LOW); 0

    Serial.write(1);
}
int PWM_MODE=3;

int op; 
int pin; 
int value;
void loop(){
  while (Serial.available()) {
    // get the new byte:
    op = Serial.read(); 
    //Serial.print("Read Op: "); Serial.println(op);
    while (!Serial.available()) {
    }
    pin=Serial.read();
    //  Serial.print("Read Pin: "); Serial.println(pin);
    if(op==0){//Analogwrite
      while (!Serial.available()) {
      }
      value=Serial.read();

      if(value >= 0 && value <= 255){
        analogWrite(pin, value);

        //        Serial.print("===="); 
        //        Serial.print(pin); 
        //        Serial.print(" was set to "); 
        //        Serial.print(value);
        //        Serial.println("====");
        Serial.write(1); 
      }
      else{
        Serial.write(-1); 
      }

    }
    else if(op==3){//digitalwrite
      while (!Serial.available()) {
      }
      value=Serial.read();

      if(value == HIGH || value == LOW){
        digitalWrite(pin, value);  
        Serial.write(1);
      }
      else{
        Serial.write(-1); 
      }
    }
    else if(op==1){//Analogread
      value=analogRead(pin);        
      Serial.write(value);
    }
    else if(op==2){//Digital Read
      value=digitalRead(pin);        
      //Serial.write(value);
    }
    else if(op==4){
      while (!Serial.available()) {
      }
      value=Serial.read();

      if(value == INPUT || value == OUTPUT || value ==INPUT_PULLUP || PWM_MODE){
        //        Serial.print("=##="); 
        //        Serial.print(pin); 
        //        Serial.print(" was set to "); 
        //        Serial.println(value);
        //        Serial.println("=##=");
        if(value==PWM_MODE){
          setPwmFrequency(pin, 8);  // change TimerX divisor to 8 gives 3.9kHz PWM freq Possibley?
          value=OUTPUT;
        }
        pinMode(pin,value);
        Serial.write(1); 
        //Serial.print("Set pin mode "); Serial.print(pin); Serial.print(" "); Serial.println(value);
      }
      else{
        Serial.write(-1); 
      }


    }
    else{
      Serial.write(-1);
    }

  }

}


/*
 * Divides a given PWM pin frequency by a divisor.
 * 
 * The resulting frequency is equal to the base frequency divided by
 * the given divisor:
 *   - Base frequencies:
 *      o The base frequency for pins 3, 9, 10, and 11 is 31250 Hz.
 *      o The base frequency for pins 5 and 6 is 62500 Hz.
 *   - Divisors:
 *      o The divisors available on pins 5, 6, 9 and 10 are: 1, 8, 64,
 *        256, and 1024.
 *      o The divisors available on pins 3 and 11 are: 1, 8, 32, 64,
 *        128, 256, and 1024.
 * 
 * PWM frequencies are tied together in pairs of pins. If one in a
 * pair is changed, the other is also changed to match:
 *   - Pins 5 and 6 are paired (Timer0)
 *   - Pins 9 and 10 are paired (Timer1)
 *   - Pins 3 and 11 are paired (Timer2)
 * 
 * Note that this function will have side effects on anything else
 * that uses timers:
 *   - Changes on pins 5, 6 may cause the delay() and
 *     millis() functions to stop working. Other timing-related
 *     functions may also be affected.
 *   - Changes on pins 9 or 10 will cause the Servo library to function
 *     incorrectly.
 * 
 * Thanks to macegr of the Arduino forums for his documentation of the
 * PWM frequency divisors. His post can be viewed at:
 *   http://www.arduino.cc/cgi-bin/yabb2/YaBB.pl?num=1235060559/0#4
 */

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



