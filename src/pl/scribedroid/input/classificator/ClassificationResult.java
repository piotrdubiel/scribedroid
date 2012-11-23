package pl.scribedroid.input.classificator;

import android.util.Pair;

public interface ClassificationResult {
	Pair<String,Float> getBestResult();
	Pair<String,Float> getResult(int index);
}
