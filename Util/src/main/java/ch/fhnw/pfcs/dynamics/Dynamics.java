package ch.fhnw.pfcs.dynamics;

public abstract class Dynamics {
	/**
	 * Calculation of integral curve. As described in 3.2
	 * 
	 * @param x current position
	 * @param dt the delta to use
	 * @return the new position
	 */
	public double[] euler(double[] x, final double dt) {
		double[] y = f(x);
		double[] res = new double[x.length];
		
		for(int i = 0; i < x.length; i++) {
			res[i] = x[i] + y[i] * dt;
		}
		
		return res;
	}
	
	public double[] rungeKutta(double[] x, final double dt) {
		final double[] y1 = f(x);
		final double[] y2 = f(add(x, mult(y1, dt/2)));
		final double[] y3 = f(add(x, mult(y2, dt/2)));
		final double[] y4 = f(add(x, mult(y3, dt)));
		final double[] y = mult(add(y1, mult(y2,2), mult(y3, 2), y4), (double)1/6);
		
		double[] res = new double[x.length];
		for(int i = 0; i < x.length; i++) {
			res[i] = x[i] + y[i] * dt;
		}
		
		return res;
	}
	
	private double[] mult(double[] a, double fact) {
		final double[] res = new double[a.length];
		for(int i = 0; i < a.length; i++) {
			res[i] = a[i] * fact;
		}
		return res;
	}
	
	private double[] add(double[]... a) {
		final double[] res = new double[a[0].length];
		for(int pos = 0; pos < a.length; pos++) {
			for(int i = 0; i < a[pos].length; i++) {
				res[i] += a[pos][i];
			}
		}
		return res;
	}
	
	public abstract double[] f(double[] x);
}
