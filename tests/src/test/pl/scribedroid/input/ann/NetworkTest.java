package pl.scribedroid.input.ann;

import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import pl.scribedroid.R;
import pl.scribedroid.input.Utils;
import pl.scribedroid.input.classificator.Classificator;
import roboguice.test.RobolectricRoboTestRunner;
import android.app.Activity;
import android.content.Context;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GesturePoint;
import android.gesture.GestureStroke;
import android.graphics.Bitmap;
import android.util.Pair;

@RunWith(RobolectricRoboTestRunner.class)
public class NetworkTest {
	Context context;

	@Before
	public void setUp() {
		context = new Activity();
	}

	@Test
	public void testNetworkLoad() {
		// when
		NetworkImpl alphaNet = NetworkImpl.createFromRawResource(context, R.raw.alphanet, Classificator.SMALL_ALPHA);
		
		// then
		Assert.assertNotNull(alphaNet);
	}
//	
//	public void testNetworkSave() throws FileNotFoundException {
//		NetworkImpl net = NetworkImpl.createFromRawResource(
//				getInstrumentation().getTargetContext(), R.raw.alphanet);
//
//		GestureLibrary testLib = GestureLibraries.fromRawResource(
//				getInstrumentation().getTargetContext(), R.raw.test_alpha_asia);
//		assertNotNull(testLib);
//		assertTrue(testLib.load());
//
//		List<Pair<float[], String>> testSet = new ArrayList<Pair<float[], String>>();
//
//		Set<String> labels = testLib.getGestureEntries();
//		assertEquals(26, labels.size());
//		Pair<float[], float[][]> pca = loadPCA();
//
//		for (String l : labels) {
//			Gesture gesture = testLib.getGestures(l).get(0);
//
//			Bitmap in = Utils.getBitmapFromGesture(gesture);
//			if (in.getWidth() > in.getHeight()) {
//				in = Bitmap.createScaledBitmap(in, 20,
//						Math.max(20 * in.getHeight() / in.getWidth(), 1), true);
//			} else {
//				in = Bitmap.createScaledBitmap(in,
//						Math.max(20 * in.getWidth() / in.getHeight(), 1), 20,
//						true);
//			}
//
//			float[] sample = Utils.getVectorFromBitmap(in);
//			// PCA
//			sample = Utils.applyPCA(sample, pca.first, pca.second);
//
//			testSet.add(new Pair<float[], String>(sample, l));
//		}
//
//		assertEquals(26, testSet.size());
//
//		int right = 0;
//		for (Pair<float[], String> p : testSet) {
//			List<Pair<Character, Float>> netStrings = Utils.getBest(
//					net.answer(p.first), 1, Classificator.ALPHA);
//			System.out.println(p.second + " " + netStrings.get(0).first);
//
//			if (p.second.charAt(0) == netStrings.get(0).first)
//				right++;
//		}
//
//		int[] topology = net.getTopology();
//		net.save(getInstrumentation().getTargetContext().openFileOutput(
//				"tmp_net", Context.MODE_PRIVATE));
//		net = new NetworkImpl();
//		net.load(getInstrumentation().getTargetContext().openFileInput(
//				"tmp_net"));
//
//		int[] topologyAfterSave = net.getTopology();
//
//		assertEquals(topology.length, topologyAfterSave.length);
//		System.out.println(Arrays.toString(topology));
//		System.out.println(Arrays.toString(topologyAfterSave));
//		assertTrue(Arrays.equals(topology, topologyAfterSave));
//
//		int rightAfterSave = 0;
//		for (Pair<float[], String> p : testSet) {
//			List<Pair<Character, Float>> netStrings = Utils.getBest(
//					net.answer(p.first), 1, Classificator.ALPHA);
//			System.out.println(p.second + " " + netStrings.get(0).first);
//
//			if (p.second.charAt(0) == netStrings.get(0).first)
//				rightAfterSave++;
//		}
//		assertEquals(right, rightAfterSave);
//	}
//
//	public void testNetowrkLettersAndNumbers() {
//		NetworkImpl lplusnNet = NetworkImpl.createFromRawResource(
//				getInstrumentation().getTargetContext(), R.raw.l_plus_n_net);
//		assertNotNull(lplusnNet);
//
//		GestureLibrary testLib = GestureLibraries.fromRawResource(
//				getInstrumentation().getTargetContext(), R.raw.test_alpha_asia);
//		assertNotNull(testLib);
//		assertTrue(testLib.load());
//
//		List<Pair<float[], String>> testSet = new ArrayList<Pair<float[], String>>();
//
//		Set<String> labels = testLib.getGestureEntries();
//		assertEquals(26, labels.size());
//		Pair<float[], float[][]> pca = loadPCA();
//
//		for (String l : labels) {
//			Gesture gesture = testLib.getGestures(l).get(0);
//
//			Bitmap in = Utils.getBitmapFromGesture(gesture);
//			if (in.getWidth() > in.getHeight()) {
//				in = Bitmap.createScaledBitmap(in, 20,
//						Math.max(20 * in.getHeight() / in.getWidth(), 1), true);
//			} else {
//				in = Bitmap.createScaledBitmap(in,
//						Math.max(20 * in.getWidth() / in.getHeight(), 1), 20,
//						true);
//			}
//
//			float[] sample = Utils.getVectorFromBitmap(in);
//			// PCA
//			sample = Utils.applyPCA(sample, pca.first, pca.second);
//
//			testSet.add(new Pair<float[], String>(sample, l));
//		}
//
//		assertEquals(26, testSet.size());
//
//		int right = 0;
//		for (Pair<float[], String> p : testSet) {
//			List<Pair<Character, Float>> netStrings = Utils.getBest(
//					lplusnNet.answer(p.first), 1,
//					Classificator.ALPHA_AND_NUMBER);
//			System.out.println(p.second + " " + netStrings.get(0).first);
//
//			if (p.second.charAt(0) == netStrings.get(0).first)
//				right++;
//		}
//		System.out.println("Quality: " + right / 26.0);
//		assertTrue(right / 26.0 >= 0.6);
//	}
//
//	public void testNetowrkLettersOrNumber() {
//		NetworkImpl lornNet = NetworkImpl.createFromRawResource(
//				getInstrumentation().getTargetContext(), R.raw.l_or_n_net);
//		assertNotNull(lornNet);
//
//		GestureLibrary testLib = GestureLibraries.fromRawResource(
//				getInstrumentation().getTargetContext(), R.raw.test_alpha_asia);
//		assertNotNull(testLib);
//		assertTrue(testLib.load());
//
//		List<Pair<float[], String>> testSet = new ArrayList<Pair<float[], String>>();
//
//		Set<String> labels = testLib.getGestureEntries();
//		assertEquals(26, labels.size());
//		Pair<float[], float[][]> pca = loadPCA();
//
//		for (String l : labels) {
//			Gesture gesture = testLib.getGestures(l).get(0);
//
//			Bitmap in = Utils.getBitmapFromGesture(gesture);
//			if (in.getWidth() > in.getHeight()) {
//				in = Bitmap.createScaledBitmap(in, 20,
//						Math.max(20 * in.getHeight() / in.getWidth(), 1), true);
//			} else {
//				in = Bitmap.createScaledBitmap(in,
//						Math.max(20 * in.getWidth() / in.getHeight(), 1), 20,
//						true);
//			}
//
//			float[] sample = Utils.getVectorFromBitmap(in);
//			// PCA
//			sample = Utils.applyPCA(sample, pca.first, pca.second);
//
//			testSet.add(new Pair<float[], String>(sample, l));
//		}
//
//		assertEquals(26, testSet.size());
//
//		int right = 0;
//		for (Pair<float[], String> p : testSet) {
//			float[] result = lornNet.answer(p.first);
//			System.out.println(result[0] + " " + result[1]);
//
//			if (result[0] > result[1])
//				right++;
//		}
//		System.out.println("Quality: " + right / 26.0);
//		assertTrue(right / 26.0 >= 0.8);
//	}
//
//	public void testXORTrain() throws Exception {
//		int[] arch = { 2, 2, 1 };
//		Network net = new NetworkImpl(arch);
//
//		List<Vector> trainSet = new ArrayList<Vector>();
//		float[] t1 = { 0.0f, 0.0f };
//		trainSet.add(new Vector(t1));
//		float[] t2 = { 0.0f, 1.0f };
//		trainSet.add(new Vector(t2));
//		float[] t3 = { 1.0f, 1.0f };
//		trainSet.add(new Vector(t3));
//		float[] t4 = { 1.0f, 0.0f };
//		trainSet.add(new Vector(t4));
//		int[] labels = { 0, 1, 0, 1 };
//
//		for (int z = 0; z < 100; z++)
//			for (int i = 0; i < trainSet.size() - 1; ++i) {
//				net.train(trainSet.get(i).toArray(), labels[i]);
//			}
//
//		List<Vector> testSet = new ArrayList<Vector>();
//		float[] s1 = { 0.05f, 0.05f };
//		trainSet.add(new Vector(s1));
//		float[] s2 = { 0.0f, 0.8f };
//		trainSet.add(new Vector(s2));
//		float[] s3 = { 0.84f, 0.9f };
//		trainSet.add(new Vector(s3));
//		float[] s4 = { 0.87f, 0.07f };
//		trainSet.add(new Vector(s4));
//		int[] testLabels = { 0, 1, 0, 1 };
//
//		for (int i = 0; i < testSet.size() - 1; ++i) {
//			float[] y = net.answer(testSet.get(i).toArray());
//			int imax = -1;
//			float max = -1.0f;
//
//			for (int j = 0; j < y.length; ++j) {
//				if (y[j] > max) {
//					max = y[j];
//					imax = j;
//				}
//			}
//			assertEquals(testLabels[i], imax);
//		}
//
//	}
//
//	public void testTopology() {
//		Network net = new NetworkImpl(new int[0]);
//		assertEquals(net.getTopology().length, 0);
//
//		int[] arch = { 1, 2, 3 };
//		net = new NetworkImpl(arch);
//		assertEquals(3, net.getTopology().length);
//		assertEquals(2, net.getTopology()[0]);
//		assertEquals(3, net.getTopology()[1]);
//		assertEquals(3, net.getTopology()[2]);
//	}
//
////	public void testNetworkTrain() throws Exception {
////		GestureLibrary testLib = GestureLibraries.fromRawResource(
////				getInstrumentation().getTargetContext(), R.raw.test_alpha_asia);
////		assertNotNull(testLib);
////		assertTrue(testLib.load());
////
////		List<Pair<float[], String>> trainSet = new ArrayList<Pair<float[], String>>();
////
////		Set<String> labels = testLib.getGestureEntries();
////		assertEquals(26, labels.size());
////		Pair<float[], float[][]> pca = loadPCA();
////
////		for (String l : labels) {
////			Gesture gesture = testLib.getGestures(l).get(0);
////
////			Bitmap in = Utils.getBitmapFromGesture(gesture);
////			if (in.getWidth() > in.getHeight()) {
////				in = Bitmap.createScaledBitmap(in, 20,
////						Math.max(20 * in.getHeight() / in.getWidth(), 1), true);
////			} else {
////				in = Bitmap.createScaledBitmap(in,
////						Math.max(20 * in.getWidth() / in.getHeight(), 1), 20,
////						true);
////			}
////
////			float[] sample = Utils.getVectorFromBitmap(in);
////			// PCA
////			sample = Utils.applyPCA(sample, pca.first, pca.second);
////
////			trainSet.add(new Pair<float[], String>(sample, l));
////		}
////
////		assertEquals(26, trainSet.size());
////
////		int[] arch = { 80, 60, 26 };
////
////		Network net = new NetworkImpl(arch);
////		for (int z = 0; z < 100; z++)
////			for (int i = 0; i < trainSet.size() - 1; ++i) {
////				net.train(trainSet.get(i).first,
////						toLabel(trainSet.get(i).second));
////			}
////
////		for (Pair<float[], String> p : trainSet) {
////			List<Pair<Character, Float>> netStrings = Utils.getBest(
////					net.answer(p.first), 1, Classificator.ALPHA);
////			System.out.println(p.second + " " + netStrings.get(0).first);
////			// assertEquals(p.second,netStrings.get(0).second);
////		}
////	}
//
////	public void testNetworkTrainBig() throws Exception {
////		GestureLibrary testLib = GestureLibraries.fromRawResource(
////				getInstrumentation().getTargetContext(), R.raw.test_alpha_asia);
////		assertNotNull(testLib);
////		assertTrue(testLib.load());
////
////		List<Pair<float[], String>> trainSet = new ArrayList<Pair<float[], String>>();
////
////		Set<String> labels = testLib.getGestureEntries();
////		assertEquals(26, labels.size());
////
////		for (String l : labels) {
////			Gesture gesture = testLib.getGestures(l).get(0);
////
////			float[] sample = GestureUtils.spatialSampling(gesture, 20);
////
////			trainSet.add(new Pair<float[], String>(sample, l));
////		}
////
////		assertEquals(26, trainSet.size());
////
////		int[] arch = { 400, 150, 26 };
////
////		Network net = new NetworkImpl(arch);
////		for (int z = 0; z < 100; z++)
////			for (int i = 0; i < trainSet.size() - 1; ++i) {
////				net.train(trainSet.get(i).first,
////						toLabel(trainSet.get(i).second));
////			}
////
////		for (Pair<float[], String> p : trainSet) {
////			List<Pair<Character, Float>> netStrings = Utils.getBest(
////					net.answer(p.first), 1, Classificator.ALPHA);
////			System.out.println(p.second + " " + netStrings.get(0).first);
////			// assertEquals(p.second,netStrings.get(0).second);
////		}
////	}
//
//	private Pair<float[], float[][]> loadPCA() {
//		InputStream file_input = getInstrumentation().getTargetContext()
//				.getResources().openRawResource(R.raw.pca);
//		float[] mu = null;
//		float[][] trmx = null;
//		try {
//			DataInputStream data_in = new DataInputStream(file_input);
//
//			int muSize = data_in.readInt();
//			mu = new float[muSize];
//
//			for (int i = 0; i < muSize; ++i) {
//				mu[i] = data_in.readFloat();
//			}
//
//			int trmxRows = data_in.readInt();
//			int trmxCols = data_in.readInt();
//
//			trmx = new float[trmxRows][trmxCols];
//
//			for (int i = 0; i < trmxRows; ++i) {
//				for (int j = 0; j < trmxCols; ++j) {
//					trmx[i][j] = data_in.readFloat();
//				}
//			}
//
//			data_in.close();
//		} catch (IOException e) {
//			System.out.println("PCA IO Exception LOADING =: " + e);
//		}
//		return new Pair<float[], float[][]>(mu, trmx);
//	}
//
//	private int toLabel(String x) {
//		if (x.equals("a"))
//			return 0;
//		if (x.equals("b"))
//			return 1;
//		if (x.equals("c"))
//			return 2;
//		if (x.equals("d"))
//			return 3;
//		if (x.equals("e"))
//			return 4;
//		if (x.equals("f"))
//			return 5;
//		if (x.equals("g"))
//			return 6;
//		if (x.equals("h"))
//			return 7;
//		if (x.equals("i"))
//			return 8;
//		if (x.equals("j"))
//			return 9;
//		if (x.equals("k"))
//			return 10;
//		if (x.equals("l"))
//			return 11;
//		if (x.equals("m"))
//			return 12;
//		if (x.equals("n"))
//			return 13;
//		if (x.equals("o"))
//			return 14;
//		if (x.equals("p"))
//			return 15;
//		if (x.equals("q"))
//			return 16;
//		if (x.equals("r"))
//			return 17;
//		if (x.equals("s"))
//			return 18;
//		if (x.equals("t"))
//			return 19;
//		if (x.equals("u"))
//			return 20;
//		if (x.equals("v"))
//			return 21;
//		if (x.equals("w"))
//			return 22;
//		if (x.equals("x"))
//			return 23;
//		if (x.equals("y"))
//			return 24;
//		if (x.equals("z"))
//			return 25;
//		return -1;
//	}
//	
//	public void testPossibleGestures() {
//		// given
//		Gesture gesture = new Gesture();
//		ArrayList<GesturePoint> points = new ArrayList<GesturePoint>();
//		points.add(new GesturePoint(0, 0, System.currentTimeMillis() - 10));
//		gesture.addStroke(new GestureStroke(points));
//		points.add(new GesturePoint(0, 0, System.currentTimeMillis() - 10));
//		gesture.addStroke(new GestureStroke(points));
//		points.add(new GesturePoint(0, 0, System.currentTimeMillis() - 10));
//		gesture.addStroke(new GestureStroke(points));
//		points.add(new GesturePoint(0, 0, System.currentTimeMillis() - 10));
//		gesture.addStroke(new GestureStroke(points));
//		points.add(new GesturePoint(0, 0, System.currentTimeMillis() - 10));
//
//		System.out.println("Strokes: " + gesture.getStrokesCount() + " "
//				+ gesture.getStrokes().size());
//		// when
//		List<Gesture> gestures = Utils.getPossibleGestures(gesture);
//
//		// then
//		Assert.assertEquals(1, gestures.size());
//	}
}
