package versuche;

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
	static RMIRegulatedMotor motorA;
	static RMIRegulatedMotor motorB;
	static RMIRegulatedMotor motorC;
	static RMIRegulatedMotor motorD;
	static RMISampleProvider ultraSensor;
	static RMISampleProvider farbSensor;
	static RMISampleProvider gyroSensor;
	static MotorControl mControl;
	static RemoteEV3 ev3;
	static RobotMoves rMoves;
	static volatile int speed=50;
	static volatile boolean followLine = false;
	static Label lAbweichung = new Label("Abweichung:");
	static Label lTurn = new Label("Turn:");
	static Label lDistance = new Label("Distance:");
	static Label lIAbweichung = new Label("I-Abweichung:");
	static boolean rotModus=false;
	
	public RobotControl(){
		try {
			setup();
			//View
			JFrame frame= new JFrame("Robot Control");
			frame.setSize(2000, 2000);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setLayout(new BorderLayout());
			
			JPanel center = new JPanel(new GridLayout(12, 0));
			JPanel east = new JPanel(new GridLayout(10, 0));
			
			Label lHell=new Label("Hell:");
			Label lDunkel=new Label("Dunkel:");
			Label lConnect = new Label("Verbindungsstatus: Disconnected");
			
			JRadioButton bStop= new JRadioButton("Stop", true);
			bStop.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					followLine = false;
					mControl.drive(0, 0);
					System.out.println("Stop");
				}
			});
			JRadioButton bZurueck= new JRadioButton("Zurueck");
			JButton bConnect= new JButton("Verbinden");
			bConnect.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
						RobotControl.setup();
						lConnect.setText("Verbindungsstatus: Connected");
				}
			});
			
			JButton bEnd= new JButton("Verbindung trennen");
			bEnd.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					followLine = false;
					beenden();
					lConnect.setText("Verbindungsstatus: Discconected");
				}
			});
			JRadioButton bRed= new JRadioButton("Rot-Modus");
			bConnect.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					rotModus=true;
					followLine=false;
					mControl.drive(0, 0);
					bStop.setSelected(true);
					if (farbSensor!=null) {
						try {
							farbSensor.close();
						} catch (RemoteException e2) {
							// TODO Auto-generated catch block
							e2.printStackTrace();
						}
					}
					farbSensor = ev3.createSampleProvider("S1", "lejos.hardware.sensor.EV3ColorSensor", "Red");
					rMoves.setRotModus(true, farbSensor);
					lHell.setText("Hell:");
					lDunkel.setText("Dunkel:");
				}
			});
			
			JRadioButton bRGB= new JRadioButton("RGB-Modus", true);
			bEnd.addActionListener(new ActionListener() {
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
							// TODO Auto-generated catch block
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
			JButton bRand= new JButton("Linie");
			bRand.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						followLine = false;
						rMoves.setArrayRand();
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
					mControl.drive(100, 0);
					System.out.println("Vorwaerts");
				}
			});
			JRadioButton bRueckwaerts= new JRadioButton("Rueckwaerts");
			bRueckwaerts.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					followLine = false;
					mControl.drive(-speed, 0);
					System.out.println("Rueckwaerts");
				}
			});
			JRadioButton bLinks= new JRadioButton("Links");
			bLinks.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					followLine = false;
					mControl.drive(speed, 100);
					System.out.println("Links Kurve");
				}
			});
			JRadioButton bRechts= new JRadioButton("Rechts");
			bRechts.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					followLine = false;
					mControl.drive(speed, -100);
					System.out.println("Rechts Kurve");
				}
			});
			bZurueck.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
//					try {
//						rMoves.zurueck();
//					} catch (RemoteException e1) {
//						e1.printStackTrace();
//					}
				}
			});
			JRadioButton bAusweichen= new JRadioButton("Ausweichen");
			bAusweichen.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					followLine = true;
					new Thread() {
						public void run() {
							while(followLine) {
								try {
									rMoves.setEvade(true);
									rMoves.followLine();
								} catch (RemoteException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
					}.start();
				}
			});
			JRadioButton bFolgen= new JRadioButton("Roboter folgen");
			bFolgen.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						followLine = true;
						rMoves.setEvade(false);
						rMoves.followLine();
					} catch (RemoteException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			});
			
			
			ButtonGroup manualButtons = new ButtonGroup();
			manualButtons.add(bVorwaerts);
			manualButtons.add(bRueckwaerts);
			manualButtons.add(bRechts);
			manualButtons.add(bLinks);
			manualButtons.add(bZurueck);
			manualButtons.add(bStop);
			manualButtons.add(bAusweichen);
			manualButtons.add(bFolgen);
			
			ButtonGroup modusButtons = new ButtonGroup();
			modusButtons.add(bRed);
			modusButtons.add(bRGB);
			
			JPanel jPan1 = new JPanel();
			jPan1.add(bVorwaerts);
			jPan1.add(bRueckwaerts);
			jPan1.add(bRechts);
			jPan1.add(bLinks);
			jPan1.add(bZurueck);
			jPan1.add(bStop);
			
			JPanel jPan2 = new JPanel();
			jPan2.add(bAusweichen);
			jPan2.add(bFolgen);

			JPanel jPan3 = new JPanel();
			//jPan3.add(bRand);
			jPan3.add(bHell);
			jPan3.add(bDunkel);
			
			JPanel jPan4 = new JPanel();
			jPan4.add(bConnect);
			jPan4.add(bEnd);

			JPanel jPanModus = new JPanel();
			jPanModus.add(bRed);
			jPanModus.add(bRGB);
			
			Label lSpeed = new Label("Speed Regulator: Currently: " + speed);
			JScrollBar jsSpeed = new JScrollBar(JScrollBar.HORIZONTAL, speed, 0, 1, speed*2);
			jsSpeed.addAdjustmentListener(e->{
				speed=jsSpeed.getValue();
	            rMoves.setSpeed(speed);
	            lSpeed.setText("Speed Regulator: Currently: " + speed);
	            System.out.println("Speed wurde geaendert: " + speed);
	        });

			center.add(new JLabel("Verbindung"));
			center.add(jPan4);
			center.add(new JLabel("Modus"));
			center.add(jPanModus);
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
	public static void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		Port portA = ev3.getPort("A");
//		Port portB = ev3.getPort("B");
//		Port portC = ev3.getPort("C");
//		Port portD = ev3.getPort("D");
//		Port portS1 = ev3.getPort("S1");
//		Port portS2 = ev3.getPort("S2");
		
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
				// TODO Auto-generated catch block
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		ultraSensor=ev3.createSampleProvider("S2", "lejos.hardware.sensor.EV3UltrasonicSensor", "Distance");
		if (gyroSensor != null) {
			try {
				gyroSensor.close();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
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
		for (int i = 0; i < distance.length; i++) {
			txt+=distance[i] + "\n";
		}
		lDistance.setText("Distance: " + txt);
		lIAbweichung.setText("I-Abweichung: " + iAbweichung);
		
	}
}
