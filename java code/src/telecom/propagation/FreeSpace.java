package telecom.propagation;

import telecom.util.Point3D;
import static telecom.util.Functions.get3DDistanceKM;
import static java.lang.Math.log10;

public class FreeSpace extends PropagationModel {

	@Override
	public double getPathLoss(Point3D txPosition, Point3D rxPosition, double freqMHz) {
		double d_km = get3DDistanceKM(txPosition, rxPosition);
		double L = 32.44 + 20*log10(freqMHz) + 20*log10(d_km);
		return L;
	}
}
