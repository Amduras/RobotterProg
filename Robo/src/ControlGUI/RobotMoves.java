package versuche;

import java.rmi.RemoteException;
import java.sql.Timestamp;
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
	private float vorvorletzteAbweichung = 0, vorletzteAbweichung = 0, letzteAbweichung = 0, zuletzt = 0, vorletzte=0;
	private float KONSTANTE_P = 10, KONSTANTE_I = 0, KONSTANTE_D = 750;
	private float abweichung=0;
	//Kp=10 Ki=0 Kd=500 fuer Linie folgen
	private int farbe = 0;
	private volatile int speed=50;
	private volatile boolean followLine=false;
	private volatile boolean evade=true; //Wenn evade==true: Roboter weicht Hindernissen aus ? Folgt anderen Robotern
	private boolean rotModus=false;
	private StepEnum step =StepEnum.FOLLOWLINE;
	private float[] sample;
	private float grenzwert=0.55f;
	private int turn = 0;
	private boolean first=true;
	private int timerAbzug=0;
	long time;
	
			
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
			if (first) {
				zuletzt=pTurn;
				vorletzte=pTurn;
				first=false;
			}
			abweichung=pTurn-zuletzt;
			differenz=vorletzte-pTurn;
			System.out.println(differenz + " und pTurn:" + pTurn);
			if((differenz>=grenzwert ||differenz<=-grenzwert) && step==StepEnum.FOLLOWLINE) {
				if (differenz>=grenzwert) {
					step=StepEnum.TURNPOSITIV;
				}else {
					step=StepEnum.TURNNEGATIV;
				}
				System.out.println("START TURN!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			}
			turn=(int)(KONSTANTE_P*pTurn+KONSTANTE_I*iAbweichung+KONSTANTE_D*(abweichung));
			vorvorletzteAbweichung = vorletzteAbweichung;
			vorletzteAbweichung = letzteAbweichung;
			letzteAbweichung = abweichung;
			vorletzte=zuletzt;
			zuletzt = pTurn;
			if (turn>100) {
				turn=100;
			}
			if (turn<-100) {
				turn=-100;
			}
			if (pTurn<0.8 &&(step==StepEnum.EVADE3 || step==StepEnum.EVADE4 || step==StepEnum.EVADE5 || step==StepEnum.EVADE6)) {
				step=StepEnum.FOLLOWLINE;
			}
			switch(step) {
				case TURNNEGATIV:
					mControl.motorA.rotate(135,true);
					mControl.motorB.rotate(-135,true);
					while(mControl.motorB.isMoving())
					{
						;
					}
					while (pTurn<0.5)
					{
						colors=farbSensor.fetchSample();
						nvalue=colors[farbe]-middle;
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
						mControl.motorA.stop(true);
						mControl.motorB.stop(true);
						mControl.motorA.rotate(20,true);
						mControl.motorB.rotate(-20,true);
						while(mControl.motorB.isMoving())
						{
							;
						}
						mControl.drive(speed, 0);
						sleep(15);
					}

					step=StepEnum.FOLLOWLINE;
					System.out.println("ENDE TURN!!!!!!!!!!!!!!!!!!!!!!!!!!!");
					first = true;
					break;
				case TURNPOSITIV:
					mControl.motorA.rotate(-135,true);
					mControl.motorB.rotate(135,true);
					while(mControl.motorB.isMoving())
					{
						;
					}
					while (pTurn>-0.5)
					{
						colors=farbSensor.fetchSample();
						nvalue=colors[farbe]-middle;
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
						mControl.motorA.stop(true);
						mControl.motorB.stop(true);
						mControl.motorA.rotate(-20,true);
						mControl.motorB.rotate(20,true);
						while(mControl.motorB.isMoving())
						{
							;
						}
						mControl.drive(speed, 0);
						sleep(15);
					}

					step=StepEnum.FOLLOWLINE;
					System.out.println("ENDE TURN!!!!!!!!!!!!!!!!!!!!!!!!!!!");
					first = true;
					break;
				case FOLLOWLINE:
					sample=ultraSensor.fetchSample();
					//Sample Range between 0.03-2,5
					if (sample[0]>=0.3) {
						mControl.drive(speed, turn);
					}else {
						if ( sample[0]>0.1) {
							if (evade) {
								//turn<=90 Grad rechts
								mControl.drive(50, -100);
								sleep(480);
								step=StepEnum.EVADE1;
								time= System.currentTimeMillis();
							}else {
								double slow=(sample[0]-0.1)/0.2; //abhaengig von dem Abstand vom vorherfahrenden Wagen verlangsamen (normalisiert auf die momentanen Parameter)
								mControl.drive((int)slow*speed, turn);
							}
						}else {
							if (evade) {
								mControl.drive(-speed, 0);
							}else {
								mControl.drive(0, 0);
							}
						}
					}
					break;
				case EVADE1:
					//gerade aus fahren
					if ((time - System.currentTimeMillis())<1000) {
						if (time-System.currentTimeMillis()>200 && pTurn<0.8) {
							step=StepEnum.FOLLOWLINE;
							mControl.drive(speed, turn);
						}else {
							mControl.drive(speed, 0);
						}
						break;
					}else {
						step=StepEnum.EVADE2;
						time=System.currentTimeMillis();
					}
				case EVADE2:
					//links drehen (wieder gerade)
					if ((time - System.currentTimeMillis())<480) {
						mControl.drive(50, 100);
						break;
					}else {
						step=StepEnum.EVADE3;
						time=System.currentTimeMillis();
					}
				case EVADE3:
					//gerade aus fahren
					if ((time - System.currentTimeMillis())<1000) {
						mControl.drive(speed, 0);
						break;
					}else {
						step=StepEnum.EVADE4;
						time=System.currentTimeMillis();
					}
				case EVADE4:
					//links drehen (hinter dem Hinderniss Linie suchen)
					if ((time - System.currentTimeMillis())<480) {
						mControl.drive(50, 100);
						break;
					}else {
						step=StepEnum.EVADE5;
						time=System.currentTimeMillis();
					}
				case EVADE5:
					//gerade aus fahren
					if ((time - System.currentTimeMillis())<1000) {
						mControl.drive(speed, 0);
						break;
					}else {
						step=StepEnum.EVADE6;
						time=System.currentTimeMillis();
					}
				case EVADE6:
					//Linie suchen indem man in immer groesser werdenden Kreisen sucht
					int Abzug=(int) ((time-System.currentTimeMillis())/1000);
					mControl.drive(speed, 100-Abzug);
					break;
			}
			RobotControl.setCurrentValues(abweichung, turn, sample, iAbweichung);
//			try {
//				Thread.sleep(20);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
	}
	
	private void highLow() {
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
		highLow();
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
		highLow();
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
	public void reset() {
		iAbweichung=0;
		zuletzt=0;
		abweichung=0;
		letzteAbweichung=0;
		vorletzteAbweichung=0;
		vorvorletzteAbweichung=0;
		first=true;
	}
}
