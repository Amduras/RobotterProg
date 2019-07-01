package ControlGUI;

public class Richtungsmerker {
	private long time=0;
	private EnumRichtung richtung=EnumRichtung.START;
	Richtungsmerker(long time, EnumRichtung richtung){
		this.setTime(time);
		this.setRichtung(richtung);
	}
	public float getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public EnumRichtung getRichtung() {
		return richtung;
	}
	public void setRichtung(EnumRichtung richtung) {
		this.richtung = richtung;
	}
}
