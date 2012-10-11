package pl.scribedroid.input.ann;

import java.util.ArrayList;

public class Layer {
	private ArrayList<Neuron> neurons;
	
	public Layer() {
		neurons=new ArrayList<Neuron>();
	}
	
	public Layer(int count,int inserts) {
		neurons=new ArrayList<Neuron>();
		for (int i=0;i<count;++i) {
			neurons.add(new Neuron(inserts));
		}
	}
	
	public float[] answer(float[] x) {
		float[] result=new float[neurons.size()];
		for (int i=0;i<neurons.size();++i) {
			result[i]=neurons.get(i).answer(x);
		}	
		return result;
	}
	
	public float[] dy(float[] x) {
		float[] result=new float[neurons.size()];
		for (int i=0;i<neurons.size();++i) {
			result[i]=neurons.get(i).dy(x);
		}	
		return result;
	}
	
	public Layer set(float[][] w) {
		neurons.clear();
		for (float[] x : w) {
			neurons.add(new Neuron().set(x));
		}
    	return this;
	}
	
	public Vector get(int location) {
		return neurons.get(location).weights;
	}
	
	public int size() {
		return neurons.size();
	}
}
