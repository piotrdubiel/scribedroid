package pl.scribedroid.input.ann;

import java.util.ArrayList;

public class Layer {
	private ArrayList<Neuron> neurons;
	
	public Layer(int count,int inserts) {
		for (int i=0;i<count;++i) {
			neurons.add(new Neuron(inserts));
		}
	}
	
	public double[] answer(double[] x) {
		double[] result=new double[neurons.size()];
		for (int i=0;i<neurons.size();++i) {
			result[i]=neurons.get(i).answer(x);
		}
		
		return x;
	}
}
