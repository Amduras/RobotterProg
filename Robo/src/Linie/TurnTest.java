package Linie;

public class TurnTest {
	public static void main(String[] args) {
		turn(5, 3, (float)3.2);
		turn(5, 3, 4);
		turn(5, 3, (float)3.5);
		turn(5, 3, (float)4.5);
		turn(5,3,5);
		turn(5,3,3);
	}
	public static void turn(float high, float low, float value) {
		int turn=0;
		float differenz=high-low;
		float nvalue=value-low;
		float average=differenz/4;
		float relativValue=nvalue-average;
		float percentageValue= 0;
		if (relativValue<0) {
			percentageValue=relativValue/average;
		}else {
			percentageValue=relativValue/average/3;
		}
		float nturn= percentageValue*100;
		turn=(int)nturn;
		System.out.println("High: " + high + "\tLow: " + low + "\tWert" + value);
		System.out.println("nhigh: " + differenz + "\tnvalue: " + nvalue + "\taverage: " + average);
		System.out.println("relativValue: "+ relativValue + "\tpercentageValue: " + percentageValue);
		System.out.println("nturn: " + nturn);
		System.out.println("Turn: " + turn);
	}
}
