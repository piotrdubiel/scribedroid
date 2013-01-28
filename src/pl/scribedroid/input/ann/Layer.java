package pl.scribedroid.input.ann;

import java.util.ArrayList;

public class Layer {
	private ArrayList<Neuron> neurons;
	
	/**
	 * Tworzy warstwę  o zerowej liczbie neuronów
	 */
	public Layer() {
		neurons=new ArrayList<Neuron>();
	}
	
	/**
	 * Tworzy warstwę o liczbie neuronów równej count 
	 * i liczbie wejść równej inserts oraz losuje wagi dla każdego z neuronów
	 * @param count liczba neuronów
	 * @param inserts liczba wejść
	 */
	public Layer(int count,int inserts) {
		neurons=new ArrayList<Neuron>();
		for (int i=0;i<count;++i) {
			neurons.add(new Neuron(inserts));
		}
	}
	
	/**
	 * Zwraca wektor wartości funkcji aktywacji dla zadanego wektora cech
	 * @param x
	 * @return
	 */
	public float[] answer(float[] x) {
		float[] result=new float[neurons.size()];
		for (int i=0;i<neurons.size();++i) {
			result[i]=neurons.get(i).answer(x);
		}	
		return result;
	}
	
	/**
	 * Zwraca wektor pochodnych funkcji aktywacji dla zadanego wektora cech
	 * @param x
	 * @return
	 */
	public float[] dy(float[] x) {
		float[] result=new float[neurons.size()];
		for (int i=0;i<neurons.size();++i) {
			result[i]=neurons.get(i).dy(x);
		}	
		return result;
	}
	
	/**
	 * Ustawia w warstwie wagi według tablicy w argumencie
	 * @param w
	 * @return
	 */
	public Layer set(float[][] w) {
		neurons.clear();
		for (float[] x : w) {
			neurons.add((Neuron) new Neuron().set(x));
		}
    	return this;
	}
	
	/**
	 * Zwraca wagi neuronu o indeksie równym location
	 * @param location
	 * @return
	 */
	public Vector get(int location) {
		return neurons.get(location).weights;
	}

	/**
	 * Zwraca liczbę wejść warstwy
	 * @return
	 */
	public int numberOfInputs() {
		return neurons.get(0).weights.size();
	}

	/**
	 * Zwraca liczbę wyjść warstwy
	 * @return
	 */
	public int numberOfOutputs() {
		return neurons.size();
	}
	
	
}
