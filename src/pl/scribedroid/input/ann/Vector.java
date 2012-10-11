package pl.scribedroid.input.ann;

import android.util.Log;

public class Vector {
	//private static float EPS = Float.;
	private float[] values;
	
	public Vector(float[] v) {
		values=v;
	}
	
	public Vector() {}
	
	public Vector(Vector b) {
		set(b.values);
	}
	
	public Vector(float b, int count) {
		values=new float[count];
		for (int i=0;i<count;++i)
			values[i]=b;
	}
	
	public float get(int location) {
		return values[location];
	}
	
	public Vector set(float[] v) {
		values=v;
		return this;
	}
	
	public Vector set(float v,int location) {
		values[location]=v;
		return this;
	}
	
	public Vector add(Vector b) {
		for (int i=0;i<size();++i) {
			values[i]+=b.get(i);
		}
		
		return this;
	}
	
	public static Vector add(Vector a,Vector b) {
		Vector result=new Vector(a);
		result.add(b);
		return result;
	}
	
	public Vector substract(Vector b) {
		for (int i=0;i<size();++i) {
			values[i]-=b.get(i);
		}
		
		return this;
	}
	
	public static Vector substract(Vector a,Vector b) {
		Vector result=new Vector(a);
		result.substract(b);
		return result;
	}
	
	public Vector multiply(float b) {		
		for (int i=0;i<size();++i) {
			values[i]*=b;
		}
		
		return this;
	}
	
	public Vector multiply(Vector b) {		
		for (int i=0;i<size();++i) {
			values[i]=values[i]*b.get(i);
		}		
		return this;
	}
	
	public static Vector multiply(Vector a,Vector b) {
		Vector result=new Vector(a);
		result.multiply(b);
		return result;
	}
	
	public boolean equals(Vector b) {
		//Log.d("VECTOR", "START");
		if (size()!=b.size()) return false;
		for (int i=0;i<size();++i) {
			Log.d("VECTOR", String.valueOf(values[i])+" "+String.valueOf(b.get(i)));
			//if (Math.abs(values[i]-b.get(i))>EPS) return false;
			if (Float.compare(values[i],b.get(i))!=0) return false;
		}
		return true;
	}
	
	public int size() {
		return values.length;
	}
	
	public float[] toArray() {
		return values;
	}
	
	public float sum() {
		float sum=0.0f;
		for (int i=0;i<size();++i) {
			sum+=values[i];
		}
		return sum;
	}
	
	public Vector append(float v) {
		float[] buffer=values;
		values=new float[values.length+1];
		for (int i=0;i<buffer.length;++i)
			values[i]=buffer[i];
		values[values.length-1]=v;
		return this;
	}
}
