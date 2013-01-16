package pl.scribedroid.input.knn;

import java.util.List;

import android.content.Context;
import pl.scribedroid.input.ann.Vector;

public class KNNClassifier {
	private List<Vector> data;
	private Context context;
	
	public KNNClassifier(Context c) {
		context = c;
	}
}
