package telecom.propagation;

import static java.lang.Math.*;
import static telecom.util.Functions.get3DDistanceKM;

import java.util.Random;

import telecom.util.Functions;
import telecom.util.Point3D;

public class HataUrban extends PropagationModel {
	
	public HataUrban() { }
	
	private double a(double Hm, double freqMHz) {
		return (1.1*log10(freqMHz) - 0.7) * min(10, Hm) - (1.56 * log10(freqMHz) - 0.8) + max(0, 20*log10(Hm/10));
	}
	private double b(double Hb) {
		return min(0, 20*log10(Hb/30));
	}
	private double pathLossLessThan40meters(double freqMHz, double d_km, double Hb, double Hm) {
		return 32.4 + 20*log10(freqMHz) + 10*log10(d_km*d_km + Math.pow((Hb - Hm), 2)/1e6);
	}
	private double pathLossGreaterThan100meters(double freqMHz, double d_km, double Hb, double Hm) {
		double L = 0;
		if (freqMHz >= 150 && freqMHz <= 1500) {
			// Valid for d <= 20km, alpha = 1
			L = 69.6 + 26.2*log10(freqMHz) - 13.82*log10(max(30, Hb)) + 
					(44.9 - 6.55*log10(max(30, Hb)))*(log10(d_km)) - a(Hm, freqMHz) - b(Hb);
		} else if (freqMHz > 1500 && freqMHz <= 2000) {
			// Valid for d <= 20km, alpha = 1
			L = 46.3 + 33.9*log10(freqMHz) - 13.82*log10(max(30, Hb)) + 
					(44.9 - 6.55*log10(max(30, Hb)))*(log10(d_km)) - a(Hm, freqMHz) - b(Hb);			
		} else if (freqMHz > 2000 && freqMHz <= 3000) {
			// Valid for d <= 20km, alpha = 1
			L = 46.3 + 33.9*log10(2000) + 10*log10(freqMHz/2000) - 13.82*log10(max(30, Hb)) + 
					(44.9 - 6.55*log10(max(30, Hb)))*(log10(d_km)) - a(Hm, freqMHz) - b(Hb);				
		}
		return L;
	}
	// Implementation of https://wiki.cept.org/display/SH/A17.3.1+Outdoor-outdoor+propagation
	@Override
	public double getPathLoss(Point3D txPosition, Point3D rxPosition, double freqMHz) {
		double d_km = get3DDistanceKM(txPosition, rxPosition);
		if (d_km > 0.1103034211198208 && d_km < 0.1103034211198209) {
			System.out.println(d_km);
		}
		double Hb = max(txPosition.z, rxPosition.z);
		double Hm = min(txPosition.z, rxPosition.z);
		
		double L = 0;
		if (d_km <= 0.04) {
			L = pathLossLessThan40meters(freqMHz, d_km, Hb, Hm);
		} else if (d_km >= 0.1) {
			L = pathLossGreaterThan100meters(freqMHz, d_km, Hb, Hm);
		} else {
			double L_40meters = pathLossLessThan40meters(freqMHz, 0.04, Hb, Hm);
			double L_100meters = pathLossGreaterThan100meters(freqMHz, 0.1, Hb, Hm);
			L = L_40meters + (log10(d_km) - log10(0.04))/(log10(0.1) - log10(0.04)) * (L_100meters - L_40meters);
		}

		return L;
	}

	public static void main(String args[]) {
		HataUrban hu = new HataUrban();
		FreeSpace fs = new FreeSpace();
		Point3D tx = new Point3D(0, 0, 30);
		double freq = 700;
		
		for (int i = 10; i < 1000; i++) {
			Point3D rx = new Point3D(i, 0, 1.5);
			System.out.println(Functions.get3DDistanceKM(tx, rx) + "\t" + hu.getPathLoss(tx, rx, freq) + "\t" + fs.getPathLoss(tx, rx, freq));			
		}
	}
}
