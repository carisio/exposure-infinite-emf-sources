package telecom.util;

import java.util.StringTokenizer;


public class Parser {
	
	public static String codeWhitespaces(String s) {
		String result = s;
		if (result.equals(""))
			result = " ";
		result = result.replaceAll("&", "&amps");
		result = result.replaceAll("<", "&lt");
		result = result.replaceAll(">", "&gt");
		result = result.replaceAll(" ", "<ws>");
		return result;
	}
	public static String uncodeWhitespaces(String s) {
		String result = s;
		result = result.replaceAll("<ws>", " ");
		result = result.replaceAll("&gt", ">");
		result = result.replaceAll("&lt", "<");
		result = result.replaceAll("&amps", "&");
		return result;
	}
	
	public static String codeDoubleArray(double[] array) {
		int size = array.length;
		String str = "" + size;
		
		if (size == 0)
			return str;
		else
			str += "#";
		
		for (int i = 0; i < size-1; i++)
			str += array[i] + "#";
		str += array[size-1];
		
		return str;

	}
	public static double[] uncodeDoubleArray(String str) {
		StringTokenizer tokenizer = new StringTokenizer(str, "#");
		
		int size = Integer.parseInt(tokenizer.nextToken());
		double[] array = new double[size];

		int i = 0;
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			double d = Double.parseDouble(token);
			array[i] = d;
			i++;
		}
		return array;

	}
}
