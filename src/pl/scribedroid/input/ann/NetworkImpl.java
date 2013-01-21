package pl.scribedroid.input.ann;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import pl.scribedroid.input.Utils;
import pl.scribedroid.input.classificator.ClassificationResult;
import pl.scribedroid.input.classificator.ClassificationResult.Label;
import pl.scribedroid.input.classificator.Classificator;

import android.content.Context;
import android.gesture.Gesture;
import android.util.Log;

public class NetworkImpl implements Network {
	private ArrayList<LayerImpl> layers;
	int type;

	public NetworkImpl() {
	}

	public NetworkImpl(int[] topology, int type) {
		this.type = type;
		layers = new ArrayList<LayerImpl>();
		for (int i = 1; i < topology.length; ++i) {
			layers.add(new LayerImpl(topology[i], topology[i - 1]));
		}
	}

	@Override
	public ClassificationResult classify(Gesture gesture, int type) {
		return null;
	}

	@Override
	public ClassificationResult classify(float[] sample) {
		float[] y = classifyRaw(sample);
		if ((type & Classificator.GROUP) == 0) {
			return new ClassificationResult(y, type);
		}
		else {
			ArrayList<Label> result = new ArrayList<Label>();
			if ((type & Classificator.DIGIT) == 0 
					&& (type & Classificator.SMALL_ALPHA) > 0
					&& (type & Classificator.CAPITAL_ALPHA) > 0) {
				// COS net
				for (char c : Utils.LETTERS)
					result.add(new Label(c, y[1]));
				for (char c : Utils.LETTERS)
					result.add(new Label(Character.toUpperCase(c), y[0]));

			}
			if ((type & Classificator.DIGIT) > 0 
					&& (type & Classificator.SMALL_ALPHA) > 0
					&& (type & Classificator.CAPITAL_ALPHA) > 0) {
				// LOD net
				for (char c : Utils.LETTERS)
					result.add(new Label(c, y[1]));
				for (char c : Utils.LETTERS)
					result.add(new Label(Character.toUpperCase(c), y[1]));
				
				for (char c = '0'; c < '9'; ++c)
					result.add(new Label(c, y[0]));

			}
			return new ClassificationResult(result, type);
		}
	}

	
	@Override
	public float[] classifyRaw(float[] sample) {
		float[] result = sample;
		for (LayerImpl l : layers) {
			result = l.answer(result);
		}
		return result;
	}
	
	@Override
	public List<List<Vector>> train(float[] in, int label) throws Exception {
		List<Vector> y = new ArrayList<Vector>();
		List<Vector> dy = new ArrayList<Vector>();

		if (in.length + 1 != layers.get(0).numberOfInputs()) throw new Exception("Expected "
				+ (layers.get(0).numberOfInputs() - 1) + " inputs instead of " + in.length);

		y.add(new Vector(in));
		for (LayerImpl l : layers) {
			dy.add(new Vector(l.dy(y.get(y.size() - 1).toArray())));
			y.add(new Vector(l.answer(y.get(y.size() - 1).toArray())));
		}

		List<Vector> d = new ArrayList<Vector>();
		int lastLayer = dy.get(dy.size() - 1).size();
		float[] l = new float[lastLayer];

		for (int i = 0; i < lastLayer; ++i) {
			if (i == label) l[i] = 1;
			else l[i] = 0;
		}

		d.add(new Vector(l).substract(y.get(y.size() - 1)).multiply(dy.get(dy.size() - 1)));

		for (int i = layers.size() - 2; i >= 0; --i) {
			Vector D = new Vector(0, layers.get(i + 1).get(0).size() - 1);
			for (int q = 0; q < layers.get(i + 1).get(0).size() - 1; ++q) {
				float sum = 0;
				for (int p = 0; p < layers.get(i + 1).numberOfOutputs(); ++p) {
					sum += layers.get(i + 1).get(p).get(q) * d.get(layers.size() - i - 2).get(p);
				}
				D.set(sum * dy.get(i).get(q), q);
			}
			d.add(D);
		}

		for (int i = 0; i < layers.size(); ++i) {
			for (int j = 0; j < layers.get(i).numberOfOutputs(); ++j) {
				Vector Y = new Vector(y.get(i)).append(1.0f);
				Y.multiply(d.get(layers.size() - i - 1).get(j));
				layers.get(i).get(j).add(Y);
			}
		}

		List<List<Vector>> result = new ArrayList<List<Vector>>();
		result.add(y);
		result.add(dy);
		result.add(d);

		return result;
	}

	@Override
	public Network load(InputStream input) {
		try {
			DataInputStream data_in = new DataInputStream(input);

			int layer = data_in.readInt();
			int[] arch = new int[layer];

			for (int i = 0; i < layer; ++i) {
				arch[i] = data_in.readInt();
			}

			layers = new ArrayList<LayerImpl>();

			for (int l = 0; l < layer - 1; ++l) {
				layers.add(new LayerImpl());
			}

			for (int i = 0; i < arch.length - 2; ++i) {
				float[][] w = new float[arch[i + 1] - 1][arch[i]];
				for (int j = 0; j < arch[i]; ++j) {
					for (int k = 0; k < arch[i + 1] - 1; ++k) {
						w[k][j] = data_in.readFloat();
					}
				}
				layers.get(i).set(w);
			}

			float[][] w = new float[arch[arch.length - 1]][arch[arch.length - 2]];
			for (int j = 0; j < arch[arch.length - 2]; ++j) {
				for (int k = 0; k < arch[arch.length - 1]; ++k) {
					w[k][j] = data_in.readFloat();
				}
			}
			layers.get(arch.length - 2).set(w);

			data_in.close();
		}
		catch (IOException e) {
			System.out.println("IO Exception LOADING =: " + e);
		}

		return this;
	}

	@Override
	public void save(OutputStream output) {
		try {
			DataOutputStream data_out = new DataOutputStream(output);

			int[] topology = getTopology();
			data_out.writeInt(topology.length);
			for (int t : topology)
				data_out.writeInt(t);
			Log.v("XXX", "NET: " + topology[0] + " " + topology[1]);

			for (int i = 0; i < topology.length - 2; ++i)
				for (int j = 0; j < topology[i]; ++j)
					for (int k = 0; k < topology[i + 1] - 1; ++k)
						data_out.writeFloat(layers.get(i).get(k).get(j));

			for (int j = 0; j < topology[topology.length - 2]; ++j)
				for (int k = 0; k < topology[topology.length - 1]; ++k)
					data_out.writeFloat(layers.get(topology.length - 2).get(k).get(j));

			data_out.close();
		}
		catch (IOException e) {
			System.out.println("IO Exception SAVING =: " + e);
		}
	}

	public static NetworkImpl createFromRawResource(Context context, int rid, int type) {
		NetworkImpl instance = (NetworkImpl) new NetworkImpl().load(context.getResources().openRawResource(rid));
		instance.type = type;
		return instance;
	}

	public static NetworkImpl createFromInputStream(InputStream in, int type) {
		NetworkImpl instance = (NetworkImpl) new NetworkImpl().load(in);
		instance.type = type;
		return instance;
	}

	public Layer get(int location) {
		return layers.get(location);
	}

	public int size() {
		return layers.size();
	}

	@Override
	public int[] getTopology() {
		if (layers.isEmpty()) return new int[0];
		int[] topology = new int[layers.size() + 1];
		for (int i = 0; i < layers.size(); ++i) {
			topology[i] = layers.get(i).numberOfInputs();
		}
		topology[layers.size()] = layers.get(layers.size() - 1).numberOfOutputs();

		return topology;
	}
}
