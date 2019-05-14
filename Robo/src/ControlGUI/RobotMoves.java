package versuche;

import java.rmi.RemoteException;

import lejos.hardware.lcd.LCD;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.remote.ev3.RMISampleProvider;
import lejos.robotics.SampleProvider;

public class RobotMoves {
	private RMISampleProvider farbSensor;
	private MotorControl mControl;
	private RMISampleProvider ultraSensor;
	private SampleProvider distance=null;
	private float[] aHell = new float[3];
	private float[] aDunkel = new float[3];
	private float[] aRand = new float[3];
	private float hell = 0, pTurn = 0, dunkel = 0, differenz = 0, dunkelArea = 0;
	private float hellArea = 0, iAbweichung = 0, zuletzt = 0;
	private int farbe = 0, KONSTANTE_P = 40, KONSTANTE_I = 0, KONSTANTE_D = 60;
	private int speed=75;
	private boolean followLine=false;
	
	public RobotMoves(RMISampleProvider farbSensor, MotorControl mControl, RMISampleProvider ultraSensor) {
		this.mControl = mControl;
		this.farbSensor = farbSensor;
		this.ultraSensor=ultraSensor;
	}
	
	public void followLine() throws RemoteException {
		while(followLine) {
			float[] colors=farbSensor.fetchSample();
			float nvalue=colors[farbe]-aRand[farbe];
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
			iAbweichung += pTurn;
			if (iAbweichung>1) {
				iAbweichung=1;
			}
			if (iAbweichung<-1) {
				iAbweichung=-1;
			}
			int turn=(int)(KONSTANTE_P*pTurn+KONSTANTE_I*iAbweichung+KONSTANTE_D*(pTurn-zuletzt));
			System.out.println("Turn: " + turn);
			System.out.println("pTurn: " + pTurn);
			System.out.println("pTurn-Zuletzt: " + (pTurn-zuletzt));
			System.out.println("I-Abweichung: " + iAbweichung);
			zuletzt = pTurn;
			mControl.drive(speed, turn);
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
	
	public void followRobot() throws RemoteException {
		 
		float[] sample = new float[distance.sampleSize()];
		 
		distance.fetchSample(sample, 0);
		String txt="";
		for (int i = 0; i < sample.length; i++) {
			txt+=sample[i] + "\n";
		}
		LCD.drawString("Distanz: " + txt, 0, 3);
		
		if (sample[0]>=5) {
			followLine();
		}else {
			
		}
			// sample[0] contains distance ...
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
			hellArea=aRand[farbe]-hell;
			dunkelArea=dunkel-aRand[farbe];
			System.out.println("Hell: "+hell);
			System.out.println("Dunkel: "+dunkel);
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
		highLow();
	}

	public void setArrayDunkel() throws RemoteException {
		aDunkel = farbSensor.fetchSample();
		System.out.print("DunkelArray: (");
		for (int i = 0; i < aDunkel.length; i++) {
			System.out.print(aDunkel[i]+"  ");
		}
		System.out.println(")");
		highLow();
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
		highLow();
	}
	
	public boolean isFollowLine() {
		return followLine;
	}

	public void setFollowLine(boolean followLine) {
		this.followLine = followLine;
	}
}
