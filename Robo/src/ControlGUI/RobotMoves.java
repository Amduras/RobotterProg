package versuche;

import java.rmi.RemoteException;
import java.sql.Time;

import lejos.hardware.lcd.LCD;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.remote.ev3.RMISampleProvider;
import lejos.robotics.SampleProvider;

public class RobotMoves {
	private RMISampleProvider farbSensor;
	private MotorControl mControl;
	private RMISampleProvider ultraSensor;
	private RMISampleProvider gyroSensor;
	private SampleProvider distance=null;
	private float[] aHell = new float[3];
	private float[] aDunkel = new float[3];
	private float[] aRand = new float[3];
	private float hell = 0, pTurn = 0, dunkel = 0, differenz = 0, dunkelArea = 0;
	private float hellArea = 0, iAbweichung = 0, zuletzt = 0, middle=0;
	private float KONSTANTE_P = 30, KONSTANTE_I = -0.1f, KONSTANTE_D = 0;
	private int farbe = 0;
	private volatile int speed=0;
	
	public RobotMoves(RMISampleProvider farbSensor, MotorControl mControl, RMISampleProvider ultraSensor, RMISampleProvider gyroSensor, int speed) {
		this.mControl = mControl;
		this.farbSensor = farbSensor;
		this.ultraSensor=ultraSensor;
		this.gyroSensor=gyroSensor;
		this.speed=speed;
	}
	
	public void followLine() throws RemoteException {
			float[] colors=farbSensor.fetchSample();
//			float nvalue=colors[farbe]-aRand[farbe];
			float nvalue=colors[farbe]-middle;
			if (nvalue==0) {
				pTurn=0;
			}else {
				if(nvalue<0) {
					pTurn=nvalue/hellArea;
				}else {
					if(nvalue>0) {
						pTurn=nvalue/dunkelArea;
					}
				}
			}
//			System.out.println("pTurn: " + pTurn);
			if (pTurn>1) {
				
				pTurn=1;
			}
			if (pTurn<-1) {
				pTurn=-1;
			}
			iAbweichung += pTurn;
			if (iAbweichung>200) {
				iAbweichung=200;
			}

			int turn=(int)(KONSTANTE_P*pTurn+KONSTANTE_I*iAbweichung+KONSTANTE_D*(pTurn-zuletzt));
//			System.out.println("Turn: " + turn);
//			System.out.println("pTurn-Zuletzt: " + (pTurn-zuletzt));
//			System.out.println("I-Abweichung: " + iAbweichung);
			zuletzt = pTurn;
			if(turn > 90) {
				turn = 90;
			}
			if(turn < -90) {
				turn = -90;
			}
			mControl.drive(speed, turn);
	}
	
	public static void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void followRobot() throws RemoteException {
		 
		float[] sample = new float[distance.sampleSize()];
		 
		distance.fetchSample(sample, 0);
		String txt="";
		for (int i = 0; i < sample.length; i++) {
			txt+=sample[i] + "\n";
		}
		LCD.drawString("Distanz: " + txt, 0, 3);

		//Sample Range between 0.03-2,5
		if (sample[0]>=0.2) {
			followLine();
		}else {
			if ( sample[0]>0.1 && sample[0]<0.2 ) {
				followLine();
			}else {
				
			}
		}
			// sample[0] contains distance ...
	}
	
	public void evade() {
		float[] sample = new float[distance.sampleSize()];
		distance.fetchSample(sample, 0);
		String txt="";
		for (int i = 0; i < sample.length; i++) {
			txt+=sample[i] + "\n";
		}
//		System.out.println("Distanz: " + txt);
		//Sample Range between 0.03-2,5
		if (sample[0]>=0.5) {
			
		}else {
			evadeStep1();
			evadeStep2();
			evadeStep3();
		}
	}
	
	private void evadeStep1() {
		
	}
	private void evadeStep2() {
		
	}
	private void evadeStep3() {
		
	}

	private void highLow() {
		if (aHell[0]!=0 && aDunkel[0]!=0 && aRand[0]!=0) {
			for (int i = 0; i < aDunkel.length; i++) {
				float differenz=aHell[i]-aDunkel[i];
				if(differenz>0){
					if (this.differenz<differenz) {
						this.differenz=differenz;
						hell=aHell[i];
						dunkel=aDunkel[i];
						farbe=i;
					}
				}else {
					differenz=aDunkel[i]-aHell[i];
					if (this.differenz<differenz) {
						this.differenz=differenz;
						hell=aDunkel[i];
						dunkel=aHell[i];
						farbe=i;
					}
				}
			}
			hellArea=hell-aRand[farbe];
			dunkelArea=aRand[farbe]-dunkel;
			System.out.println("Hell: "+hell);
			System.out.println("Dunkel: "+dunkel);
			System.out.println("Farbe: "+farbe);
			System.out.println("HellArea: " + hellArea);
			System.out.println("DunkelArea: " + dunkelArea);
		}
	}
	
	private void highLow2() {
		hell=0;
		dunkel=0;
		if (aHell[0]!=0 && aDunkel[0]!=0) {
			for (int i = 0; i < aDunkel.length; i++) {
				float differenz=aHell[i]-aDunkel[i];
				if(differenz>0){
					if (this.differenz<differenz) {
						this.differenz=differenz;
						hell=aHell[i];
						dunkel=aDunkel[i];
						farbe=i;
					}
				}else {
					differenz=aDunkel[i]-aHell[i];
					if (this.differenz<differenz) {
						this.differenz=differenz;
						hell=aDunkel[i];
						dunkel=aHell[i];
						farbe=i;
					}
				}
			}
			middle=(hell+dunkel)/2;
			hellArea=middle-dunkel;
			dunkelArea=hellArea;
			System.out.println("Hell: "+hell);
			System.out.println("Dunkel: "+dunkel);
			System.out.println("Middle: "+middle);
			System.out.println("Farbe: "+farbe);
			System.out.println("HellArea: " + hellArea);
			System.out.println("DunkelArea: " + dunkelArea);
		}
	}
	public float[] getColors() throws RemoteException {
		return farbSensor.fetchSample();
		
	}

	public void setArrayHell() throws RemoteException {
		aHell = farbSensor.fetchSample();
		System.out.print("HellArray: (");
		for (int i = 0; i < aHell.length; i++) {
			System.out.print(aHell[i]+"  ");
		}
		System.out.println(")");
		//highLow();
		highLow2();
	}

	public void setArrayDunkel() throws RemoteException {
		aDunkel = farbSensor.fetchSample();
		System.out.print("DunkelArray: (");
		for (int i = 0; i < aDunkel.length; i++) {
			System.out.print(aDunkel[i]+"  ");
		}
		System.out.println(")");
		//highLow();
		highLow2();
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
		System.out.println("Speed: " + speed);
	}

	public float[] getArrayRand() {
		return aRand;
	}

	public void setArrayRand() throws RemoteException {
		aRand = farbSensor.fetchSample();
		System.out.print("RandArray: (");
		for (int i = 0; i < aRand.length; i++) {
			System.out.print(aRand[i]+"  ");
		}
		System.out.println(")");
		//highLow();
	}
}
