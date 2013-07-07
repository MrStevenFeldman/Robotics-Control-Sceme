package dalek.server.contolunits;

import jssc.SerialPort;
import jssc.SerialPortException;

/**
 * This was tested using an aruino unit, but should work with any device that accepts serial communication over usb.
 * @author stevenfeldman
 *
 */

//TODO: Could be an issue with the serial communication/reaing of bytes in this or the arduino script.
public class SerialUSBDriver implements ControlUnit {
	SerialPort serialPort;
	String portname;
	int BAUD_RATE;
	
	/** The port we could use
	private static final String PORT_NAMES[] = { 
			"/dev/tty.usbserial-A9007UX1", // Mac OS X
			"/dev/ttyUSB0", // Linux
			"COM3", // Windows
			"/dev/tty.usbmodem1411", // Mac
			"/dev/tty.usbmodem1421", // Mac
	};
	. 
	 * @throws Exception */
	
	public void initialize() throws Exception {
		
		serialPort = new SerialPort(portname);
        try {
            serialPort.openPort();//Open serial port
            serialPort.setParams(BAUD_RATE, 
                                 SerialPort.DATABITS_8,
                                 SerialPort.STOPBITS_1,
                                 SerialPort.PARITY_NONE);//Set params. Also you can set params by this string: serialPort.setParams(9600, 8, 1, 0);
            byte [] res=serialPort.readBytes(1,3000);
            if(res[0]==1){
            	return;
            }
            else{
            	throw new Exception("Serial Device Return non 1 value\n");
            }
        }
        catch (SerialPortException ex) {
            System.out.println(ex);
            throw ex;
        }
        
        
		
	}

	/**
	 * This should be called when you stop using the port.
	 * This will prevent port locking on platforms like Linux.
	 * @throws SerialPortException 
	 */
	public synchronized void close() throws SerialPortException {
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.closePort();
		}
	}
	

		
	
	public SerialUSBDriver(String portname, int BAUD_RATE) throws Exception{
		this.BAUD_RATE=BAUD_RATE;
		this.portname=portname;
		this.initialize();
	}
	
	//Communication with Arduino:
	
	//Digitial I/O
			//(4)pinMode(pin, mode)	Mode: Input (0), output (1), or Input_PullUp (2) or PWM_MODE (3)
			//(3)digitalWrite(pin, value)	Value: HIGH(1) or LOW (0)
			//(2)digitalRead(pin)
		//Analog I/O
			//(0) analogWrite(PWM)	(pin, value) 0 to 255 inclusive		
			//(1)analogRead(pin
			
	public void init_pwm(int pinA, int pinB) throws Exception{
		init_pwm(pinA);
		init_pwm(pinB);
		
		return;

	}

	public void init_pwm(int pin) throws Exception{
		serialPort.writeByte((byte)4);
		serialPort.writeByte((byte)pin);
		serialPort.writeByte((byte)3);
		
		byte [] res=serialPort.readBytes(1,3000);
		int res1=res[0];
		if(res1== -1){
			throw new Exception(String.format("Error Updating PWM for %d pin on SerialUSBDriver\n",pin));
		}
		

	}
	public void set_pwm_duty(int pin, int duty) throws Exception{
		
		if(duty < 0 || duty > 255){
			throw new Exception(String.format("Error  PWM duty[%d] for %d  pin on SerialUSBDriver is not a proper value\n",duty, pin));

		}
		serialPort.writeByte((byte)0);
		serialPort.writeByte((byte)pin);
		serialPort.writeByte((byte)duty);
		byte [] res=serialPort.readBytes(1,3000);
		int res1=res[0];
		if(res1== -1){
			throw new Exception(String.format("Error Updating PWM duty[%d] for %d pin on SerialUSBDriver\n",duty, pin));
		}
		

	}
	
	public void init_gpio_pin_output(int pin) throws Exception{
		serialPort.writeByte((byte)4);
		serialPort.writeByte((byte)pin);
		serialPort.writeByte((byte)1);
		
		byte [] res=serialPort.readBytes(1,3000);
		int res1=res[0];
		if(res1== -1){
			throw new Exception(String.format("Error Updating Pin %d for output on SerialUSBDriver\n",pin));
		}
		
		

	}
	
	public void init_gpio_pin_input(int pin) throws Exception{
		serialPort.writeByte((byte)4);
		serialPort.writeByte((byte)pin);
		serialPort.writeByte((byte)0);
		
		byte [] res=serialPort.readBytes(1,3000);
		int res1=res[0];
		if(res1== -1){
			throw new Exception(String.format("Error Updating Pin %d for input on SerialUSBDriver\n",pin));
		}
		

	}
	public int analog_read_pin(int pin) throws Exception{
		serialPort.writeByte((byte)1);
		serialPort.writeByte((byte)pin);
		
		byte [] res=serialPort.readBytes(1,3000);
		
		return res[0];
		

	}
	public int digital_read_pin(int pin) throws Exception{
		serialPort.writeByte((byte)2);
		serialPort.writeByte((byte)pin);

		byte [] res=serialPort.readBytes(1,3000);
		return res[0];
		

	}
	
	public void analog_write_pin(int pin, int value) throws Exception{
		
		serialPort.writeByte((byte)0);
		serialPort.writeByte((byte)pin);
		serialPort.writeByte((byte)value);
		
		byte [] res=serialPort.readBytes(1,3000);
		int res1=res[0];
		if(res1== -1){
			throw new Exception(String.format("Error Analog Write Pin %d [value %d] for input on SerialUSBDriver\n",pin,value));
		}

	}
	
	public void digital_write_pin(int pin, int value) throws Exception{
		
		serialPort.writeByte((byte)3);
		serialPort.writeByte((byte)pin);
		serialPort.writeByte((byte)value);
		
		byte [] res=serialPort.readBytes(1,3000);
		int res1=res[0];
		if(res1== -1){
			throw new Exception(String.format("Error digital Write Pin %d [value %d] for input on SerialUSBDriver\n",pin,value));
		}

	}



}
