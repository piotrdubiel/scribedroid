package pl.scribedroid.input.ann;

public interface Neuron {
    public float answer(float[] x);    
    public float dy(float[] x);    
    public Neuron set(float[] w);
}
