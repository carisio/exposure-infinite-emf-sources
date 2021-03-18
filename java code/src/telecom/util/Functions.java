package telecom.util;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.log10;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static telecom.util.Constants.DEGREE_TO_RAD;
import static telecom.util.Constants.RAD_TO_DEGREE;

import java.util.Random;

public class Functions {
	
	public static boolean between(double min, double val, double max) {
		return val >= min && val <= max;
	}
	public static double dB2Watt(double db) {
		return pow(10, db/10);
	}
	public static double[] dB2Watt(double[] db) {
		double watt[] = new double[db.length];
		for (int i = 0; i < db.length; i++)
			watt[i] = pow(10, db[i]/10);
		return watt;
	}
	public static double watt2dB(double watt) {
		return 10*log10(watt);
	}

	public static double[] watt2dB(double[] watt) {
		double db[] = new double[watt.length];
		for (int i = 0; i < watt.length; i++)
			db[i] = 10*log10(watt[i]);
		return db;
	}
	public static double power2electricfield(double potenciaWatt, double freqMHz) {
		double lambda = 3e8/(freqMHz*1e6);
		double aff = (lambda*lambda)/(4*PI);
		return sqrt(377*potenciaWatt/aff);
	}
	public static double deg2rad(double deg) {
		return deg * DEGREE_TO_RAD;
	}
	public static double[] deg2rad(double[] deg) {
		double rad[] = new double[deg.length];
		for (int i = 0; i < deg.length; i++)
			rad[i] = deg[i] * DEGREE_TO_RAD;
		return rad;
	}
	public static double rad2deg(double rad) {
		return rad * RAD_TO_DEGREE;
	}
	public static double[] rad2deg(double[] rad) {
		double deg[] = new double[rad.length];
		for (int i = 0; i < rad.length; i++)
			deg[i] = rad[i] * RAD_TO_DEGREE;
		return deg;
	}
	public static double getICNIRPLimits(double f) {
		if (f < 10) return 83;
		if (f < 400) return 28;
		if (f < 2000) return 1.375 * pow(f, 0.5);
		if (f < 300000) return 61;
		return 61;
	}

	public static double get2DDistanceKM(Point3D p1, Point3D p2) {
		return sqrt(pow(p1.x - p2.x, 2) + pow(p1.y - p2.y, 2))/1000.0;
	}

	public static double get3DDistanceKM(Point3D p1, Point3D p2) {
		return sqrt(pow(p1.x - p2.x, 2) + pow(p1.y - p2.y, 2) + pow(p1.z - p2.z, 2))/1000.0;
	}

	
	/**
	 * Returns the elevation angle (theta) between the point bs and the point probe. Theta is the angle
	 * between the horizon and the line of sign between the points bs and probe. If htx is the height 
	 * of the point bs and hrx is the height of the point probe, the elevation angle is given by:
	 * 
	 * 
	 * teta_degree = atan2(htx-hrx, d_2d) * 180 / PI
	 * 
	 * 		^
	 * 		|
	 * 		|
	 * 	htx	x\----------------------> Horizon
	 * 		| \  
	 * 		|  \
	 * 		|   \   -----> Line of sign between bs and probe
	 * 		|    \
	 * 	hrx	|     \x---> Probe
	 * 		0------|------------------> x
	 *           d_2d
	 * 
	 * @return
	 */
	public static double getThetaDegree(Point3D bs, Point3D probe) {
		double htx = bs.z;
		double hrx = probe.z;
		double d_2d = get2DDistanceKM(bs, probe);
		double teta_degree = atan2(htx-hrx, d_2d*1000) * RAD_TO_DEGREE;
		
		return teta_degree;
	}
	public static double getThetaRad(Point3D bs, Point3D probe) {
		double htx = bs.z;
		double hrx = probe.z;
		double d_2d = get2DDistanceKM(bs, probe);
		double teta_degree = atan2(htx-hrx, d_2d*1000);
		
		return teta_degree;
	}
	/**
	 * Returns the azimuth angle between the points bs and probe. The reference (0 degree) is the y axis 
	 * and the angle increases clockwise until it reaches the line connecting the bs and probe (see figure).
	 * 
	 * 
	 * 		North (latitude)
	 * 			^
	 * 			|       o
	 * 			|      /
	 * 			|_phi /
	 * 			|  \ /
	 * 			|   /
	 * 			|  /
	 * 			| / 
	 * 			|/
	 * 	--------X-------------------------------> East (longitude)
	 * 			|
	 * 			|
	 * Note: The point bs is the orign, represented with an 'X'. The probe is represented with an 'o'.
	 */
	public static double getPhiDegree(Point3D bs, Point3D probe) {
		double phi_degree = 90 - atan2(probe.y - bs.y, probe.x - bs.x) * RAD_TO_DEGREE;
		
		if (phi_degree < 0) { phi_degree += 360; }
		
		return phi_degree;
	}
	
	public static void main(String args[]) {
		Point3D bs = new Point3D(0, 0, 0);
		Point3D probe = new Point3D(0, 1, 1);
		System.out.println("Should be 0: " + Functions.getPhiDegree(bs, probe));
		
		probe = new Point3D(1, 1, 1);
		System.out.println("Should be 45: " + Functions.getPhiDegree(bs, probe));
		
		probe = new Point3D(1, 0, 1);
		System.out.println("Should be 90: " + Functions.getPhiDegree(bs, probe));
	
		probe = new Point3D(1, -1, 1);
		System.out.println("Should be 135: " + Functions.getPhiDegree(bs, probe));
		
		probe = new Point3D(0, -1, 1);
		System.out.println("Should be 180: " + Functions.getPhiDegree(bs, probe));
		
		probe = new Point3D(-1, -1, 1);
		System.out.println("Should be 225: " + Functions.getPhiDegree(bs, probe));
		
		probe = new Point3D(-1, 0, 1);
		System.out.println("Should be 270: " + Functions.getPhiDegree(bs, probe));
		
		probe = new Point3D(-1, 1, 1);
		System.out.println("Should be 315: " + Functions.getPhiDegree(bs, probe));
	}
	
}
