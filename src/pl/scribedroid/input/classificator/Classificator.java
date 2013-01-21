package pl.scribedroid.input.classificator;

import android.gesture.Gesture;

public interface Classificator {
	public static final int SMALL_ALPHA			= 1 << 0;
	public static final int CAPITAL_ALPHA		= 1 << 1;
	public static final int DIGIT				= 1 << 2;
	public static final int GROUP				= 1 << 3;
	
	public ClassificationResult classify(Gesture gesture, int type);
	public ClassificationResult classify(float[] sample);
	public float[] classifyRaw(float[] sample);
	
}
