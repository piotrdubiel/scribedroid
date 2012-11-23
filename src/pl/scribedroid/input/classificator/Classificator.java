package pl.scribedroid.input.classificator;

import android.gesture.Gesture;

public interface Classificator {
	public static final int ALPHA				= 0;
	public static final int NUMBER				= 1;
	public static final int ALPHA_AND_NUMBER	= 2;
	
	public Character classify(Gesture gesture,int type);
}
