package pl.scribedroid.input.classificator;

import java.io.IOException;

import pl.scribedroid.R;
import pl.scribedroid.input.Utils;
import pl.scribedroid.input.ann.Network;
import pl.scribedroid.input.ann.NetworkImpl;
import pl.scribedroid.input.classificator.ClassificationResult.Label;
import android.content.Context;
import android.gesture.Gesture;
import android.graphics.Bitmap;
import android.util.Log;

import com.google.inject.Inject;

public class MetaClassificator implements Classificator {
	private static final String TAG = "MetaClassificator";

	private Network pl_small_net;
	private Network pl_capital_net;
	private Network digit_net;
	private Network lod_net;
	private Network cos_net;

	private GestureLibraryClassificator alpha_library;
	private GestureLibraryClassificator number_library;
	private PCA pca;
	private Context context;

	@Inject
	public MetaClassificator(Context c) {
		context = c;
		try {
			pca = new PCA(c);

			pl_small_net = NetworkImpl.createFromInputStream(context.getAssets().open("pl_small_net"), Classificator.SMALL_ALPHA);
			pl_capital_net = NetworkImpl.createFromInputStream(context.getAssets().open("pl_capital_net"), Classificator.CAPITAL_ALPHA);
			digit_net = NetworkImpl.createFromInputStream(context.getAssets().open("digit_net"), Classificator.NUMBER);
			lod_net = NetworkImpl.createFromInputStream(context.getAssets().open("lod_net"), Classificator.GROUP | Classificator.CAPITAL_ALPHA | Classificator.SMALL_ALPHA | Classificator.NUMBER);
			cos_net = NetworkImpl.createFromInputStream(context.getAssets().open("cos_net"), Classificator.GROUP);

			alpha_library = new GestureLibraryClassificator(c, GestureLibraryClassificator.USER_ALPHA_FILENAME);
			if (!alpha_library.isValid()) {
				alpha_library = new GestureLibraryClassificator(c, R.raw.default_alpha_lib);
			}

			number_library = new GestureLibraryClassificator(c, GestureLibraryClassificator.USER_NUMBER_FILENAME);
			if (!number_library.isValid()) {
				number_library = new GestureLibraryClassificator(c, R.raw.default_number_lib);
			}

		}
		catch (IOException e) {
			e.printStackTrace();
		}

		Log.i(TAG, "Classificator loaded");
	}

	@Override
	public ClassificationResult classify(Gesture gesture, int type) {
		float[] sample = prepare(gesture);
		if (sample == null) return null;
		ClassificationResult result = null;
		Log.i(TAG, "First type: " + type + " " +  (Classificator.SMALL_ALPHA | Classificator.CAPITAL_ALPHA));
		if (type == (Classificator.SMALL_ALPHA | Classificator.CAPITAL_ALPHA | Classificator.NUMBER)) {
			ClassificationResult pl_small = pl_small_net.classify(sample);
			// for (Label l : pl_small.result)
			// Log.i(TAG, "Small: " + l.label + " " + l.belief);
			ClassificationResult pl_capital = pl_capital_net.classify(sample);
			// for (Label l : pl_capital.result)
			// Log.i(TAG, "Capital: " + l.label + " " + l.belief);
			ClassificationResult digit = digit_net.classify(sample);
			// for (Label l : digit.result)
			// Log.i(TAG, "Digit: " + l.label + " " + l.belief);
			ClassificationResult lod = lod_net.classify(sample);
			// for (Label l : lod.result)
			// Log.i(TAG, "LOD: " + l.label + " " + l.belief);
			ClassificationResult cos = cos_net.classify(sample);
			// for (Label l : cos.result)
			// Log.i(TAG, "COS: " + l.label + " " + l.belief);
			ClassificationResult alpha_lib = alpha_library.classify(gesture, Classificator.CAPITAL_ALPHA);
			// for (Label l : alpha_lib.result)
			// Log.i(TAG, "ALPHA lib: " + l.label + " " + l.belief);
			ClassificationResult number_lib = number_library.classify(gesture, Classificator.NUMBER);
			// for (Label l : number_lib.result)
			// Log.i(TAG, "NUMBER lib: " + l.label + " " + l.belief);

			result = lod.combine(cos);
			result = result.combine(pl_small);
			result = result.combine(pl_capital);
			result = result.combine(digit);
			result = result.combine(alpha_lib);
			result = result.combine(number_lib);

		}
		else if (type == (Classificator.SMALL_ALPHA | Classificator.CAPITAL_ALPHA)) {
			ClassificationResult pl_small = pl_small_net.classify(sample);
			ClassificationResult pl_capital = pl_capital_net.classify(sample);
			ClassificationResult cos = cos_net.classify(sample);
			ClassificationResult alpha_lib = alpha_library.classify(gesture, Classificator.CAPITAL_ALPHA);
			
			result = cos.combine(pl_small).combine(pl_capital).combine(alpha_lib);
		}
		else if (type == Classificator.SMALL_ALPHA ) {
			ClassificationResult pl_small = pl_small_net.classify(sample);
			ClassificationResult alpha_lib = alpha_library.classify(gesture, Classificator.CAPITAL_ALPHA);
			
			result = pl_small.combine(alpha_lib);
		}
		else if (type ==  Classificator.CAPITAL_ALPHA) {
			ClassificationResult pl_capital = pl_capital_net.classify(sample);
			ClassificationResult alpha_lib = alpha_library.classify(gesture, Classificator.CAPITAL_ALPHA);
			
			result = pl_capital.combine(alpha_lib);
		}
		else if (type == Classificator.NUMBER) {
			ClassificationResult digit = digit_net.classify(sample);
			ClassificationResult number_lib = number_library.classify(gesture, Classificator.NUMBER);
			
			result = digit.combine(number_lib);
		}

		for (Label l : result.result)
			Log.i(TAG, "RESULT: " + l.label + " " + l.belief);

		return result;
	}

	private float[] prepare(Gesture gesture) {
		Bitmap in = Utils.getBitmapFromGesture(gesture);
		if (in == null) return null;
		if (in.getWidth() > in.getHeight()) {
			in = Bitmap.createScaledBitmap(in, 20, Math.max(20 * in.getHeight() / in.getWidth(), 1), true);
		}
		else {
			in = Bitmap.createScaledBitmap(in, Math.max(20 * in.getWidth() / in.getHeight(), 1), 20, true);
		}

		float[] sample = Utils.getVectorFromBitmap(in);

		return pca.applyPCA(sample);
	}

	@Override
	public ClassificationResult classify(float[] sample) {
		// TODO Auto-generated method stub
		return null;
	}
}
