package pl.scribedroid.input.ann;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class NativeNetwork implements Network {
    static {
    	System.loadLibrary("ann");
    }
    
    private int network;
    
	@Override
	public float[] answer(float[] in) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector answer(Vector in) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<List<Vector>> train(float[] in, int label) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Network load(InputStream file_input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void save(OutputStream output) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int[] getTopology() {
		// TODO Auto-generated method stub
		return null;
	}
}
