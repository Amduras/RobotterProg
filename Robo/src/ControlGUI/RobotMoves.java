package versuche;

import java.rmi.RemoteException;
import java.security.Timestamp;
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
	private float hellArea = 0, iAbweichung = 0, middle=0;
	private float vorvorletzteAbweichung = 0, vorletzteAbweichung = 0, letzteAbweichung = 0, zuketzt = 0;
	private float KONSTANTE_P = 10, KONSTANTE_I = 0.5f, KONSTANTE_D = 750;
	//Kp=10 Ki=0 Kd=500 für Linie folgen
	private int farbe = 0;
	private volatile int speed=0;
	private volatile boolean followLine=false;
	private volatile boolean evade=true; //Wenn evade==true: Roboter weicht Hindernissen aus ? Folgt anderen Robotern
	private boolean rotModus=false;
	private StepEnum step =StepEnum.FOLLOWLINE;
	private float[] sample;
	private float grenzwert=50;
	private int turn = 0;
	Timestamp time;
	
			
	public RobotMoves(RMISampleProvider farbSensor, MotorControl mControl, RMISampleProvider ultraSensor, RMISampleProvider gyroSensor, int speed) {
		this.mControl = mControl;
		this.farbSensor = farbSensor;
		this.ultraSensor=ultraSensor;
		this.gyroSensor=gyroSensor;
		this.speed=speed;
	}
	
	public void followLine() throws RemoteException {
			float[] colors=farbSensor.fetchSample();
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
			if (pTurn>1) {
				pTurn=1;
			}
			if (pTurn<-1) {
				pTurn=-1;
			}
			iAbweichung += pTurn;
			if (iAbweichung>40) {
				iAbweichung=40;
			}
			if (iAbweichung<-40) {
				iAbweichung=-40;
			}
			float abweichung=pTurn-zuketzt;
			if(((vorvorletzteAbweichung>=grenzwert || vorletzteAbweichung>=grenzwert || letzteAbweichung>=grenzwert || zuketzt>=grenzwert || pTurn>=grenzwert) && (pTurn<-80 || pTurn>80)) ||((vorvorletzteAbweichung<=-grenzwert || vorletzteAbweichung<=-grenzwert || letzteAbweichung<=-grenzwert || zuketzt<=-grenzwert || pTurn>=grenzwert) && (pTurn<-80 || pTurn>80))) {
				if (pTurn>80) {
					turn=100;
				}else {
					turn=-100;
				}
			}else {
				turn=(int)(KONSTANTE_P*pTurn+KONSTANTE_I*iAbweichung+KONSTANTE_D*(abweichung));
			}
			vorvorletzteAbweichung = vorletzteAbweichung;
			vorletzteAbweichung = letzteAbweichung;
			letzteAbweichung = abweichung;
			zuketzt = pTurn;

			switch(step) {
				case FOLLOWLINE:
					sample=ultraSensor.fetchSample();
					//Sample Range between 0.03-2,5
					if (sample[0]>=0.3) {
						mControl.drive(speed, turn);
					}else {
						if ( sample[0]>0.1) {
							if (evade) {
								evade();
							}else {
								double slow=(sample[0]-0.1)/0.2; //abhängig von dem Abstand vom vorherfahrenden Wagen verlangsamen (normalisiert auf die momentanen Parameter)
								mControl.drive((int)slow*speed, turn);
							}
						}else {
							if (sample[0]<=0.1) {
								if (evade) {
									//mControl.drive(-speed, 0);
									mControl.drive(0, 0);
								}else {
									mControl.drive(0, 0);
								}
							}
						}
					}
					break;
				case EVADE1:
					break;
				case EVADE2:
					break;
				case EVADE3:
					break;
				case EVADE4:
					break;
				case EVADE5:
					break;
				case EVADE6:
					break;
			}
			RobotControl.setCurrentValues(abweichung, turn, sample, iAbweichung);
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	}
	
	
	public void evade() {
		
	}
	public void evade1() {
		//turn<=90° rechts
		mControl.drive(speed, 70);
	}
	public void evade2() {
		//gerade aus fahren
		mControl.drive(speed, 0);
	}
	public void evade3() {
		//links drehen (wieder gerade)
		mControl.drive(speed, -70);
	}
	public void evade4() {
		//gerade aus fahren
		mControl.drive(speed, 0);
	}
	public void evade5() {
		//links drehen (hinter dem Hinderniss wieder nach Linie suchen)
		mControl.drive(speed, -70);
	}
	public void evade6() {
		//Linie suchen indem man in immer größer werdenden Kreisen sucht
		int time=0;
		mControl.drive(speed, 100-time);
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
	
	public float[] getArrayHell() {
		return aHell;
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

	public float[] getArrayDunkel() {
		return aDunkel;
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

	public boolean isFollowLine() {
		return followLine;
	}

	public void setFollowLine(boolean followLine) {
		this.followLine = followLine;
	}

	public boolean isEvade() {
		return evade;
	}

	public void setEvade(boolean evade) {
		this.evade = evade;
	}

	public boolean isRotModus() {
		return rotModus;
	}

	public void setRotModus(boolean rotModus, RMISampleProvider farbSensor) {
		this.farbSensor=farbSensor;
		this.rotModus = rotModus;
		aHell=null;
		aDunkel=null;
		hell=0;
		dunkel=0;
		middle=0;
		hellArea=0;
		dunkelArea=0;
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
