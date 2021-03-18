package telecom.basestation;

import java.util.ArrayList;
import java.util.StringTokenizer;

import telecom.util.Parser;
import telecom.util.Point2D;
import telecom.util.Point3D;

public abstract class BaseStation {
	private long id;
	private static long lastID = 0;
	private String name;
	private Point2D pos;
	private double[] height;
	private double[] frequency;

	public BaseStation() {
		this(null, null, null, null);
	}
	public BaseStation(Point2D position, double[] height, double[] frequency_mhz) {
		this("", position, height, frequency_mhz);
	}
	public BaseStation(String name, Point2D position, double[] height, double[] frequency_mhz) {
		setId();
		setName(name);
		setPosition(position);
		setHeight(height);
		setFrequencyMHz(frequency_mhz);
	}
	/**
	 * Returns the transmitted power in dB in the (teta, phi) direction
	 * @param teta_vertical_degree	The elevation angle between the measurement point and the base station. 
	 * 								If htx is the base station height and hrx is the measurment point height, 
	 * 								teta_vertical_degree is atan((htx-hrx)/d_2d)*180/PI, where d_2d is the
	 * 								projection of the distance between htx and hrx
	 * @param phi_horizontal_degree	The azimuth angle between the measurement point and the base station.
	 * 								The reference (0 degree) is the north and the angle increases clockwise.
	 * 								Setting the origin at the base station position, phi_horizontal_degree is
	 * 								phi_horizontal_degree = atan2(delta_longitude, delta_latidue)*180/PI,
	 * 								where delta_longitude = longitude(measurement point)
	 * 								and delta_latitude = latitude(measurement point)
	 * 								both delta_longitude and delta_latitude should be converter to length units.
	 * 								Note: result of atan2 can be negative. Depending on the implementation of
	 * 								the subclasses of BaseStation, it can be convenient to transform it
	 * 								to a positive value (angle = 360 + negative_value)
	 * 								
	 * 
	 * double teta_vertical_degree, double phi_horizontal_degree
	 * @return
	 */
	public abstract double[] getEIRPdBm(Point3D probe);
	
	public Point2D getPosition() {
		if (pos == null)
			pos = new Point2D(0, 0);
		return pos;
	}
	public void setPosition(Point2D pos) {
		this.pos = pos;
	}
	public double getX() {
		return getPosition().x;
	}
	public double getY() {
		return getPosition().y;
	}
	public int getNRadioSources() {
		return getFrequencyMHz().length;
	}
	public double[] getFrequencyMHz() {
		if (frequency == null) {
			frequency = new double[0];
			
		}
		return frequency;
	}
	public void setFrequencyMHz(double[] frequency) {
		this.frequency = frequency;
	}
	public double[] getHeight() {
		if (height == null) {
			 height = new double[0];
		}
		return height;
	}
	public void setHeight(double[] height) {
		this.height = height;
	}
	public String getName() {
		if (name == null)
			name = "";
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String toString() {
		String result = Parser.codeWhitespaces(getClass().getCanonicalName().toString()) + "\t"
				+ Parser.codeWhitespaces(getName()) + "\t"
				+ getPosition().x + "\t"
				+ getPosition().y + "\t"
				+ Parser.codeDoubleArray(getHeight()) + "\t"
				+ Parser.codeDoubleArray(getFrequencyMHz()) + "\t"
				+ doToString("\t");
		return result;
	}
	public String doToString(String separator) {
		return "";
	}
	public static BaseStation fromString(String str) throws Exception {
		String separator = "\t";
		StringTokenizer tokenizer = new StringTokenizer(str, separator);

		String classNameString = Parser.uncodeWhitespaces(tokenizer.nextToken());
		BaseStation bs = null;
		bs = (BaseStation) Class.forName(classNameString).newInstance();
		String name = Parser.uncodeWhitespaces(tokenizer.nextToken());
		double latitude = Double.parseDouble(tokenizer.nextToken());
		double longitude = Double.parseDouble(tokenizer.nextToken());
		double[] height = Parser.uncodeDoubleArray(tokenizer.nextToken());
		double[] freqMHz = Parser.uncodeDoubleArray(tokenizer.nextToken());
		
		bs.setName(name);
		bs.setPosition(new Point2D(latitude, longitude));
		bs.setHeight(height);
		bs.setFrequencyMHz(freqMHz);
		
		ArrayList<String> nextTokens = new ArrayList<String>();
		while (tokenizer.hasMoreTokens())
			nextTokens.add(tokenizer.nextToken());
		bs.doFromString(nextTokens);

		return bs;
	}
	public void doFromString(ArrayList<String> strings) {
	}
	private void setId() {
		lastID++;
		id = lastID;
	}
	public String getId() {
		return "" + id;
	}
}
