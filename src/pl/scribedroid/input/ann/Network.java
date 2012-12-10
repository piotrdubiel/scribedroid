package pl.scribedroid.input.ann;

import java.io.InputStream;
import java.util.List;

public interface Network {
	public float[] answer(float[] in);
	
	public Vector answer(Vector in);
	
	public List<List<Vector>> train(float[] in,int label);
	
	public Network load(InputStream file_input);
	
	public Layer get(int location);
	
	public int size();
}
