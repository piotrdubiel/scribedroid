package pl.scribedroid.input.ann;

import java.util.Random;

public class Neuron {
	public Vector weights;

	public Neuron() {
		weights=new Vector();
	}
	
    public Neuron(int inserts) {
    	float[] w=new float[inserts+1];
    	Random rand=new Random();
    	for (int i=0;i<=inserts;++i) {
    		w[i]=rand.nextFloat();
    	}
    	weights=new Vector(w);
    }
    public float answer(float[] x) {
    	double result=0.0;
    	for (int i=0; i<x.length; i++) {
    	    result+=weights.get(i)*x[i];
    	}    	
    	result+=weights.get(weights.size()-1);
    	
   	 	return (float) (1/(1+Math.exp(-result)));
    }
    
    public float dy(float[] x) {
    	double result=0.0;
    	for (int i=0; i<x.length; i++) {
    	    result+=weights.get(i)*x[i];
    	}    	
    	result+=weights.get(weights.size()-1);
    	
   	 	return (float) (Math.exp(-result)/Math.pow(1+Math.exp(-result), 2));
    }
    
    public Neuron set(float[] w) {
    	weights.set(w);
    	return this;
    }
}
