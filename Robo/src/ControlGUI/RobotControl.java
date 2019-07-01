package ControlGUI;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;

import lejos.remote.ev3.RMIRegulatedMotor;
import lejos.remote.ev3.RMISampleProvider;
import lejos.remote.ev3.RemoteEV3;

public class RobotControl {
	private static RMIRegulatedMotor motorA;
	private static RMIRegulatedMotor motorB;
	private static RMIRegulatedMotor motorC;
	private static RMIRegulatedMotor motorD;
	private static RMISampleProvider ultraSensor;
	private static RMISampleProvider farbSensor;
	private static RMISampleProvider gyroSensor;
	private static MotorControl mControl;
	private static RemoteEV3 ev3;
	private static RobotMoves rMoves;
	private static volatile int speed=70;
	private static volatile boolean followLine = false;
	private static Label lAbweichung = new Label("Abweichung:");
	private static Label lTurn = new Label("Turn:");
	private static Label lDistance = new Label("Distance:");
	private static Label lIAbweichung = new Label("I-Abweichung:");
	private static boolean rotModus=false;
	private static Thread followLineThread;
	private long time=0;
	private EnumRichtung richtung = EnumRichtung.START;
	private Vector<Richtungsmerker> richtungen = new Vector<Richtungsmerker>();
	
	public RobotControl(){
		try {
			setup();
			//View
			JFrame frame= new JFrame("Robot Control");
			frame.setSize(2000, 2000);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setLayout(new BorderLayout());
			
			JPanel center = new JPanel(new GridLayout(8, 0));
			JPanel east = new JPanel(new GridLayout(7, 0));
			
			Label lHell=new Label("Hell:");
			Label lDunkel=new Label("Dunkel:");
			Label lConnect = new Label("Verbindungsstatus: Disconnected");
			
			JRadioButton bStop= new JRadioButton("Stop", true);
			bStop.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (!(richtung==EnumRichtung.START)) {
						richtungen.add(new Richtungsmerker(System.currentTimeMillis()-time, richtung));
					}
					richtung=EnumRichtung.START;
					followLine = false;
					mControl.drive(0, 0);
					System.out.println("Stop");
					rMoves.reset();
				}
			});
			JRadioButton bRGB= new JRadioButton("RGB-Modus", true);
			bRGB.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					rotModus=false;
					followLine=false;
					mControl.drive(0, 0);
					bStop.setSelected(true);
					
					if (farbSensor!=null) {
						try {
							farbSensor.close();
						} catch (RemoteException e2) {
							e2.printStackTrace();
						}
					}
					farbSensor = ev3.createSampleProvider("S1", "lejos.hardware.sensor.EV3ColorSensor", "RGB");
					rMoves.setRotModus(true, farbSensor);
					lHell.setText("Hell:");
					lDunkel.setText("Dunkel:");
				}
			});
			JButton bHell= new JButton("Hell");
			bHell.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						followLine = false;
						rMoves.setArrayHell();
						float[] array = rMoves.getArrayHell();
						String txt="";
						for (int i = 0; i < array.length; i++) {
							txt+=array[i] + "\n";
						}
						lHell.setText("Hell= RGB: " + txt);
					} catch (RemoteException e1) {
						e1.printStackTrace();
					}
				}
			});
			JButton bDunkel= new JButton("Dunkel");
			bDunkel.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						followLine = false;
						rMoves.setArrayDunkel();
						float[] array = rMoves.getArrayDunkel();
						String txt="";
						for (int i = 0; i < array.length; i++) {
							txt+=array[i] + "\n";
						}
						lDunkel.setText("Dunkel= Red: " + txt);
					} catch (RemoteException e1) {
						e1.printStackTrace();
					}
				}
			});
			JRadioButton bVorwaerts= new JRadioButton("Vorwaerts");
			bVorwaerts.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					followLine = false;
					if (!(richtung==EnumRichtung.START)) {
						richtungen.add(new Richtungsmerker((System.currentTimeMillis()-time), richtung));
					}
					time=System.currentTimeMillis();
					richtung=EnumRichtung.GERADEAUS;
					mControl.drive(speed, 0);
					System.out.println("Vorwaerts");
				}
			});
			JRadioButton bRueckwaerts= new JRadioButton("Rueckwaerts");
			bRueckwaerts.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (!(richtung==EnumRichtung.START)) {
						richtungen.add(new Richtungsmerker((System.currentTimeMillis()-time), richtung));
					}
					time=System.currentTimeMillis();
					richtung=EnumRichtung.RUECKWAERTSGANG;
					followLine = false;
					mControl.drive(-speed, 0);
					System.out.println("Rueckwaerts");
				}
			});
			JRadioButton bLinks= new JRadioButton("Links");
			bLinks.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (!(richtung==EnumRichtung.START)) {
						richtungen.add(new Richtungsmerker((System.currentTimeMillis()-time), richtung));
					}
					time=System.currentTimeMillis();
					richtung=EnumRichtung.LINKS;
					followLine = false;
					mControl.drive(speed, 100);
					System.out.println("Links Kurve");
				}
			});
			JRadioButton bRechts= new JRadioButton("Rechts");
			bRechts.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (!(richtung==EnumRichtung.START)) {
						richtungen.add(new Richtungsmerker((System.currentTimeMillis()-time), richtung));
					}
					time=System.currentTimeMillis();
					richtung=EnumRichtung.RECHTS;
					followLine = false;
					mControl.drive(speed, -100);
					System.out.println("Rechts Kurve");
				}
			});
			JRadioButton bReset= new JRadioButton("Reset");
			bReset.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					mControl.drive(0, 0);
					time=0;
					richtung=EnumRichtung.START;
					richtungen=new Vector<Richtungsmerker>();
					followLine = false;
					System.out.println("Reset");
				}
			});
			JRadioButton bZurueck= new JRadioButton("Zurueck");
			bZurueck.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (!(richtung==EnumRichtung.START)) {
						richtungen.add(new Richtungsmerker((System.currentTimeMillis()-time), richtung));
					}
					richtung=EnumRichtung.START;
					try {
						mControl.rotate(360, true);
					} catch (RemoteException e1) {
						e1.printStackTrace();
					}
					for (int i = 1; i <= richtungen.size(); i++) {
						Richtungsmerker ab = richtungen.get(richtungen.size()-i);
						if (!(ab.getTime()==0)) {
							switch (ab.getRichtung()){
							case LINKS:
								time=System.currentTimeMillis();
								while (System.currentTimeMillis()-time<ab.getTime()) {
									mControl.drive(speed, -100);
								}
								mControl.drive(0, 0);
								break;
							case GERADEAUS:
								time=System.currentTimeMillis();
								while (System.currentTimeMillis()-time<ab.getTime()) {
									mControl.drive(speed, 0);
								}
								mControl.drive(0, 0);
								break;
							case RECHTS:
								time=System.currentTimeMillis();
								while (System.currentTimeMillis()-time<ab.getTime()) {
									mControl.drive(speed, 100);
								}
								mControl.drive(0, 0);
								break;
							case RUECKWAERTSGANG:
								time=System.currentTimeMillis();
								while (System.currentTimeMillis()-time<ab.getTime()) {
									mControl.drive(-speed, 0);
								}
								mControl.drive(0, 0);
								break;
							case START:
								System.out.println("FEHLER");
								break;
							default:
								break;
							}
						}
					}
					
				}
				
			});
			JRadioButton bAusweichen= new JRadioButton("Ausweichen");
			bAusweichen.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					richtung=EnumRichtung.START;
					richtungen=new Vector<Richtungsmerker>();
					rMoves.setFollowLine(false);
					rMoves.setEvade(true);
					startFollowLine();
				}
			});
			JRadioButton bFolgen= new JRadioButton("Roboter folgen");
			bFolgen.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					richtung=EnumRichtung.START;
					richtungen=new Vector<Richtungsmerker>();
					rMoves.setFollowLine(false);
					rMoves.setEvade(false);
					startFollowLine();
				}
			});
			JRadioButton bFollowLine= new JRadioButton("Follow line");
			bFollowLine.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					richtung=EnumRichtung.START;
					richtungen=new Vector<Richtungsmerker>();
					rMoves.setFollowLine(true);
					startFollowLine();
				}
			});
			
			
			ButtonGroup manualButtons = new ButtonGroup();
			manualButtons.add(bVorwaerts);
			manualButtons.add(bRueckwaerts);
			manualButtons.add(bRechts);
			manualButtons.add(bReset);
			manualButtons.add(bLinks);
			manualButtons.add(bStop);
			manualButtons.add(bAusweichen);
			manualButtons.add(bFolgen);
			manualButtons.add(bFollowLine);
			manualButtons.add(bZurueck);
			
			ButtonGroup modusButtons = new ButtonGroup();
			modusButtons.add(bRGB);
			
			JPanel jPan1 = new JPanel();
			jPan1.add(bVorwaerts);
			jPan1.add(bRueckwaerts);
			jPan1.add(bRechts);
			jPan1.add(bLinks);
			jPan1.add(bZurueck);
			jPan1.add(bReset);
			jPan1.add(bStop);
			
			JPanel jPan2 = new JPanel();
			jPan2.add(bAusweichen);
			jPan2.add(bFolgen);
			jPan2.add(bFollowLine);

			JPanel jPan3 = new JPanel();
			jPan3.add(bHell);
			jPan3.add(bDunkel);
			
			JPanel jPanModus = new JPanel();
			jPanModus.add(bRGB);
			
			Label lSpeed = new Label("Speed Regulator: Currently: " + speed);
			JScrollBar jsSpeed = new JScrollBar(JScrollBar.HORIZONTAL, speed, 0, 1, speed*2);
			jsSpeed.addAdjustmentListener(e->{
				speed=jsSpeed.getValue();
	            rMoves.setSpeed(speed);
	            lSpeed.setText("Speed Regulator: Currently: " + speed);
	        });

			center.add(new Label("Kalibrieren"));
			center.add(jPan3);
			center.add(new Label("Remote control"));
			center.add(jPan1);
			center.add(new JLabel("Linie folgen Befehle"));
			center.add(jPan2);
			center.add(lSpeed);
			center.add(jsSpeed);
			
			
			east.add(lConnect);
			east.add(lHell);
			east.add(lDunkel);
			east.add(lAbweichung);
			east.add(lTurn);
			east.add(lDistance);
			east.add(lIAbweichung);
			
			frame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					followLine=false;
					try {
						mControl.stop();
					} catch (RemoteException e1) {
						e1.printStackTrace();
					}
					beenden();
					e.getWindow().dispose();
				}
			});
			frame.add(BorderLayout.EAST, east);
			frame.add(BorderLayout.CENTER, center);
			frame.pack();
			frame.setVisible(true);
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public static void setup() {
		try {
			if (ev3==null) {
//				ev3=new RemoteEV3("10.0.1.1");
				ev3 = new RemoteEV3("192.168.0.210");
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
				
		if (motorA==null) {
			motorA = ev3.createRegulatedMotor("A", 'L');
		}
		if (motorB==null) {
			motorB = ev3.createRegulatedMotor("B", 'L');
		}
		if (motorC==null) {
//			motorC = ev3.createRegulatedMotor("C, 'L');
		}
		if (motorD==null) {
//			motorD = ev3.createRegulatedMotor("D", 'L');
		}
		mControl = new MotorControl(motorA, motorB);
		if (farbSensor!=null) {
			try {
				farbSensor.close();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		if (rotModus) {
			farbSensor = ev3.createSampleProvider("S1", "lejos.hardware.sensor.EV3ColorSensor", "Red");
		}else {
			farbSensor = ev3.createSampleProvider("S1", "lejos.hardware.sensor.EV3ColorSensor", "RGB");
		}
		if (ultraSensor != null) {
			try {
				ultraSensor.close();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		ultraSensor=ev3.createSampleProvider("S2", "lejos.hardware.sensor.EV3UltrasonicSensor", "Distance");
		if (gyroSensor != null) {
			try {
				gyroSensor.close();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
//			ev3.createSampleProvider("S3", "lejos.hardware.sensor.EV3GyroSensor", "AngleAndRate");
		rMoves = new RobotMoves(farbSensor, mControl, ultraSensor, gyroSensor, speed);		
		System.out.println("Setup fetig");
	}
	
	public static void main(String[] args) {
		new RobotControl();
	}
	public static void beenden() {
		try {
			System.out.println("beende Motoren:");
			if(motorA!=null) {
				System.out.println("MotorA laeuft noch");
				motorA.close();
				motorA=null;
				System.out.println("MotorA wurde beendet");
			}
			if(motorB!=null) {
				System.out.println("MotorB laeuft noch");
				motorB.close();
				motorB=null;
				System.out.println("MotorB wurde beendet");
			}
			if(motorC!=null) {
				System.out.println("MotorC laeuft noch");
				motorC.close();
				motorC=null;
				System.out.println("MotorC wurde beendet");
			}
			if(motorD!=null) {
				System.out.println("MotorD laeuft noch");
				motorD.close();
				motorD=null;
				System.out.println("MotorD wurde beendet");
			}
			if(farbSensor!=null) {
				System.out.println("Farbsensor laeuft noch");
				farbSensor.close();
				farbSensor=null;
				System.out.println("FarbSensor wurde beendet");
			}
			if(ultraSensor!=null) {
				System.out.println("UltraSensor laeuft noch");
				ultraSensor.close();
				ultraSensor=null;
				System.out.println("UltraSensor wurde beendet");
			}
			if (gyroSensor!=null) {
				System.out.println("gyroSensor laeuft noch");
				gyroSensor.close();
				gyroSensor=null;
				System.out.println("gyroSensor wurde beendet");
			}
			System.out.println("Alle Sensoren erfolgreich beendet!");
		} catch (RemoteException e) {
			e.printStackTrace();
			System.out.println("Fehler beim beenden");
		}
	}
	public static void setCurrentValues(float abweichung, int turn, float[] distance, float iAbweichung) {
		lAbweichung.setText("Abweichung: " +abweichung);
		lTurn.setText("Turn: " + turn);
		String txt="";
//		for (int i = 0; i < distance.length; i++) {
//			txt+=distance[i] + "\n";
//		}
		lDistance.setText("Distance: " + txt);
		lIAbweichung.setText("I-Abweichung: " + iAbweichung);
	}
	private void startFollowLine() {
		followLine=true;
		mControl.drive(5,0);
		rMoves.setStep(StepEnum.START);
		followLineThread = new Thread() {
			public void run() {
				while (followLine) {
					try {
						rMoves.followLine();
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
		};
		followLineThread.start();
	}
	private void stopFollowLine() {
		followLine=false;
	}
}
