package pl.scribedroid.input.classificator;

import android.gesture.Gesture;

public interface Classificator {
	/**
	 * Typ klasyfikatora rozpoznający małe litery
	 */
	public static final int SMALL_ALPHA			= 1 << 0;
	/**
	 * Typ klasyfikatora rozpoznający wielkie litery
	 */
	public static final int CAPITAL_ALPHA		= 1 << 1;
	/**
	 * Typ klasyfikatora rozpoznający cyfry
	 */
	public static final int DIGIT				= 1 << 2;
	/**
	 * Klasyfikator grupowy. Nieużywany
	 */
	public static final int GROUP				= 1 << 3;
	
	/**
	 * Klasyfikacja gestu o danym typie i
	 * opakowanie wyniku w obiekt ClassificationResult
	 * @param gesture gest do klasyfikacji
	 * @param type typ klasyfikowanego znaku
	 * @return rezultat klasyfikacji w obiekcie ClassificationResult
	 */
	public ClassificationResult classify(Gesture gesture, int type);
	/**
	 * Klasyfikacja wektora cech zapisanego jako tablica wartości float 
	 * i opakowanie wyniku w obiekt ClassificationResult
	 * @param sample tablica z wartościami cech
	 * @return rezultat klasyfikacji w obiekcie ClassificationResult
	 */
	public ClassificationResult classify(float[] sample);
	
	/**
	 * Klasyfikacja wektora cech jako tablica wartości float 
	 * i  przekazanie wyniku jako wektor również w tablicy wartości  float.
	 * @param sample tablica z wartościami cech
	 * @return  rezultat klasyfikacji
	 */
	public float[] classifyRaw(float[] sample);
	
}
