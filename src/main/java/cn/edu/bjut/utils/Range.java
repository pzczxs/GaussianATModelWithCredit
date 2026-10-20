package cn.edu.bjut.utils;

public class Range {
	public static double[] openClosed(double lower, double upper, int number) {
		double[] range = new double[number];
		
		double increment = (upper - lower) / number; 
		for (int i = 0; i < number - 1; i++) {
			range[i] = lower + increment * (i + 1); 
		}
		range[number - 1] = upper; 
		
		return range; 
	}
	
	public static double[] closedClosed(double lower, double upper, int number) {
		double[] range = new double[number];
		
		double increment = (upper - lower) / number; 
		for (int i = 0; i < number - 1; i++) {
			range[i] = lower + increment * i; 
		}
		range[number - 1] = upper; 
		
		return range; 
	}
}
