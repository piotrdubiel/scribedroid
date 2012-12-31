package pl.scribedroid.input.ann;

public interface Layer {	
	public float[] answer(float[] x);
	
	public float[] dy(float[] x);
	
	public Layer set(float[][] w);
	
	public Vector get(int location);
	
	public int numberOfInputs();
	public int numberOfOutputs();
}
