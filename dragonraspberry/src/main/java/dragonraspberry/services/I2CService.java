package dragonraspberry.services;

import java.io.IOException;
import org.apache.log4j.Logger;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

public class I2CService {

	private Logger log = Logger.getLogger(this.getClass().getSimpleName());
	private final static int PCAADDR = 0x40;
	private final static int MODE1 = 0x00;
	private final static int PRESCALE = 0xFE;
	private final static int SLEEP = 0b00010000;
	private final static int AI = 0b00100000;
	private final static int LEDBASE = 0x06;
	private final static int[] LEDBASELIST = { 6, 10, 14, 18, 22, 26, 30, 34, 38, 42, 46, 50, 54, 58, 62, 66 };
	private final static int[] FULLZERO={0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	private I2CDevice i2cdev;
	private I2CBus i2cbus;
	private boolean demoMode=false;
	
	public I2CService() {
		log.info("Make I2CService");
	}
	
	public void init(int frequency)  {
		try {
			log.info("Init the PCA9685");
			
			i2cbus = I2CFactory.getInstance(I2CBus.BUS_1);
			i2cdev = i2cbus.getDevice(PCAADDR);

			int settings_mode1 = i2cdev.read(MODE1);
			log.info("Current settings MODE1 is B" + Integer.toBinaryString(settings_mode1));

			settings_mode1 = settings_mode1 & 0xEF | AI;
			log.info("Enable auto increment B" + Integer.toBinaryString(settings_mode1));
			i2cdev.write(MODE1, (byte) settings_mode1);

			setFrequency(frequency);
			log.info("Init done");
		} catch (UnsupportedBusNumberException e) {
			log.info("UnsupportedBusNumberException switch to demo mode");
			demoMode=true;
		} catch (IOException e) {
			log.error("IO Exception "+e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void setFrequency(int frequency) throws IOException {
		
		log.info("Set the frequencyof the  PCA9685 on " + frequency + "Hz");
		int prescale = (25_000_000 / (4096 * frequency)) - 1;
		log.info("Prescale set on " + prescale);
		if(demoMode)return;
		int settings_mode1 = i2cdev.read(MODE1);
		i2cdev.write(MODE1, (byte) (settings_mode1 | SLEEP));
		i2cdev.write(PRESCALE, (byte) prescale);
		i2cdev.write(MODE1, (byte) (settings_mode1 & 0xEF));
		wacht(500);
	}
	
	public void reset() throws IOException {
		log.info("Reset the PCA9685");
		if(demoMode)return;
		int settings_mode1 = i2cdev.read(MODE1);
		i2cdev.write(MODE1, (byte) (settings_mode1 | 0x80));
		writeAllLeds(FULLZERO);
	}
	
	public void writeByteArray(byte[] data) throws IOException {
		if(demoMode)return;
		i2cdev.write(LEDBASE, data);
	}

	public void writeAllLeds(int[] valueList) throws IOException {
		byte[] byteValueList = new byte[valueList.length * 4];
		for (int tel = 0; tel < valueList.length; tel++) {
			byte[] result = intToBytes(valueList[tel]);
			byteValueList[tel * 4] = result[0];
			byteValueList[1 + tel * 4] = result[1];
			byteValueList[2 + tel * 4] = result[2];
			byteValueList[3 + tel * 4] = result[3];
		}
		if(demoMode)return;
		i2cdev.write(LEDBASE, byteValueList);
	}
	
	private void wacht(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private static byte[] intToBytes(final int data) {
		return new byte[] { (byte) ((data >> 16) & 0xff), 
							(byte) ((data >> 24) & 0xff), 
							(byte) ((data >> 0) & 0xff),
							(byte) ((data >> 8) & 0xff), };
	}

	public void writeLedString(int valueList[]) throws IOException
	{
		writeAllLeds(valueList);
	}

}
