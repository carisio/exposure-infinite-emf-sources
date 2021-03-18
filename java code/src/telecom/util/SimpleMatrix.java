package telecom.util;


public class SimpleMatrix {
	private double[][] matrix;
	
	public SimpleMatrix() {
		initializeMatrix(0, 0);
	}
	public SimpleMatrix(int n, int m) {
		initializeMatrix(n, m);
	}

	public void initializeMatrix(int n, int m) {
		matrix = new double[n][];
		for (int i = 0; i < n; i++) {
			matrix[i] = new double[m];
		}
	}
	public Pair<Integer, Integer> getSize() {
		if (matrix.length == 0)
			return new Pair<Integer, Integer>(0, 0);
		return new Pair<Integer, Integer>(matrix.length, matrix[0].length);
	}
	public void setElement(int i, int j, double val) {
		matrix[i][j] = val;
	}
	public double getElement(int i, int j) {
		return matrix[i][j];
	}
}
