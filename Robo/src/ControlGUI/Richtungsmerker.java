package ControlGUI;

public class Richtungsmerker {
	private float time=0;
	private EnumRichtung richtung=EnumRichtung.START;
	Richtungsmerker(float time, EnumRichtung richtung){
		this.setTime(time);
		this.setRichtung(richtung);
	}
	public float getTime() {
		return time;
	}
	public void setTime(float time) {
		this.time = time;
	}
	public EnumRichtung getRichtung() {
		return richtung;
	}
	public void setRichtung(EnumRichtung richtung) {
		this.richtung = richtung;
	}
}
