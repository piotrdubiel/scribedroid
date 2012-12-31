package pl.scribedroid.input;

import java.util.ArrayList;

import android.gesture.Gesture;
import android.gesture.GestureStroke;

import com.xtremelabs.robolectric.internal.Implements;

@Implements(Gesture.class)
public class GestureShadow {
	private ArrayList<GestureStroke> strokes = new ArrayList<GestureStroke>();
	
	/**
     * @return all the strokes of the gesture
     */
    public ArrayList<GestureStroke> getStrokes() {
        return strokes;
    }

    /**
     * @return the number of strokes included by this gesture
     */
    public int getStrokesCount() {
        return strokes.size();
    }

    /**
     * Adds a stroke to the gesture.
     * 
     * @param stroke
     */
    public void addStroke(GestureStroke stroke) {
        strokes.add(stroke);       
    }
}
