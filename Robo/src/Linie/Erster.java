package Linie;

import java.rmi.Remote;
import java.rmi.RemoteException;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.remote.ev3.RMIRegulatedMotor;
import lejos.remote.ev3.RMISampleProvider;
import lejos.remote.ev3.RemoteEV3;
import lejos.robotics.SampleProvider;
import lejos.hardware.*;
import lejos.hardware.lcd.LCD;

public class Erster {

	public static void main(String[] args) {
		RMIRegulatedMotor motorA=null;
		RMIRegulatedMotor motorB=null;
		RMIRegulatedMotor motorC=null;
		RMIRegulatedMotor motorD=null;
		EV3UltrasonicSensor ultraSensor=null;
		RMISampleProvider sampleProvider = null;
		RemoteEV3 ev3 = null;
		float high;
		float low;
		try {
			ev3=new RemoteEV3("10.0.1.1");
			//Motors
			motorA = ev3.createRegulatedMotor("A", 'L');
			motorB = ev3.createRegulatedMotor("B", 'L');
//			motorC = ev3.createRegulatedMotor("C, 'L');
//			motorD = ev3.createRegulatedMotor("D", 'L');
			//Fahren
			MotorControl mControl = new MotorControl(motorA, motorB);
			sampleProvider = ev3.createSampleProvider("S1", "lejos.hardware.sensor.EV3ColorSensor", "RGB");
			float[] colors = new float[3];
			
//			//Hell
//			LCD.drawString("Stellen Sie den ", 0, 0);
//			LCD.drawString("Roboter auf den ", 0, 1);
//			LCD.drawString("Hintergrund und", 0, 2);
//			LCD.drawString("drücken Sie einen", 0, 3);
//			LCD.drawString("beliebigen Knopf ", 0, 4);
//			Button.waitForAnyPress();
//			colors=sampleProvider.fetchSample();
//			high=colors[0];
//			LCD.clear();
//			
//			//Dunkel
//			LCD.drawString("Stellen Sie den ", 0, 0);
//			LCD.drawString("Roboter auf die ", 0, 1);
//			LCD.drawString("Linie und drücken ", 0, 2);
//			LCD.drawString("Sie einen belieb- ", 0, 3);
//			LCD.drawString("igen Knopf ", 0, 4);
//			Button.waitForAnyPress();
//			colors=sampleProvider.fetchSample();
//			low=colors[0];
//			LCD.clear();
//			LCD.drawString("Drücken Sie einen", 0, 0);
//			LCD.drawString("beliebigen Knopf ", 0, 1);
//			LCD.drawString("um Linie folgen  ", 0, 2);
//			LCD.drawString("zu starten ", 0, 3);
//			Button.waitForAnyPress();
			
//			//Run
//			LCD.clear();
//			LCD.drawString("Running", 0, 0);
			for (int i = 0; i < 1000; i++) {
				colors=sampleProvider.fetchSample();
						LCD.drawString("Rot: " + colors[0], 0, 0);
						LCD.drawString("Gruen: " + colors[1]+"", 0, 1);
						LCD.drawString("Blau: " + colors[2]+"", 0, 2);
//				int turn=0;
//				float differenz=high-low;
//				float nvalue=colors[0]-low;
//				float average=differenz/4;
//				float relativValue=nvalue-average;
//				float percentageValue= 0;
//				if (relativValue<0) {
//					percentageValue=relativValue/average;
//				}else {
//					percentageValue=relativValue/average/3;
//				}
//				float nturn= percentageValue*50;
//				turn=(int)nturn;
//				mControl.drive(50, turn);
//				LCD.clear();
//				LCD.drawString("Aktuell: " + colors[0], 0, 0);
//				LCD.drawString("Soll: " + average, 0, 1);
//				LCD.drawString("Differenz: " + relativValue, 0, 2);
//				LCD.drawString("Prozent: " + percentageValue, 0, 3);
				sleep(100);	
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		finally {
			try {
				if(motorA!=null) {
					motorA.close();
				}
				if(motorB!=null) {
					motorB.close();
				}
				if(motorC!=null) {
					motorC.close();
				}
				if(motorD!=null) {
					motorD.close();
				}
				if(sampleProvider!=null) {
				sampleProvider.close();
				}
				if(ultraSensor!=null) {
				ultraSensor.close();
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	public static void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
