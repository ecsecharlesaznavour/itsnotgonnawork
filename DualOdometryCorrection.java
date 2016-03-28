package classes2;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.SampleProvider;

public class DualOdometryCorrection extends Thread{

	private EV3ColorSensor rightSensor, leftSensor;
	private SampleProvider Csp1, Csp2;
	private float[] datas1, datas2;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private boolean black1, black2, allowAng = false;
	private Odometer odometer;
	
	/**
	 * Default Constructor for the DualOdometryCorrection class.
	 * @param Csp1 SampleProvider for the right ColorSensor.
	 * @param sp2 SampleProvider for the left ColorSensor.
	 * @param leftMotor Left Motor of the robot.
	 * @param rightMotor Right Motor of the robot.
	 * @param odometer Odometer for the robot.
	 */
	public DualOdometryCorrection(EV3ColorSensor leftSensor, EV3ColorSensor rightSensor,
			EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
			Odometer odometer)
	{
		this.rightSensor = rightSensor;
		this.leftSensor = leftSensor;
		this.Csp1 = this.rightSensor.getRedMode();
		this.Csp2 = this.leftSensor.getRedMode();
		this.datas1 = new float[Csp1.sampleSize()];
		this.datas2 = new float[Csp2.sampleSize()];
	}
	
	/**
	 * Run method for the DualOdometryCorrection thread. Responsible for handling the data returned by the sensors.
	 */
	public void run()
	{
		float data1 = getFilteredData(Csp1, datas1, -1);
		float data2 = getFilteredData(Csp2, datas2, -1);
		while(true)
		{
			float data3 = getFilteredData(Csp1, datas1, data1);
			float data4 = getFilteredData(Csp2, datas2, data2);
			if(Math.abs(data1 - data3) > 0.15)
				black1 = !black1;
			if(Math.abs(data2 - data4) > 0.15)
				black2 = !black2;
			
			if(black1 && !black2)
			{
				if(allowAng)
				{
					leftMotor.stop();
					while(black2 != black1 )
					{
						data2 = data4;
						data4 = getFilteredData(Csp2, datas2, data2);
						black2 = ((data2 - data4) > 0.15);
					}
					leftMotor.forward();
					correctOdo(10);
					try{Thread.sleep(500);}catch(Exception e){}
					allowAng = false;
				}
			} else if(black2 && !black1)
			{
				if(allowAng)
				{
					rightMotor.stop();
					while(black2 != black1)
					{
						data1 = data3;
						data3 = getFilteredData(Csp1, datas1, data1);
						black1 = ((data1 - data3) > 0.15);
					}
					rightMotor.forward();
					correctOdo(10);
					try{Thread.sleep(500);}catch(Exception e){}
					allowAng = false;
				}
			} else if(black1 || black2)
			{
				correctOdo(10);
				try{Thread.sleep(500);}catch(Exception e){}
			}
			
			
			data1 = data3;
			data2 = data4;
			
		}
	}
	
	/**
	 * Corrects the angle and position of the Odometer under a certain threshold.
	 * @param Tthreshold Threshold for the angle to correct.
	 */
	public void correctOdo(double Tthreshold)
	{
		double theta = odometer.getAng();
		double x1 = theta - 90;
		double x2 = theta - 180;
		double x3 = theta - 270;
		if(Math.abs(x1) < Tthreshold)
		{
			odometer.setAng(90);
			float Y = (float) odometer.getY();
			int factor = Math.round(Y/30);
			odometer.setY(factor*30);
		}
		else if(Math.abs(x2) < Tthreshold)
		{
			odometer.setAng(180);
			float X = (float) odometer.getX();
			int factor = Math.round(X/30);
			odometer.setX(factor*30);
		}
		else if(Math.abs(x3) < Tthreshold)
		{
			odometer.setAng(270);
			float Y = (float) odometer.getY();
			int factor = Math.round(Y/30);
			odometer.setY(factor*30);
		}
		else 
		{
			odometer.setAng(0);
			float X = (float) odometer.getX();
			int factor = Math.round(X/30);
			odometer.setX(factor*30);
		}
	}
	
	/**
	 * Filters the data returned by the desired Sensor.
	 * @param sp SampleProvider of the Sensor.
	 * @param datas Data array of the Sensor.
	 * @param lData Last Data of the Sensor.
	 * @return Filtered Data detected by the Sensor.
	 */
	public float getFilteredData(SampleProvider sp, float[] datas, float lData)
	{
		int Filter = 0;
		sp.fetchSample(datas, 0);
		float data = datas[0]*100;
		
		if(lData < 0)
			lData = data;
		if(Math.abs(lData - data) > 0.15)
		{
			while(Filter<15)
			{
				sp.fetchSample(datas, 0);
				data = datas[0]*100;
				if(Math.abs(lData - data) > 0.15)
					Filter++;
				else
					break;
			}
		}
		return data;
	}
	
	public void CSEnable(EV3ColorSensor sensor)
	{
		synchronized(this)
		{
			sensor.setFloodlight(true);
		}
	}
	
	public void CSDisable(EV3ColorSensor sensor)
	{
		synchronized(this)
		{
			sensor.setFloodlight(false);
		}
	}
	
	public void setAllow(boolean state)
	{
		synchronized(this)
		{
			this.allowAng = state;
		}
	}

}
