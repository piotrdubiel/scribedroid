package pl.scribedroid.input.classificator;

import java.util.ArrayList;

import pl.scribedroid.input.classificator.ClassificationResult.Label;

import android.content.Context;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.Prediction;

/**
 * Klasa ta opakowuje obiekt GestureLibrary, aby można było otrzymywać rezultaty w formie obiektu ClassifiactionResult, 
 * a nie obiektu ArrayList<Prediction>, jak robi to GestureLibrary.
 * @see android.gesture.GestureLibrary
 */
public class GestureLibraryClassificator implements Classificator {
	public static final String USER_SMALL_FILENAME = "user_small_lib";
	public static final String USER_CAPITAL_FILENAME = "user_capital_lib";
	public static final String USER_DIGIT_FILENAME = "user_digit_lib";

	private GestureLibrary library;

	public GestureLibraryClassificator(Context context, String filename) {
		library = GestureLibraries.fromPrivateFile(context, filename);
		library.load();
	}

	public GestureLibraryClassificator(Context context, int resid) {
		library = GestureLibraries.fromRawResource(context, resid);
		library.load();
	}

	/**
	 * Zwraca wynik klasyfikacji gestu w bibliotece GestureLibrary opakowany w obiekt ClassificationResult. Normalizuje wynik.
	 * @see pl.scribedroid.input.classificator.Classificator#classify(android.gesture.Gesture, int)
	 */
	@Override
	public ClassificationResult classify(Gesture gesture, int type) {
		ArrayList<Prediction> predictions = library.recognize(gesture);
		double library_denom = 0.0;
		for (Prediction p : predictions)
			library_denom += p.score;
		
		ArrayList<Label> labels = new ArrayList<Label>();
		for (Prediction p : predictions)
			labels.add(new Label(p.name.charAt(0),(float) (p.score / library_denom)));		
		
		return new ClassificationResult(labels,type);
	}

	/**
	 * Nieużywane, zawsze zwraca null
	 * @see pl.scribedroid.input.classificator.Classificator#classify(float[])
	 */
	@Override
	public ClassificationResult classify(float[] sample) {
		return null;
	}

	/**
	 * Określa, czy biblioteka gestów istnieje, jest załadowana i nie jest pusta. 
	 * Jeśli tak, zwraca true.
	 * W przeciwnym przypadku false
	 * @return
	 */
	public boolean isValid() {
		if (library != null && library.load() && library.getGestureEntries().size() > 0) {
			return true;
		}
		return false;
	}

	/**
	 * Nieużywane, zawsze zwraca null
	 * @see pl.scribedroid.input.classificator.Classificator#classifyRaw(float[])
	 */
	@Override
	public float[] classifyRaw(float[] sample) {
		return null;
	}
}
