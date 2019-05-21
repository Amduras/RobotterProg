package Linie;

import java.rmi.RemoteException;

import lejos.hardware.Button;
import lejos.hardware.Key;
import lejos.hardware.KeyListener;
import lejos.hardware.lcd.LCD;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.remote.ev3.RMIRegulatedMotor;
import lejos.remote.ev3.RMISampleProvider;
import lejos.remote.ev3.RemoteEV3;

public class Erster2 {
	static boolean running = true;
	
	public static void main(String[] args) {
		
		RMIRegulatedMotor motorA=null;
		RMIRegulatedMotor motorB=null;
		RMIRegulatedMotor motorC=null;
		RMIRegulatedMotor motorD=null;
		EV3UltrasonicSensor ultraSensor=null;
		RMISampleProvider sampleProvider = null;
		RemoteEV3 ev3 = null;
		float KONSTANTE_P = 50, KONSTANTE_I = 8, KONSTANTE_D = 12;
		float goal, nvalue, insgesamt = 0, zuletzt = 0;
		float high=0, low=0, pTurn=0;
		//float low;
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
			
			//Hell
			LCD.drawString("Stellen Sie den ", 0, 0);
			LCD.drawString("Roboter auf den ", 0, 1);
			LCD.drawString("Hintergrund und", 0, 2);
			LCD.drawString("drücken Sie einen", 0, 3);
			LCD.drawString("beliebigen Knopf ", 0, 4);
			Button.waitForAnyPress();
			colors=sampleProvider.fetchSample();
			high=colors[0];
			LCD.clear();
			
			//Dunkel
			LCD.drawString("Stellen Sie den ", 0, 0);
			LCD.drawString("Roboter auf die ", 0, 1);
			LCD.drawString("Linie und drücken ", 0, 2);
			LCD.drawString("Sie einen belieb- ", 0, 3);
			LCD.drawString("igen Knopf ", 0, 4);
			Button.waitForAnyPress();
			colors=sampleProvider.fetchSample();
			low=colors[0];

			//Ziel
			LCD.drawString("Stellen Sie den ", 0, 0);
			LCD.drawString("Roboter auf den ", 0, 1);
			LCD.drawString("Rand und drücken ", 0, 2);
			LCD.drawString("Sie einen belieb- ", 0, 3);
			LCD.drawString("igen Knopf ", 0, 4);
			Button.waitForAnyPress();
			colors=sampleProvider.fetchSample();
//			running = true;
			
			goal=colors[0];
			LCD.clear();
//			LCD.drawString("Drücken Sie einen", 0, 0);
//			LCD.drawString("beliebigen Knopf ", 0, 1);
//			LCD.drawString("um Linie folgen  ", 0, 2);
//			LCD.drawString("zu starten ", 0, 3);
//			Button.waitForAnyPress();
			
//			//Run
//			LCD.clear();
			LCD.drawString("Running", 0, 0);
			
			Button.UP.addKeyListener(new KeyListener() {

				@Override
				public void keyPressed(Key k) {
					running = false;
					LCD.drawString("UP gedrückt", 0, 0);
				}

				@Override
				public void keyReleased(Key k) {
					LCD.clear();
				}
				
			});
			
			float blackArea=goal-low;
			float whiteArea=high-goal;
//			Button.UP.isUp
			while(running) {
				colors=sampleProvider.fetchSample();
				nvalue=colors[0]-goal;
				if (nvalue==0) {
					pTurn=0;
				}else {
					if(nvalue<0) {
						pTurn=nvalue/whiteArea;
					}else {
						if(nvalue>0) {
							pTurn=nvalue/blackArea;
						}
					}
				}
				insgesamt += pTurn;
				if (insgesamt>1) {
					insgesamt=1;
				}
				if (insgesamt<-1) {
					insgesamt=-1;
				}
				int turn=(int)(KONSTANTE_P*pTurn+KONSTANTE_I*insgesamt+KONSTANTE_D*(zuletzt-pTurn));
				zuletzt = pTurn;
				mControl.drive(100, turn);
//				LCD.clear();
//				LCD.drawString("Aktuell: " + colors[0], 0, 0);
//				LCD.drawString("Soll: " + average, 0, 1);
//				LCD.drawString("Differenz: " + relativValue, 0, 2);
//				LCD.drawString("Prozent: " + percentageValue, 0, 3);
//				sleep(100);	
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