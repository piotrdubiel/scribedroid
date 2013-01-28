package pl.scribedroid.input.ann;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import pl.scribedroid.input.classificator.Classificator;

public interface Network extends Classificator {
	public List<List<Vector>> train(float[] in,int label) throws Exception;
	
	/**
	 * Ładuje sieć z podanego obiektu InputStream.
	 * @param input
	 * @return
	 */
	public Network load(InputStream input);
	/**
	 * Zapisuje sieć do podanego obiektu OutputStream.
	 * @param output
	 */
	public void save(OutputStream output);
	
	/**
	 * Zwraca tablicę z liczbą neuronów w kolejnych warstwach.
	 * @return tablica z architekturą sieci
	 */
	public int[] getTopology();
}
