package telecom.tests;

import telecom.basestation.BaseStation;
import telecom.basestation.DirectivityCat2BSWith3Sectors;
import telecom.nir.NIR;
import telecom.propagation.FreeSpace;
import telecom.propagation.PropagationModel;
import telecom.util.Pair;
import telecom.util.Point2D;
import telecom.util.Point3D;

import static java.lang.Math.*;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Test {
	// bsDensity: Minimum BS density (bs/km2)
	// hBS: base station height (meters)
	// freq: transmitting frequency (MHz)
	// tilt: tilt of the base stations (degree)
	// teta_bw: vertical beamwidth (degree)
	// eirp: (dBm)
	// nProbes: number of probes
	// hProbe: height of the probes
	//
	// Return Pair<String, String> = <baseStations, probes>
	public static Pair<String, String> simulate(double bsDensity, double hBS,	double freq,
			double tilt, double teta_bw, double eirp, int nProbes,
			double hProbe) {
		// Initialize seed as 42 (an obvious reference to The Hitchhiker's Guide to the Galaxy)
		Random random = new Random();
		random.setSeed(42);
		
		// Analysis in a L x L square, where L is the side of the square in meters
		double L = 10000;
			
		// Total of base stations
		int nBS = (int)ceil(bsDensity*L*L/1e6);
		// Create propagation model (free space) and a nBS randmo base stations:
		NIR nir = new NIR();
		PropagationModel pm = new FreeSpace();
		List<BaseStation> bsList = new ArrayList<BaseStation>();
		
		// Result string
		StringBuffer sbErbs = new StringBuffer();
		StringBuffer sbProbes = new StringBuffer();
		
		for (int i = 0; i < nBS; i++) {
			double xBS = L*random.nextDouble();
			double yBS = L*random.nextDouble();
			
			boolean useHorizontalRP = true;
			
			BaseStation bs = new DirectivityCat2BSWith3Sectors(new Point2D(xBS, yBS),
													new double[] {hBS},
													new double[] {freq},
													new double[] {tilt},
													new double[] {teta_bw},
													new double[] {eirp},
													new double[] {-20}, 
													useHorizontalRP);
			bsList.add(bs);
			nir.addBaseStation(bs,  pm);
		}
		
		// Generate probes
		double[] x_probes = new double[nProbes];
		double[] y_probes = new double[nProbes];
		for (int i = 0; i < nProbes; i++) {		
			x_probes[i] = L/4 + (L/2)*random.nextDouble();
			y_probes[i] = L/4 + (L/2)*random.nextDouble();
		}
		
		// Consider only the base stations with d > distanceToNearestBS
		double distanceToNearestBS = 0;
		
		// Print results
		sbErbs.append("------------------------------\n");
		sbErbs.append("Base station positions\n");
		sbErbs.append("------------------------------\n");
		for (BaseStation bs: bsList) {
			sbErbs.append(bs.getX() + "\t" + bs.getY() + "\n");
		}
		sbProbes.append("------------------------------\n");
		sbProbes.append("Probe (position, E, TER\n");
		sbProbes.append("------------------------------\n");
		for (int i = 0; i < nProbes; i++) {
			Pair<Double, Double> eAndTer = nir.evalEandTERAtProbe(new Point3D(x_probes[i], y_probes[i], hProbe), distanceToNearestBS);
			sbProbes.append(x_probes[i] + "\t" + y_probes[i] + "\t" + eAndTer.getFirst() + "\t" + eAndTer.getSecond() + "\n");
		}
		return new Pair<String, String>(sbErbs.toString(), sbProbes.toString());
	}
	public static void main(String args[]) {
		// Scenario
		double bsDensity = 21;	
		int nProbes = 10000;
		double hProbe = 1.5;
		
		// BS characteristics:
		// 
		// Tilt based on Report ITU-R M. 2292:
		// 		For f < 1GHz, 3 degrees
		// 		For 1 GHz <= f <= 3 GHz, 10 degrees (macro urban)
		// Teta_bw based on real antenna models
		//		700/850 MHz: Kathrein type no. 80010735v01 
		//		1800/2100 MHz: Kathrein type no. 80010765v01
		//		2600 MHz: Kathrein type no. 80010685
		double[] freq = 	{700,	850,	1800,	2100,	2600};
		double[] eirp = 	{60,	60,		60,		60,		60};		
		double[] hBS =  	{30, 	30,		25,		20,		20};
		double[] tilt = 	{3,		3,		10,		10,		10};
		double[] teta_bw = 	{11.3,	10,		5.8,	5.8,	3.5};
		
		for (int i = 1; i < freq.length; i++) {
			simulateAndSaveResults(bsDensity, hBS[i], freq[i], tilt[i], teta_bw[i], eirp[i], nProbes, hProbe);			
		}
	}
	private static void simulateAndSaveResults(double bsDensity, double hBS,	double freq,
			double tilt, double teta_bw, double eirp, int nProbes,
			double hProbe) {
		System.out.println("Simulating: " + ((int)freq) + "MHz");
		
		Pair<String, String> result = simulate(bsDensity, hBS, freq, tilt, teta_bw, eirp, nProbes, hProbe);
		
		String bsFileName = "bs " + ((int)freq) + "MHz.txt";
		String nirFileName = "nir " + ((int)freq) + "MHz.txt";
		
		saveFile(bsFileName, result.getFirst());
		saveFile(nirFileName, result.getSecond());

		System.out.println("Finished: " + ((int)freq) + "MHz");
	}
	private static void saveFile(String fileName, String contents) {
		try {
			FileWriter file = new FileWriter(fileName);
			file.write(contents);
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
