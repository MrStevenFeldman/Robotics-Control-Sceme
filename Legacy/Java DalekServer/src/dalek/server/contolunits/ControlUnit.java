package dalek.server.contolunits;

public interface ControlUnit {
	public void init_pwm(int pinA, int pinB) throws Exception;
	public void init_pwm(int pinA) throws Exception;
	public void set_pwm_duty(int pinA, int duty) throws Exception;
	public void init_gpio_pin_input(int pin) throws Exception;
	public void init_gpio_pin_output(int pin) throws Exception;
	public int analog_read_pin(int pin) throws Exception;
	public int digital_read_pin(int pin) throws Exception;
	public void analog_write_pin(int pin, int value) throws Exception;
	public void digital_write_pin(int pin, int value) throws Exception;
}
