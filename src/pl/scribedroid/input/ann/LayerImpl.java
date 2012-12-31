package pl.scribedroid.input.ann;

import java.util.ArrayList;

public class LayerImpl implements Layer {
	private ArrayList<NeuronImpl> neurons;
	
	public LayerImpl() {
		neurons=new ArrayList<NeuronImpl>();
	}
	
	public LayerImpl(int count,int inserts) {
		neurons=new ArrayList<NeuronImpl>();
		for (int i=0;i<count;++i) {
			neurons.add(new NeuronImpl(inserts));
		}
	}
	
	@Override
	public float[] answer(float[] x) {
		float[] result=new float[neurons.size()];
		for (int i=0;i<neurons.size();++i) {
			result[i]=neurons.get(i).answer(x);
		}	
		return result;
	}
	
	@Override
	public float[] dy(float[] x) {
		float[] result=new float[neurons.size()];
		for (int i=0;i<neurons.size();++i) {
			result[i]=neurons.get(i).dy(x);
		}	
		return result;
	}
	
	@Override
	public Layer set(float[][] w) {
		neurons.clear();
		for (float[] x : w) {
			neurons.add((NeuronImpl) new NeuronImpl().set(x));
		}
    	return this;
	}
	
	@Override
	public Vector get(int location) {
		return neurons.get(location).weights;
	}

	@Override
	public int numberOfInputs() {
		return neurons.get(0).weights.size();
	}

	@Override
	public int numberOfOutputs() {
		return neurons.size();
	}
	
	
}
