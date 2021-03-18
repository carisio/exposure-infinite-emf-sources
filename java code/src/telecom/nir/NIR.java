package telecom.nir;

/**
 * Based on ITU-R K.100
 */
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.pow;
import static telecom.util.Functions.dB2Watt;
import static telecom.util.Functions.getICNIRPLimits;
import static telecom.util.Functions.power2electricfield;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Vector;

import telecom.basestation.BaseStation;
import telecom.propagation.PropagationModel;
import telecom.util.Functions;
import telecom.util.Pair;
import telecom.util.Point2D;
import telecom.util.Point3D;
import telecom.util.SimpleMatrix;

public class NIR {
	private Vector<Pair<BaseStation, PropagationModel>> baseStations;
	
	public NIR() {
		baseStations = new Vector<Pair<BaseStation, PropagationModel>>();
	}
	public void clearBaseStations() {
		baseStations.clear();
	}
	public void addBaseStation(BaseStation bs, PropagationModel pm) {
		baseStations.add(new Pair<BaseStation, PropagationModel>(bs, pm));
	}
	// Consider only the base stations that are at least minDist meters apart form the probe
	// To consider all base stations, use minDist = 0
	public Pair<Double, Double> evalEandTERAtProbe(Point3D probe, double minDist) {
		double E_field_total = 0;
		double TER = 0;
		
		for (Pair<BaseStation, PropagationModel> pairBsPm : baseStations) {
			BaseStation bs = pairBsPm.getFirst();
			PropagationModel pm = pairBsPm.getSecond();

			int NRadioSources = bs.getNRadioSources();
			double[] freq_mhz = bs.getFrequencyMHz();
			double[] eirpToProbe_dBm = bs.getEIRPdBm(probe);
			for (int i = 0; i < NRadioSources; i++) {
				Point3D txPos = new Point3D(bs.getX(), 
						bs.getY(), bs.getHeight()[i]);
				double rxIsotropicPower = eirpToProbe_dBm[i] - 30 
						- pm.getPathLoss(txPos, probe, freq_mhz[i]);

				double E_field_bs_i = power2electricfield(dB2Watt(rxIsotropicPower), freq_mhz[i]);
				double ER_bs_i = pow((E_field_bs_i/getICNIRPLimits(freq_mhz[i])), 2);

				double dist = Functions.get2DDistanceKM(txPos, probe);
				if (dist*1000 > minDist) {
					E_field_total += pow(E_field_bs_i, 2);
					TER += ER_bs_i;	
				}
			}
		}
		E_field_total = pow(E_field_total, 0.5);
		TER *= 100;

		return new Pair<Double, Double>(E_field_total, TER);
	}
}
