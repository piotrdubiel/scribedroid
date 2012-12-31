package pl.scribedroid.input.ann;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface Network {
	public static final int[] DEFAULT_TOPOLOGY = {784, 300, 45};
	public static final String DEFAULT_FILENAME = "custom_network";
	public float[] answer(float[] in);
	
	public Vector answer(Vector in);
	
	public List<List<Vector>> train(float[] in,int label) throws Exception;
	
	public Network load(InputStream input);
	public void save(OutputStream output);
	
	public int[] getTopology();
}
