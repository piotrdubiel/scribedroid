//package pl.scribedroid.input.classificator;
//
//import java.io.DataInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//import pl.scribedroid.R;
//import pl.scribedroid.input.Utils;
//import pl.scribedroid.input.ann.Network;
//import pl.scribedroid.input.ann.NetworkImpl;
//import android.content.Context;
//import android.gesture.Gesture;
//import android.gesture.GestureLibraries;
//import android.gesture.GestureLibrary;
//import android.gesture.Prediction;
//import android.graphics.Bitmap;
//import android.util.Log;
//import android.util.Pair;
//import android.view.KeyCharacterMap;
//
//import com.google.inject.Inject;
//
//public class ClassificatorImpl implements Classificator {
//	private static final boolean DEBUG = true;
//
//	private Network pl_small_net;
//	private Network pl_capital_net;
//	private Network digit_net;
//	private Network lod_net;
//
//	private GestureLibrary alphaLibrary;
//	private GestureLibrary numberLibrary;
//
//	private Context context;
//
//	private static final String TAG = "Classificator";
//
//	@Inject
//	public ClassificatorImpl(Context c) {
//		context = c;
//		try {
//			pl_small_net = NetworkImpl.createFromInputStream(context.getAssets().open("pl_small_net"));
//			pl_capital_net = NetworkImpl.createFromInputStream(context.getAssets().open("pl_capital_net"));
//			digit_net = NetworkImpl.createFromInputStream(context.getAssets().open("digit_net"));
//			lod_net = NetworkImpl.createFromInputStream(context.getAssets().open("lod_net"));
//			loadPCA();
//		}
//		catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		loadLibrary();
//
//		if (DEBUG) {
//			Log.d(TAG, String.valueOf(alphaLibrary.getGestureEntries().size())
//					+ " gestures in alpha library");
//			Log.d(TAG, String.valueOf(numberLibrary.getGestureEntries().size())
//					+ " gestures in number library");
//		}
//		Log.i(TAG, "Classificator loaded");
//	}
//
//	@Override
//	public Pair<Character, Float> classify(Gesture gesture, int type) {
//		if (DEBUG) {
//			Log.d(TAG, "Classification start - type " + String.valueOf(type));
//		}
//
//		long startTime = System.currentTimeMillis();
//
//		Network currentNet = null;
//		GestureLibrary currentLibrary = null;
//
//		Bitmap in = Utils.getBitmapFromGesture(gesture);
//
//		if (in == null) return null;
//
//		if (in.getWidth() > in.getHeight()) {
//			in = Bitmap.createScaledBitmap(in, 20, Math.max(20 * in.getHeight() / in.getWidth(), 1), true);
//		}
//		else {
//			in = Bitmap.createScaledBitmap(in, Math.max(20 * in.getWidth() / in.getHeight(), 1), 20, true);
//		}
//
//		float[] sample = Utils.getVectorFromBitmap(in);
//		float[] log = sample.clone();
//
//		// PCA
//		sample = Utils.applyPCA(sample, mu, trmx);
//
//		// if (type == Classificator.ALPHA_AND_NUMBER) {
//		// if (lonY[0] > lonY[1]) type = Classificator.ALPHA;
//		// else type = Classificator.NUMBER;
//		// }
//
//		switch (type) {
//		case ALPHA:
//			currentNet = pl_capital_net;
//			currentLibrary = alphaLibrary;
//			break;
//		case NUMBER:
//			currentNet = digit_net;
//			currentLibrary = numberLibrary;
//			break;
//		default:
//			currentNet = pl_capital_net;
//			currentLibrary = alphaLibrary;
//		}
//
//		currentNet = pl_small_net;
//		Log.i(TAG, String.valueOf(KeyCharacterMap.getDeadChar(',', 'a')));
//
//		ArrayList<Prediction> libraryAnswer = currentLibrary.recognize(gesture);
//
//		if (DEBUG) {
//			Log.d(TAG, String.valueOf(libraryAnswer.size()) + " predictions");
//		}
//
//		float[] netAnswer = currentNet.answer(sample);
//
//		float[] pl_small_answer = pl_small_net.answer(sample);
//		float[] pl_capital_answer = pl_capital_net.answer(sample);
//		float[] digit_answer = digit_net.answer(sample);
//		float[] lod_answer = lod_net.answer(sample);
//
//		Log.i(TAG, "Small: " + Arrays.toString(pl_small_answer));
//		Log.i(TAG, "Capital: " + Arrays.toString(pl_capital_answer));
//		Log.i(TAG, "Digit: " + Arrays.toString(digit_answer));
//		Log.i(TAG, "LOD: " + Arrays.toString(lod_answer));
//
//		Log.v(TAG, "Classification time: "
//				+ String.valueOf((System.currentTimeMillis() - startTime) / 1000.0) + "s");
//
//		return metaVote(netAnswer, libraryAnswer, type);
//	}
//
//	private Pair<Character, Float> metaVote(float[] netResult, ArrayList<Prediction> libraryResult,
//			int type) {
//		List<Pair<Character, Float>> netStrings = Utils.getBest(netResult, Utils.RESULT_COUNT, type);
//
//		boolean libraryValid = libraryResult != null && !libraryResult.isEmpty()
//				&& libraryResult.get(0).score > 2.0;
//		boolean netValid = netStrings != null && !netStrings.isEmpty()
//				&& netStrings.get(0).second > 0.1;
//
//		double library_denom = 0.0;
//		for (Prediction p : libraryResult) {
//			library_denom += p.score;
//		}
//
//		if (DEBUG) {
//			Log.v(TAG, "LIBRARY Valid " + String.valueOf(libraryValid));
//			if (libraryValid) {
//				for (int i = 0; i < Math.min(Utils.RESULT_COUNT, libraryResult.size()); ++i) {
//					Log.v(TAG, libraryResult.get(i).name + " "
//							+ String.valueOf(libraryResult.get(i).score / library_denom));
//				}
//			}
//
//			Log.v(TAG, "NET Valid " + String.valueOf(netValid));
//			if (netValid) for (Pair<Character, Float> n : netStrings) {
//				Log.v(TAG, n.second + " " + String.valueOf(n.first));
//			}
//		}
//
//		Pair<Character, Float> result = null;
//
//		if (libraryValid && netValid) {
//			List<Pair<Double, String>> pairs = new ArrayList<Pair<Double, String>>();
//			for (int i = 0; i < Math.min(netStrings.size(), libraryResult.size()); ++i) {
//				for (int j = 0; j < Math.min(netStrings.size(), libraryResult.size()); ++j) {
//					if (netStrings.get(i).first == libraryResult.get(j).name.charAt(0)
//							&& netStrings.get(i).second > 0.2 && libraryResult.get(j).score > 0.2) {
//						if (DEBUG) Log.v(TAG, "AGREED");
//						pairs.add(new Pair<Double, String>(netStrings.get(i).second
//								* libraryResult.get(j).score / library_denom, libraryResult.get(j).name));
//					}
//				}
//			}
//			if (!pairs.isEmpty()) {
//				// Find best pair
//				double score = Double.MAX_VALUE;
//				String bestMatch = "";
//				for (Pair<Double, String> p : pairs) {
//					if (p.first < score) {
//						score = p.first;
//						bestMatch = p.second;
//					}
//				}
//				if (DEBUG) Log.v(TAG, "Pair - " + bestMatch);
//				result = new Pair<Character, Float>(getCharacter(bestMatch), (float) score);
//			}
//
//			if (netStrings.get(0).second < 0.75) {
//				if (DEBUG) Log.v(TAG, "Library's better");
//				result = new Pair<Character, Float>(getCharacter(libraryResult.get(0).name), (float) (libraryResult.get(0).score / library_denom));
//			}
//			else {
//				if (DEBUG) Log.v(TAG, "Net's better");
//				result = new Pair<Character, Float>(netStrings.get(0).first, netStrings.get(0).second);
//			}
//		}
//		else if (libraryValid) {
//			if (DEBUG) Log.v(TAG, "Only library valid");
//			result = new Pair<Character, Float>(getCharacter(libraryResult.get(0).name), (float) (libraryResult.get(0).score / library_denom));
//		}
//		else if (netValid) {
//			if (DEBUG) Log.v(TAG, "Only net valid");
//			result = new Pair<Character, Float>(netStrings.get(0).first, netStrings.get(0).second);
//		}
//
//		return result;
//	}
//
//	private Character getCharacter(String name) {
//		return name.charAt(0);
//	}
//
//	private void loadLibrary() {
//		GestureLibrary userAlpha = GestureLibraries.fromPrivateFile(context, Utils.USER_ALPHA_FILENAME);
//		if (!validLibrary(userAlpha)) {
//
//			if (DEBUG) Log.d(TAG, "USER lib for alphas not valid - using default");
//		}
//		else {
//			alphaLibrary = userAlpha;
//			if (DEBUG) Log.d(TAG, "USER lib for alphas - OK");
//		}
//
//		GestureLibrary userNumber = GestureLibraries.fromPrivateFile(context, Utils.USER_NUMBER_FILENAME);
//		if (!validLibrary(userNumber)) {
//			numberLibrary = GestureLibraries.fromRawResource(context, R.raw.default_number_lib);
//			if (DEBUG) Log.d(TAG, "USER lib for numbers not valid - using default");
//		}
//		else {
//			numberLibrary = userNumber;
//			if (DEBUG) Log.d(TAG, "USER lib for numbers - OK");
//		}
//
//		alphaLibrary.load();
//		numberLibrary.load();
//	}
//
//	@Override
//	public ClassificationResult classifiy(Gesture gesture, int type) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//}
