package pl.scribedroid.input.ann;

import java.util.ArrayList;
import java.util.Random;

public class Neuron {
	private ArrayList<Double> weights;

    public Neuron(int inserts) {
    	Random rand=new Random();
    	for (int i=0;i<=inserts;++i) {
    		weights.add(rand.nextDouble());
    	}
    }
    public double answer(double[] x) {
    	double result=0.0;
    	for (int i=0; i<x.length; i++) {
    	    result+=weights.get(i)*x[i];
    	}    	
    	result+=weights.get(weights.size());
    	
   	 	return (2/(1+Math.exp(-result)))-1;	
    }
}
