package pl.scribedroid.input.classificator;

import java.util.ArrayList;

import pl.scribedroid.input.classificator.ClassificationResult.Label;

import android.content.Context;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.Prediction;

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

	@Override
	public ClassificationResult classify(float[] sample) {
		return null;
	}

	public boolean isValid() {
		if (library != null) {
			return true;
		}
		return false;
	}

	@Override
	public float[] classifyRaw(float[] sample) {
		return null;
	}
}
