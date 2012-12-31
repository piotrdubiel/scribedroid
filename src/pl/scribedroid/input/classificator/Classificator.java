package pl.scribedroid.input.classificator;

import java.util.List;

import android.gesture.Gesture;
import android.util.Pair;

public interface Classificator {
	public static final int ALPHA				= 0;
	public static final int NUMBER				= 1;
	public static final int ALPHA_AND_NUMBER	= 2;
	public static final int ALPHA_PL			= 3;
	
	public Pair<Character, Float> classify(Gesture gesture,int type);
	public ClassificationResult classifiy(Gesture gesture,int type);
	public ClassificationResult classifiy(List<ClassificationResult> inputs);
}
