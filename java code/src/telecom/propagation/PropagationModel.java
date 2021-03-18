package telecom.propagation;

import telecom.util.Point3D;

public abstract class PropagationModel {

	public PropagationModel() {
		
	}
	
	public abstract double getPathLoss(Point3D txPosition, Point3D rxPosition, double freqMHz);
}
