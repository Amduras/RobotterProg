package ControlGUI;

import java.rmi.RemoteException;
import java.sql.Timestamp;

import org.jfree.util.ClassComparator;

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
	private volatile float pTurn = 0;
	private float hell = 0, dunkel = 0, differenz = 0, dunkelArea = 0;
	private float hellArea = 0, iAbweichung = 0, middle=0;
	private float zuletzt = 0, vorletzte=0;
	private float KONSTANTE_P = 81, KONSTANTE_I = 10f, KONSTANTE_D = 500;
	//Linie fahren KONSTANTE_P = 15, KONSTANTE_I = 0.4f, KONSTANTE_D = 750;
	private float abweichung=0;
	//Kp=10 Ki=0 Kd=500 fuer Linie folgen
	private int farbe = 0;
	private volatile int speed=70;
	private volatile boolean followLine=false;
	private volatile boolean evade=true; //Wenn evade==true: Roboter weicht Hindernissen aus ? Folgt anderen Robotern
	private boolean rotModus=false;
	private StepEnum step =StepEnum.FOLLOWLINE;
	private float[] sample;
	private float grenzwert=0.7f;
	private volatile int turn = 0;
	private volatile boolean first=true;
	private volatile int timerAbzug=0;
	private volatile long time;
	private boolean standingright=false, exit=false;
	private boolean turnLeft=false;
	private int iMax=1;
	private int i=1, j=0;
			
	public RobotMoves(RMISampleProvider farbSensor, MotorControl mControl, RMISampleProvider ultraSensor, RMISampleProvider gyroSensor, int speed) {
		this.mControl = mControl;
		this.farbSensor = farbSensor;
		this.ultraSensor=ultraSensor;
		this.gyroSensor=gyroSensor;
		this.speed=speed;
	}
	
	public void followLine() throws RemoteException {
		calcPTurn();
			if (step==StepEnum.START) {
				if (pTurn>0.9) {
					step=StepEnum.SPIRAL;
				}else {
					step=StepEnum.FOLLOWLINE;
				}
			}
			if (pTurn>1) {
				pTurn=1;
			}
			if (pTurn<-1) {
				pTurn=-1;
			}
			iAbweichung += pTurn;
			if (iAbweichung>iMax) {
				iAbweichung=iMax;
			}
			if (iAbweichung<-iMax) {
				iAbweichung=-iMax;
			}
			if (first) {
				zuletzt=pTurn;
				vorletzte=pTurn;
				first=false;
			}
			abweichung=pTurn-zuletzt;
			differenz=vorletzte-pTurn;
//			System.out.println(differenz + " und pTurn:" + pTurn + " und turn:" + turn);
//			if(((differenz>=grenzwert && pTurn>0.9) || (differenz<=-grenzwert && pTurn<-0.9)) && step==StepEnum.FOLLOWLINE) {
//				if (differenz>=grenzwert) {
//					step=StepEnum.LEFTLINE_DARK;
//					System.out.println("DARK");
//				}else {
//					step=StepEnum.LEFTLINE_WHITE;
//					System.out.println("WHITE");
//				}
//				System.out.println("START TURN!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//			}
			turn=(int)(KONSTANTE_P*pTurn+KONSTANTE_I*iAbweichung+KONSTANTE_D*abweichung);
			vorletzte=zuletzt;
			zuletzt = pTurn;
			if (turn>100) {
				turn=100;
			}
			if (turn<-100) {
				turn=-100;
			}
			if (pTurn<0.7 &&(step==StepEnum.EVADE3 || step==StepEnum.EVADE4 || step==StepEnum.EVADE5 || step==StepEnum.LEFTLINE_WHITE)) {
				step=StepEnum.FOLLOWLINE;
			}
//			if (step==StepEnum.FOLLOWLINE && pTurn>0.9) {
//				step=StepEnum.LEFTLINE_WHITE;
//			}
			switch(step) {
				case LEFTLINE_DARK:
//					mControl.rotate(155, true);
//					System.out.println("NEGATIV!! " + pTurn);
//					while (pTurn<0.1)
//					{
//						calcPTurn();
//						mControl.stop();
//						mControl.rotate(20, true);
//						mControl.drive(speed, 0);
//						sleep(15);
//					}
//					step=StepEnum.FOLLOWLINE;
//					System.out.println(pTurn);
//					System.out.println("ENDE TURN!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//					first = true;
					turnLeft=false;
					step=StepEnum.FINDLINE;
					break;
				case LEFTLINE_WHITE:
					turnLeft=true;
					step=StepEnum.FINDLINE;
//					mControl.drive(speed, 80);
					break;
//				case FINDLINE:
//					//System.out.println("FOUNDLINE!!: " + pTurn);
//					standingright=false;
//					boolean firstrotate=true;
//					boolean rightTurn=false;
//					while (pTurn>0.2 || pTurn<-0.2) {
//						calcPTurn();
//						if (pTurn>0.2) {
//							mControl.drive(50, 0);
//						}
//						if (pTurn<-0.2) {
//							mControl.drive(-50, 0);
//						}
//					}
//					if (firstrotate) {
//						if (turnLeft) {
//							mControl.rotate(180, false);
//						}else {
//							mControl.rotate(180, true);
//						}
//						firstrotate=false;
//					}else {
//						if (rightTurn) {
//							mControl.rotate(20, true);
//						}else {
//							mControl.rotate(20, false);
//						}
//					}
//					mControl.drive(50, 0);
//					sleep(200);
//					mControl.stop();
//					calcPTurn();
//					if (pTurn>0.2 || pTurn<-0.2) {
//						if (pTurn>0.2) {
//							rightTurn=true;
//						}else {
//							rightTurn=false;
//						}
//						mControl.drive(-50, 0);
//						sleep(200);
//						mControl.stop();
//					}else {
//						step=StepEnum.FOLLOWLINE;
//						reset();
//						standingright=true;
//					}
//					break;
				case FOLLOWLINE:
					System.out.println("FOLLOWLINE!!");
					sample=ultraSensor.fetchSample();
//					Sample Range between 0.03-2,5
					if (sample[0]>=0.2) {
						mControl.drive(speed, turn);
					}else {
						if (!evade) {
							if (sample[0]>0.1) {
								double slow =(sample[0]-0.1)/0.1;
								mControl.drive((int)slow*speed, turn);
							}else {
								mControl.drive(-speed, -turn);
							}
						}else {
							if ( sample[0]>0.1) {
								//turn<=90 Grad rechts
								mControl.rotate(180, false);
								step=StepEnum.EVADE1;
								time= System.currentTimeMillis();
							}else {
								mControl.drive(-speed, 0);
							}
						}
					}
					break;
				case EVADE1:
					System.out.println("EVADE1!!!!!!");
					//gerade aus fahren
					System.out.println(System.currentTimeMillis() + "  " + time);
					if ((System.currentTimeMillis()-time)<2500) {
						
						System.out.println(System.currentTimeMillis() + "  " + time);
						calcPTurn();
						if (!checkEvade()) {
							if (time-System.currentTimeMillis()>2000 && pTurn<0.8) {
								step=StepEnum.FINDLINE;
								mControl.drive(0, 0);
								break;
							}else {
								mControl.drive(75, 0);
							}
						}
					}else {
						step=StepEnum.EVADE2;
						break;
					}
					break;
				case EVADE2:
					System.out.println("EVADE2!!!!!!");
					//links drehen (wieder gerade)
					mControl.rotate(180, true);
					time=System.currentTimeMillis();
					step=StepEnum.EVADE3;
					break;
				case EVADE3:
					System.out.println("EVADE3!!!!!!");
					//gerade aus fahren
					if ((System.currentTimeMillis()-time)<14000) {
						if (!checkEvade()) {
							mControl.drive(75, 0);
						}
					}else {
						step=StepEnum.EVADE4;
						break;
					}
					break;
				case EVADE4:
					System.out.println("EVADE4!!!!!!");
					//links drehen (hinter dem Hinderniss Linie suchen)
					mControl.rotate(180, true);
					time=System.currentTimeMillis();
					step=StepEnum.EVADE5;
					break;
				case EVADE5:
					System.out.println("EVADE5!!!!!!");
					//gerade aus fahren
					if ((System.currentTimeMillis()-time)<5000) {
						if (!checkEvade()) {
							mControl.drive(75, 0);
						}
					}else {
						step=StepEnum.SPIRAL;
						break;
					}
					break;
				case SPIRAL:
					calcPTurn();
					System.out.println("SPIRAL");
					if(!checkEvade()) {
						calcPTurn();
						mControl.drive(100, 50-i);
						if (j%(i*10)==0&&i<=45){++i;}
						++j;
						if (pTurn<0.3) {
							step=StepEnum.FOLLOWLINE;
							mControl.drive(10, 0);
						}
					}
					sleep(10);
					break;
			}
			RobotControl.setCurrentValues(abweichung, turn, sample, iAbweichung);
//			try {
//				Thread.sleep(180);
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
			e.printStackTrace();
		}
	}
	public void reset() {
		iAbweichung=0;
		zuletzt=0;
		abweichung=0;
		first=true;
	}
	private void calcPTurn() throws RemoteException {
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
	}
	private boolean checkEvade() throws RemoteException {
		sample=ultraSensor.fetchSample();
		if (sample[0]<0.2 && sample[0]>0.1) {
			//turn<=90 Grad rechts
			mControl.rotate(180, false);
			step=StepEnum.EVADE1;
			time= System.currentTimeMillis();
			return true;
		}
		if (sample[0]<0.1) {
			while (sample[0]<0.1) {
				mControl.drive(-speed, 0);
				sample=ultraSensor.fetchSample();
			}
			mControl.rotate(180, false);
			step=StepEnum.EVADE1;
			time= System.currentTimeMillis();
			return true;
		}
		return false;
	}
	public void setStep(StepEnum step) {
		this.step=step;
	}
}
