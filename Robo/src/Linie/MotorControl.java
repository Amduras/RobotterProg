package Linie;

import java.rmi.RemoteException;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.remote.ev3.RMIRegulatedMotor;
/**
 * 
 * @author seeme
 *
 */
public class MotorControl {
	RMIRegulatedMotor motorA;
	RMIRegulatedMotor motorB;
	/*
	 * @param motorA -> rechter Motor
	 * @param motorB -> linker Motor
	 */
	public MotorControl(RMIRegulatedMotor motorA, RMIRegulatedMotor motorB) {
		this.motorA=motorA;
		this.motorB=motorB;
	}
	/**
	 * turn>0 -> rechts Kurve
	 * turn<0 -> links Kurve
	 * @param speed
	 * @param turn
	 */
	public void drive(int speed, int turn){
		float multiplier = 2*speed/100;
		int speedmodifier = (int) (multiplier*turn);
		try {
			//rechts Kurve
			if (turn>=0) {
			motorA.setSpeed(speed);
			motorA.forward();
			if (speed-speedmodifier>0) {
				motorB.setSpeed(speed-speedmodifier);
				motorB.forward();
			}else {
				motorB.setSpeed(-(speed-speedmodifier));
				motorB.backward();
			}
			}
			//links Kurve
			if (turn<0) {
				if (speed+speedmodifier>0) {
					motorA.setSpeed(speed+speedmodifier);
					motorA.forward();
				}else {
					motorA.setSpeed(-(speed+speedmodifier));
					motorA.backward();
				}
				motorB.setSpeed(speed);
				motorB.forward();
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

