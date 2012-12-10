package pl.scribedroid.input.classificator;

import java.util.List;
import android.gesture.Gesture;

public interface Classificator {
	public static final int ALPHA				= 0;
	public static final int NUMBER				= 1;
	public static final int ALPHA_AND_NUMBER	= 2;
	public static final int ALPHA_PL			= 3;
	
	public List<Character> classify(Gesture gesture,int type);
	public ClassificationResult classifiy(Gesture gesture,int type);
	public ClassificationResult classifiy(List<ClassificationResult> inputs);
}
