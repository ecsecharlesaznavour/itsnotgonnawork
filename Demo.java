package classes2;

import java.io.IOException;
import java.util.HashMap;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;

public class Demo {
	
	private final static EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	private final static EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	private final static EV3UltrasonicSensor frontSensor = new EV3UltrasonicSensor(LocalEV3.get().getPort("S1"));
	private final static EV3UltrasonicSensor rightSensor = new EV3UltrasonicSensor(LocalEV3.get().getPort("S2"));
	private final static EV3ColorSensor RIGHTSensor = new EV3ColorSensor(LocalEV3.get().getPort("S3"));
	private final static EV3ColorSensor LEFTSensor = new EV3ColorSensor(LocalEV3.get().getPort("S4"));
	private static final String SERVER_IP = "192.168.0.101";
	private static final int TEAM_NUMBER = 8;

	@SuppressWarnings("unused")
	public static void main(String[] args)
	{
		Stopper stop = new Stopper();
		stop.start();
		
		int DTN, DSC, OTN, OSC, w1, d1, d2, llx, lly, urx, ury, BC;

		int mySC, myRole;

		WifiConnection conn = null;


		try {
			conn = new WifiConnection(SERVER_IP, TEAM_NUMBER);
		} catch (IOException e) {
			Button.LEDPattern(2);
		}
		
		if (conn != null){
			HashMap<String,Integer> t = conn.StartData;
			if (t == null) {
				Button.LEDPattern(3);
				while(true);
			} else {
				Button.LEDPattern(1);

					DTN=t.get("DTN");

					DSC=t.get("DSC");

					OTN=t.get("OTN");

					OSC=t.get("OSC");

					w1=t.get("w1");

					d1=t.get("d1");

					d2=t.get("d2");

					llx=t.get("ll-x");

					lly=t.get("ll-y");

					urx=t.get("ur-x");

					ury=t.get("ur-y");

					BC=t.get("BC");

				//determine my role

					if (DTN==TEAM_NUMBER)

					{

						myRole=0;

						mySC=DSC;
					}

					else

					{

						myRole=1;

						mySC=OSC;
						

					}

				// dump on the screen to make sure

				if (myRole==0)
	 
					System.out.print("Defender in corner "+mySC);

				else

					System.out.print("Offense in corner "+mySC);


			}
		} else {
			Button.LEDPattern(4);
			while(true);
		}
		
		Odometer odo = new Odometer(leftMotor, rightMotor, 30, true);
		DualOdometryCorrection odoCor = new DualOdometryCorrection(LEFTSensor, RIGHTSensor, leftMotor, rightMotor, odo);
		
		MasterBrick MB = new MasterBrick(leftMotor, rightMotor, frontSensor, rightSensor, odo, odoCor);
		
		odoCor.start();
		
		MB.doLocalization();
		MB.travelTo(60, 60);
		
		System.exit(0);
	}
}

class Stopper extends Thread
{
	public void run()
	{
		int option = 0;
		while(option != Button.ID_ESCAPE)
			option = Button.waitForAnyPress();
		
		System.exit(0);
	}
}
