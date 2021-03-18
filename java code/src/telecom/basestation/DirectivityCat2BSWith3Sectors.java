package telecom.basestation;

import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static telecom.util.Functions.between;
import static telecom.util.Functions.deg2rad;
import static telecom.util.Functions.watt2dB;

import java.util.ArrayList;

import telecom.util.Functions;
import telecom.util.Parser;
import telecom.util.Point2D;
import telecom.util.Point3D;

/**
 * This class represents a Base Station with a "Directivity category 2" antenna type, as
 * described in section IV.2.2 of Rec. ITU-T K.52 [1]. The main lobe is modeled as described
 * in section IV.2.2. According to [1], the side lobe can be described only by a constant
 * envelope or by a dipole factor. The side lobe was modulated by a short dipole
 * (cosine square) - see equation (5a) of [2].
 *
 * This original implementation of this class can be found in [3]. It uses latitude and longitude
 * to position the base station. This formulation was changed to position the base station in a
 * (x, y) plane, in meters. Moreover, now the horizontal plane uses the 3GPP 3-sector model exported
 * from Seamcat v5.
 * 
 * The 3D radiation pattern model is extracted from the H-plane and V-plane using the product model
 * (in dB, the implementation is a sum).  
 * 
 * [1] Recommendation ITU-T K.52 - Guidance on complying with limits for human
 * exposure to electromagnetic fields
 * [2] Estimating the location of maximum exposure to electromagnetic fields 
 * associated with a radiocommunication station
 * D.O.I http://dx.doi.org/10.1590/S2179-10742013000100012 
 * [3] https://github.com/carisio/emf-exposure/blob/master/app/src/main/java/telecom/basestation/DirectivityCat2BS.java
 * 
 * @author Leandro Carísio
 *
 */
public class DirectivityCat2BSWith3Sectors extends BaseStation {
	private double[] tilt_degree;
	private double[] theta_bw_vertical_degree;
	private double[] theta_bw_vertical_rad;
	private double[] eirp_max_dbm;
	private double[] envelope_db;
	private double[] horizontal_rp = DirectivityCat2BSWith3Sectors.get3GPPHorizontalRP();
	private boolean useHorizontalRP = false;
	
	public DirectivityCat2BSWith3Sectors() {
		
	}
	public DirectivityCat2BSWith3Sectors(String name, Point2D position, 
			double[] height, double[] frequency_mhz, 
			double[] tilt_degree, double[] theta_bw_vertical_degree, 
			double[] eirp_max_dbm, double[] max_envelope_side_lobe_db,
			boolean useHorizontalRP) {
		super(name, position, height, frequency_mhz);
		setEirpMaxdBm(eirp_max_dbm);
		setTiltDegree(tilt_degree);
		setMaxSideLobeEnvelopedB(max_envelope_side_lobe_db);
		setThetaBwVerticalDegree(theta_bw_vertical_degree);
		this.useHorizontalRP = useHorizontalRP;
	}
	public DirectivityCat2BSWith3Sectors(Point2D position, 
			double[] height, double[] frequency_mhz, 
			double[] tilt_degree, double[] theta_bw_vertical_degree, 
			double[] eirp_max_dbm, double[] max_envelope_side_lobe_db, boolean useHorizontalRP) {
		this("", position, height, frequency_mhz, tilt_degree, theta_bw_vertical_degree, eirp_max_dbm, max_envelope_side_lobe_db, useHorizontalRP);
	}
	public double[] getEIRPdBm(Point3D probe) {
		int NRadioSources = getNRadioSources();
		double eirp[] = new double[NRadioSources];
		for (int i = 0; i < NRadioSources; i++) {
			Point3D radioSource = new Point3D(
					getX(), 
					getY(), 
					getHeight()[i]);
			double theta_vertical_rad = 
					Functions.getThetaRad(radioSource, probe);
			double Fv = getFvdB(theta_vertical_rad, 
					deg2rad(tilt_degree[i]), 
					theta_bw_vertical_rad[i], 
					envelope_db[i]);
			double Fh = useHorizontalRP ? getFhdB(Functions.getPhiDegree(radioSource, probe)) : 0;
			eirp[i] = eirp_max_dbm[i] + Fv + Fh;
		}
		return eirp;
	}
	
	private double getFvdB(double theta_rad, double tilt_rad, 
			double theta_bw_vertical_rad, double envelope_db) {
		double firstNull = 2.257*theta_bw_vertical_rad/2;
		double firstNullMin = tilt_rad - firstNull;
		double firstNullMax = tilt_rad + firstNull;
		boolean mainBeam = between(firstNullMin, 
				theta_rad, firstNullMax);

		if (between(tilt_rad - 0.00175, 
				theta_rad, tilt_rad + 0.00175)) {
			return 0;
		}

		if (mainBeam) {
			double c = 1.392/sin(theta_bw_vertical_rad/2);
			double aux = c*sin(theta_rad-tilt_rad);
			double FLinear = pow(sin(aux)/aux, 2);

			double FdB = watt2dB(FLinear);
			if (FdB < envelope_db)
				FdB = envelope_db;
			return FdB;
		} else {
			return envelope_db;
		}
	}
	
	private double getFhdB(double angle) {	
		// angle should be between 0 and 360
		int iAngle = (int)Math.floor(angle);
		int fAngle = (int)Math.ceil(angle);
		double diff = angle - iAngle;
		
		if (iAngle == 360) { return horizontal_rp[360]; }
		if (iAngle == fAngle) { return horizontal_rp[iAngle]; }
		

		return horizontal_rp[iAngle] + (diff)/(fAngle - iAngle)*(horizontal_rp[fAngle] - horizontal_rp[iAngle]);
	}
	
	@Override
	public String doToString(String separator) {
		String result = Parser.codeDoubleArray(getTiltDegree()) + separator +
				Parser.codeDoubleArray(getThetaBwVerticalDegree()) + separator + 
				Parser.codeDoubleArray(getEirpMaxdBm()) + separator +
				Parser.codeDoubleArray(getMaxSideLobeEnvelopedB()) + separator;
		return result;
	}
	@Override
	public void doFromString(ArrayList<String> strings) {
		setTiltDegree(Parser.uncodeDoubleArray(strings.remove(0)));
		setThetaBwVerticalDegree(Parser.uncodeDoubleArray(strings.remove(0)));
		setEirpMaxdBm(Parser.uncodeDoubleArray(strings.remove(0)));
		setMaxSideLobeEnvelopedB(Parser.uncodeDoubleArray(strings.remove(0)));
	}
	
	public void setMaxSideLobeEnvelopedB(double[] envelope) {
		envelope_db = envelope;
	}
	public double[] getMaxSideLobeEnvelopedB() {
		if (envelope_db == null)
			envelope_db = new double[0];
		return envelope_db;
	}
	public double[] getEirpMaxdBm() {
		if (eirp_max_dbm == null)
			eirp_max_dbm = new double[0];
		return eirp_max_dbm;
	}
	public void setEirpMaxdBm(double[] eirpMaxdbm) {
		eirp_max_dbm = new double[eirpMaxdbm.length];
		for (int i = 0; i < eirp_max_dbm.length; i++)
			eirp_max_dbm[i] = eirpMaxdbm[i];
	}
	public void setThetaBwVerticalDegree(double[] thetaDeg) {
		theta_bw_vertical_degree = thetaDeg;
		theta_bw_vertical_rad = deg2rad(theta_bw_vertical_degree);
	}
	public double[] getThetaBwVerticalDegree() {
		if (theta_bw_vertical_degree == null)
			theta_bw_vertical_degree = new double[0];

		return theta_bw_vertical_degree;
	}
	public double[] getTiltDegree() {
		if (tilt_degree == null)
			tilt_degree = new double[0];

		return tilt_degree;
	}
	public void setTiltDegree(double[] tiltDegree) {
		tilt_degree = tiltDegree;
	}
	
	public static double[] get3GPPHorizontalRP() {
		return new double[] {0.162091634,
				0.159411401,
				0.151287422,
				0.137721542,
				0.11871656,
				0.094276343,
				0.064405855,
				0.029111188,
				-0.011600386,
				-0.05772037,
				-0.109238976,
				-0.166145052,
				-0.228426002,
				-0.29606769,
				-0.369054335,
				-0.447368393,
				-0.530990418,
				-0.619898921,
				-0.714070194,
				-0.813478133,
				-0.918094024,
				-1.027886325,
				-1.142820404,
				-1.262858263,
				-1.387958229,
				-1.518074612,
				-1.653157326,
				-1.793151477,
				-1.937996898,
				-2.087627652,
				-2.241971471,
				-2.400949146,
				-2.564473864,
				-2.732450467,
				-2.904774656,
				-3.081332118,
				-3.261997564,
				-3.446633708,
				-3.635090137,
				-3.82720211,
				-4.02278926,
				-4.221654211,
				-4.423581105,
				-4.628334051,
				-4.835655507,
				-5.045264592,
				-5.256855379,
				-5.47009516,
				-5.589884376,
				-5.747300567,
				-5.895640536,
				-6.033507554,
				-6.159498079,
				-6.272234403,
				-6.370401884,
				-6.452789293,
				-6.518330074,
				-6.566142018,
				-6.595562347,
				-6.606175487,
				-6.594404494,
				-6.564975717,
				-6.517160134,
				-6.45162039,
				-6.369238443,
				-6.271080504,
				-6.15835738,
				-6.032383246,
				-5.894535316,
				-5.746216633,
				-5.588823446,
				-5.469047619,
				-5.255842983,
				-5.044285702,
				-4.834708496,
				-4.627417314,
				-4.422693066,
				-4.220793331,
				-4.021954039,
				-3.826391093,
				-3.634301915,
				-3.445866919,
				-3.261250894,
				-3.0806043,
				-2.904064469,
				-2.731756735,
				-2.563795455,
				-2.400284971,
				-2.241320478,
				-2.086988829,
				-1.937369266,
				-1.79253409,
				-1.652549269,
				-1.517474996,
				-1.387366191,
				-1.262272961,
				-1.142241016,
				-1.027312047,
				-0.917524065,
				-0.812911714,
				-0.713506546,
				-0.61933728,
				-0.530430024,
				-0.446808487,
				-0.368494157,
				-0.295506474,
				-0.227862975,
				-0.16557943,
				-0.10866996,
				-0.057147147,
				-0.011022119,
				0.029695359,
				0.064996816,
				0.094875015,
				0.119323898,
				0.138338543,
				0.151915128,
				0.160050904,
				0.162744084,
				0.162729941,
				0.162744084,
				0.160050904,
				0.151915128,
				0.138338543,
				0.119323898,
				0.094875015,
				0.064996816,
				0.029695359,
				-0.011022119,
				-0.057147147,
				-0.10866996,
				-0.16557943,
				-0.227862975,
				-0.295506474,
				-0.368494157,
				-0.446808487,
				-0.530430024,
				-0.61933728,
				-0.713506546,
				-0.812911714,
				-0.917524065,
				-1.027312047,
				-1.142241016,
				-1.262272961,
				-1.387366191,
				-1.517474996,
				-1.652549269,
				-1.79253409,
				-1.937369266,
				-2.086988829,
				-2.241320478,
				-2.400284971,
				-2.563795455,
				-2.731756735,
				-2.904064469,
				-3.0806043,
				-3.261250894,
				-3.445866919,
				-3.634301915,
				-3.826391093,
				-4.021954039,
				-4.220793331,
				-4.422693066,
				-4.627417314,
				-4.834708496,
				-5.044285702,
				-5.255842983,
				-5.469047619,
				-5.588823446,
				-5.746216633,
				-5.894535316,
				-6.032383246,
				-6.15835738,
				-6.271080504,
				-6.369238443,
				-6.45162039,
				-6.517160134,
				-6.564975717,
				-6.594404494,
				-6.605030897,
				-6.596704443,
				-6.566142018,
				-6.518330074,
				-6.452789293,
				-6.370401884,
				-6.272234403,
				-6.159498079,
				-6.033507554,
				-5.895640536,
				-5.747300567,
				-5.589884376,
				-5.47009516,
				-5.256855379,
				-5.045264592,
				-4.835655507,
				-4.628334051,
				-4.423581105,
				-4.221654211,
				-4.02278926,
				-3.82720211,
				-3.635090137,
				-3.446633708,
				-3.261997564,
				-3.081332118,
				-2.904774656,
				-2.732450467,
				-2.564473864,
				-2.400949146,
				-2.241971471,
				-2.087627652,
				-1.937996898,
				-1.793151477,
				-1.653157326,
				-1.518074612,
				-1.387958229,
				-1.262858263,
				-1.142820404,
				-1.027886325,
				-0.918094024,
				-0.813478133,
				-0.714070194,
				-0.619898921,
				-0.530990418,
				-0.447368393,
				-0.369054335,
				-0.29606769,
				-0.228426002,
				-0.166145052,
				-0.109238976,
				-0.05772037,
				-0.011600386,
				0.029111188,
				0.064405855,
				0.094276343,
				0.11871656,
				0.137721542,
				0.151287422,
				0.159411401,
				0.162091634,
				0.162063752,
				0.162063752,
				0.159355572,
				0.151203445,
				0.137609077,
				0.118575127,
				0.094105319,
				0.064204467,
				0.028878508,
				-0.011865449,
				-0.058019077,
				-0.109572771,
				-0.166515574,
				-0.228835096,
				-0.296517424,
				-0.369547019,
				-0.447906595,
				-0.531576992,
				-0.620537026,
				-0.714763327,
				-0.814230156,
				-0.918909203,
				-1.028769367,
				-1.1437765,
				-1.263893139,
				-1.3890782,
				-1.51928664,
				-1.65446909,
				-1.794571447,
				-1.93953442,
				-2.089293038,
				-2.243776106,
				-2.4029056,
				-2.566596023,
				-2.734753671,
				-2.907275859,
				-3.084050058,
				-3.26495296,
				-3.449849469,
				-3.638591596,
				-3.831017281,
				-4.026949118,
				-4.226192999,
				-4.428536667,
				-4.633748197,
				-4.841574398,
				-5.05173916,
				-5.263941768,
				-5.477855197,
				-5.69312442,
				-5.809652096,
				-5.965400229,
				-6.111242164,
				-6.245744245,
				-6.367479873,
				-6.475065576,
				-6.567201244,
				-6.642712685,
				-6.700593939,
				-6.740046544,
				-6.760512598,
				-6.761698913,
				-6.740046544,
				-6.700593939,
				-6.642712685,
				-6.567201244,
				-6.475065576,
				-6.367479873,
				-6.245744245,
				-6.111242164,
				-5.965400229,
				-5.809652096,
				-5.69312442,
				-5.477855197,
				-5.263941768,
				-5.05173916,
				-4.841574398,
				-4.633748197,
				-4.428536667,
				-4.226192999,
				-4.026949118,
				-3.831017281,
				-3.638591596,
				-3.449849469,
				-3.26495296,
				-3.084050058,
				-2.907275859,
				-2.734753671,
				-2.566596023,
				-2.4029056,
				-2.243776106,
				-2.089293038,
				-1.93953442,
				-1.794571447,
				-1.65446909,
				-1.51928664,
				-1.3890782,
				-1.263893139,
				-1.1437765,
				-1.028769367,
				-0.918909203,
				-0.814230156,
				-0.714763327,
				-0.620537026,
				-0.531576992,
				-0.447906595,
				-0.369547019,
				-0.296517424,
				-0.228835096,
				-0.166515574,
				-0.109572771,
				-0.058019077,
				-0.011865449,
				0.028878508,
				0.064204467,
				0.094105319,
				0.118575127,
				0.137609077,
				0.151203445,
				0.159355572,
				0.162063752,
				0.162063752};
	}
}
