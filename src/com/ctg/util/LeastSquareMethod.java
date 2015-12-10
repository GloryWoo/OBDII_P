package com.ctg.util;

//��С���˷��㷨
public class LeastSquareMethod {
	private double[] x;
	private double[] y;
	//private double[] weight;
	private int m;
	private double[] coefficient;
	public LeastSquareMethod(double[] x, double[] y, int m) {
		if (x == null || y == null || x.length < 2 || x.length != y.length
				|| m < 2)
			throw new IllegalArgumentException("invalid parameter!");
		this.x = x;
		this.y = y;
		this.m = m;
		//weight = new double[x.length];
//		for (int i = 0; i < x.length; i++) {
//			weight[i] = 1;
//		}
	}
//	public LeastSquareMethod(double[] x, double[] y, double[] weight, int m) {
//		if (x == null || y == null || weight == null || x.length < 2
//				|| x.length != y.length || x.length != weight.length || m < 2)
//			throw new IllegalArgumentException("invalid parameter!");
//		this.x = x;
//		this.y = y;
//		this.m = m;
//		this.weight = weight;
//	}
	public double[] getCoefficient() {
		if (coefficient == null)
			compute();
		return coefficient;
	}
	public double fit(double v) {
		if (coefficient == null)
			compute();
		if (coefficient == null)
			return 0;
		double sum = 0;
		for (int i = 0; i < coefficient.length; i++) {
			sum += Math.pow(v, i) * coefficient[i];
		}
		return sum;
	}
	private void compute() {
		if (x == null || y == null || x.length <= 1 || x.length != y.length
				|| x.length < m || m < 2)
			return;
		double[] s = new double[(m - 1) * 2 + 1];
		for (int i = 0; i < s.length; i++) {
			for (int j = 0; j < x.length; j++)
				s[i] += Math.pow(x[j], i) ;//* weight[j]
		}
		double[] f = new double[m];
		for (int i = 0; i < f.length; i++) {
			for (int j = 0; j < x.length; j++)
				f[i] += Math.pow(x[j], i) * y[j] ;//* weight[j]
		}
		double[][] a = new double[m][m];
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < m; j++) {
				a[i][j] = s[i + j];
			}
		}
		coefficient = Algorithm.multiLinearEquationGroup(a, f);
	}
}

class Algorithm {
	/**
	 * ��Ԫһ�η������
	 * @param a ϵ������
	 * @param b �������
	 * @return ���
	 */
	public static double[] multiLinearEquationGroup(double[][] a,double[] b){
		if (a==null||b==null||a.length==0||a.length!=b.length)
			return null;
		for (double[] x:a){
			if (x==null||x.length!=a.length)
				return null;
		}
		
		int len=a.length-1;
		double[] result=new double[a.length];
		
		if (len==0){
			result[0]=b[0]/a[0][0];
			return result;
		}
		
		double[][] aa=new double[len][len];
		double[] bb=new double[len];
		int posx=-1,posy=-1;
		for (int i=0;i<=len;i++){
			for (int j=0;j<=len;j++)
				if (a[i][j]!=0.0d){
					posy=j;
					break;
				}
			if (posy!=-1){
				posx=i;
				break;
			}
		}
		if (posx==-1)
			return null;
		
		int count=0;
		for (int i=0;i<=len;i++){
			if (i==posx)
				continue;
			bb[count]=b[i]*a[posx][posy]-b[posx]*a[i][posy];
			int count2=0;
			for (int j=0;j<=len;j++){
				if (j==posy)
					continue;
				aa[count][count2]=a[i][j]*a[posx][posy]-a[posx][j]*a[i][posy];
				count2++;
			}
			count++;
		}
		
		double[] result2=multiLinearEquationGroup(aa,bb);
		
		double sum=b[posx];
		count=0;
		for (int i=0;i<=len;i++){
			if (i==posy)
				continue;
			sum-=a[posx][i]*result2[count];
			result[i]=result2[count];
			count++;
		}
		result[posy]=sum/a[posx][posy];
		
		return result;
	}
}
