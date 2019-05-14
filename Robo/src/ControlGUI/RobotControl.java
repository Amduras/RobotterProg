package ControlGUI;

import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
	static MotorControl mControl;
	static RemoteEV3 ev3;
	static RobotMoves rMoves;
	static int speed=50;
	
	public RobotControl(){
		try {
			
			//View
			JFrame frame= new JFrame("Robot Control");
			frame.setSize(1000, 1000);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setLayout(new GridLayout(8, 0));
			
			Label lKalibrieren = new Label("Kalibrieren");
			Label l1 = new Label("Remote control");

			JButton bConnect= new JButton("Connect");
			bConnect.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
						RobotControl.setup();						
				}
			});
			
			JButton bHell= new JButton("Hell");
			bHell.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						rMoves.setArrayHell();
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
						rMoves.setArrayDunkel();
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
						rMoves.setArrayRand();
					} catch (RemoteException e1) {
						e1.printStackTrace();
					}
				}
			});
			JButton bVorw�rts= new JButton("Vorw�rts");
			bVorw�rts.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					mControl.drive(100, 0);
					System.out.println("Vorw�rts");
				}
			});
			JButton bR�ckw�rts= new JButton("R�ckw�rts");
			bR�ckw�rts.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					mControl.drive(-speed, 0);
					System.out.println("R�ck�rts");
				}
			});
			JButton bLinks= new JButton("Links");
			bLinks.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					mControl.drive(speed, 100);
					System.out.println("Links Kurve");
				}
			});
			JButton bRechts= new JButton("Rechts");
			bRechts.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					mControl.drive(speed, -100);
					System.out.println("rechts Kurve");
				}
			});
			JButton bStop= new JButton("Stop");
			bStop.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					mControl.drive(0, 0);
					System.out.println("Stop");
				}
			});
			JButton bZur�ck= new JButton("Zur�ck");
			bZur�ck.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
//					try {
//						rMoves.zurueck();
//					} catch (RemoteException e1) {
//						e1.printStackTrace();
//					}
				}
			});
			JButton bLinieFolgen= new JButton("Linie folgen");
			bLinieFolgen.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						rMoves.followLine();
					} catch (RemoteException e1) {
						e1.printStackTrace();
					}
				}
			});
			JButton bFollow= new JButton("Roboter folgen");
			bFollow.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						rMoves.followRobot();
					} catch (RemoteException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			});
			JButton bEnd= new JButton("verbindung trennen");
			bEnd.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					beeneden();
				}
			});
			
			
			JButton[] manualButtons = new JButton[6];
			manualButtons[0]=bVorw�rts;
			manualButtons[1]=bR�ckw�rts;
			manualButtons[2]=bRechts;
			manualButtons[3]=bLinks;
			manualButtons[4]=bZur�ck;
			manualButtons[5]=bStop;
			
			JPanel jPan1 = new JPanel();
			for (int i = 0; i < manualButtons.length; i++) {
				jPan1.add(manualButtons[i]);
			}
			JPanel jPan2 = new JPanel();
			jPan2.add(bLinieFolgen);
			jPan2.add(bFollow);
			jPan2.add(bEnd);

			JPanel jPan3 = new JPanel();
			jPan3.add(bConnect);
			jPan3.add(bRand);
			jPan3.add(bHell);
			jPan3.add(bDunkel);

			Label lSpeed = new Label("Speed Regulator: Currently: " + speed);
			JScrollBar jsSpeed = new JScrollBar(JScrollBar.HORIZONTAL, speed, 0, 1, 100);
			jsSpeed.addAdjustmentListener(e->{
				speed=jsSpeed.getValue();
	            rMoves.setSpeed(speed);
	            lSpeed.setText("Speed Regulator: Currently: " + speed);
	            System.out.println("Speed wurde ge�ndert: " + speed);
	        });
			
			frame.add(lKalibrieren);
			frame.add(jPan3);
			frame.add(l1);
			frame.add(jPan1);
			frame.add(new JLabel("Komplexe Befehle"));
			frame.add(jPan2);
			frame.add(lSpeed);
			frame.add(jsSpeed);
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
			ev3=new RemoteEV3("10.0.1.1");
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
		farbSensor = null;
		farbSensor = ev3.createSampleProvider("S1", "lejos.hardware.sensor.EV3ColorSensor", "RGB");
		ultraSensor = null; 
		//ev3.createSampleProvider("S2", "lejos.hardware.sensor.EV3UltrasonicSensor", "distance");
		rMoves = new RobotMoves(farbSensor, mControl, ultraSensor);		
		System.out.println("Setup fetig");
	}
	
	public static void main(String[] args) {
		new RobotControl();
	}
	public static void beeneden() {
		try {
			System.out.println("beende Motoren:");
			if(motorA!=null) {
				System.out.println("MotorA l�uft noch");
				motorA.close();
				System.out.println("MotorA wurde beendet");
			}
			if(motorB!=null) {
				System.out.println("MotorB l�uft noch");
				motorB.close();
				System.out.println("MotorB wurde beendet");
			}
			if(motorC!=null) {
				System.out.println("MotorC l�uft noch");
				motorC.close();
				System.out.println("MotorC wurde beendet");
			}
			if(motorD!=null) {
				System.out.println("MotorD l�uft noch");
				motorD.close();
				System.out.println("MotorD wurde beendet");
			}
			if(farbSensor!=null) {
				System.out.println("Farbsensor l�uft noch");
				farbSensor.close();
				System.out.println("FarbSensor wurde beendet");
			}
			if(ultraSensor!=null) {
				System.out.println("UltraSensor l�uft noch");
				ultraSensor.close();
				System.out.println("UltraSensor wurde beendet");
			}
			System.out.println("Alle Sensoren erfolgreich beendet!");
		} catch (RemoteException e) {
			e.printStackTrace();
			System.out.println("Fehler beim beenden");
		}
	}
}
