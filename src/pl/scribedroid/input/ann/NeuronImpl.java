package pl.scribedroid.input.ann;

import java.util.Random;

public class NeuronImpl implements Neuron {
	public Vector weights;

	public NeuronImpl() {
		weights = new Vector();
	}

	public NeuronImpl(int inserts) {
		float[] w = new float[inserts + 1];
		Random rand = new Random();
		for (int i = 0; i <= inserts; ++i) {
			w[i] = rand.nextFloat();
		}
		weights = new Vector(w);
	}

	@Override
	public float answer(float[] x) {
		double result = 0.0;
		for (int i = 0; i < x.length; i++) {
			result += weights.get(i) * x[i];
		}
		result += weights.get(weights.size() - 1);

		return (float) (1 / (1 + Math.exp(-result)));
	}

	@Override
	public float dy(float[] x) {
		double result = 0.0;
		for (int i = 0; i < x.length; i++) {
			result += weights.get(i) * x[i];
		}
		result += weights.get(weights.size() - 1);

		return (float) (Math.exp(-result) / Math.pow(1 + Math.exp(-result), 2));
	}

	@Override
	public Neuron set(float[] w) {
		weights.set(w);
		return this;
	}
}
