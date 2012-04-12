package pl.scribedroid.input.ann;

import java.util.ArrayList;

public class Network {
	private ArrayList<Layer> layers;
	
	public Network(int[] arch) {
		for (int i=1;i<arch.length;++i) {
	        layers.add(new Layer(arch[i],arch[i-1]));
		}
	}
	
	public double[] classify(double[] x) {
		double[] result=x.clone();
	    for (Layer l : layers) {
	    	result=l.answer(result);
	    }

	    return result;
	}
}
