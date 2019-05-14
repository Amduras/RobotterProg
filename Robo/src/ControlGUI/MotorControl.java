package versuche;

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
		int mSpeed;
		if (speed<0) {
			mSpeed=-speed;
		}else {
			mSpeed=speed;
		}
		float multiplier = 2*mSpeed/100;
		int speedmodifier = (int) (multiplier*turn);
		try {
			if (speed>=0) {
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
			}else {
				speed=-speed;
				//rechts Kurve
				if (turn<=0) {
					motorA.setSpeed(speed);
					motorA.backward();
					if (speed+speedmodifier>0) {
						motorB.setSpeed(speed+speedmodifier);
						motorB.backward();
					}else {
						motorB.setSpeed(-(speed+speedmodifier));
						motorB.forward();
					}
				}
				//links Kurve
				if (turn>0) {
					if (speed-speedmodifier>0) {
						motorA.setSpeed(speed-speedmodifier);
						motorA.backward();
					}else {
						motorA.setSpeed(-(speed-speedmodifier));
						motorA.forward();
					}
					motorB.setSpeed(speed);
					motorB.backward();
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}

