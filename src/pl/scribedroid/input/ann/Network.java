package pl.scribedroid.input.ann;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import pl.scribedroid.input.classificator.Classificator;

public interface Network extends Classificator {
	public List<List<Vector>> train(float[] in,int label) throws Exception;
	
	public Network load(InputStream input);
	public void save(OutputStream output);
	
	public int[] getTopology();
}
