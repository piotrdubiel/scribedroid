package pl.scribedroid.input;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pl.scribedroid.input.classificator.Classificator;
import android.gesture.Gesture;
import android.gesture.GestureStroke;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Environment;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;

public class Utils {
	public static final int TRAINING_CYCLES = 100;

	private static final int VECTOR_LENGTH = 784;
	public static final int RESULT_COUNT = 4;
	public static final char[] LETTERS = { 'a', '\u0105', 'b', 'c', '\u0107', 'd', 'e', '\u0119', 'f', 'g', 'h',
			'i', 'j', 'k', 'l', '\u0142', 'm', 'n', '\u0144', 'o', '\u00F3', 'p', 'q', 'r', 's', '\u015B', 't', 'u',
			'v', 'w', 'x', 'y', 'z', '\u017A', '\u017C' };

	public static List<Gesture> getAllPossibleGestures(Gesture g) {
		List<Gesture> gestures = new ArrayList<Gesture>();
		gestures.add(g);
		List<GestureStroke> strokes = g.getStrokes();
		if (strokes.size() > 1) {
			for (int i = 0; i < strokes.size() - 1; ++i) {
				Gesture gesture_to_add = new Gesture();
				for (GestureStroke s : strokes.subList(i, strokes.size() - 1))
					gesture_to_add.addStroke(s);
				gestures.add(gesture_to_add);
			}
			for (int i = strokes.size() - 1; i >= 1; --i) {
				Gesture gesture_to_add = new Gesture();
				for (GestureStroke s : strokes.subList(0, i))
					gesture_to_add.addStroke(s);
				gestures.add(gesture_to_add);
			}
			for (int i = 0; i < strokes.size() / 2; ++i) {
				Gesture gesture_to_add = new Gesture();
				for (GestureStroke s : strokes.subList(i, strokes.size() - i))
					gesture_to_add.addStroke(s);
				gestures.add(gesture_to_add);
			}
		}
		return gestures;
	}

	public static List<Gesture> getPossibleGestures(Gesture g) {
		List<Gesture> gestures = new ArrayList<Gesture>();
		List<GestureStroke> strokes = g.getStrokes();
		gestures.add(g);
		if (strokes.size() > 1) {
			for (int i = strokes.size() - 1; i >= 1; --i) {
				Gesture gesture_to_add = new Gesture();
				for (GestureStroke s : strokes.subList(0, i))
					gesture_to_add.addStroke(s);
				gestures.add(gesture_to_add);
			}
			for (int i = 0; i < strokes.size() / 2; ++i) {
				Gesture gesture_to_add = new Gesture();
				for (GestureStroke s : strokes.subList(i, strokes.size() - i))
					gesture_to_add.addStroke(s);
				gestures.add(gesture_to_add);
			}
		}
		return gestures;
	}

	public static float[] applyPCA(float[] in, float[] mu, float[][] trmx) {
		for (int i = 0; i < in.length; ++i) {
			in[i] -= mu[i];

		}

		float[] out = new float[trmx.length];
		for (int i = 0; i < trmx.length; ++i) {
			out[i] = 0.0f;
			for (int j = 0; j < trmx[i].length; ++j) {
				out[i] += in[j] * trmx[i][j];
			}
		}
		return out;
	}

	public static Bitmap dilation(Bitmap in, int n, float threshold) {
		Bitmap out = null;
		for (int k = 0; k < n; ++k) {
			out = Bitmap.createBitmap(in);
			for (int i = 1; i < in.getWidth() - 1; ++i) {
				for (int j = 1; j < in.getHeight() - 1; ++j) {
					int c = in.getPixel(i, j);
					float gray = toGray(c);
					if (gray < threshold) {
						if (toGray(in.getPixel(i - 1, j)) >= threshold || toGray(in.getPixel(i, j - 1)) >= threshold || toGray(in.getPixel(i + 1, j)) >= threshold || toGray(in.getPixel(i, j + 1)) >= threshold) {
							out.setPixel(i, j, Color.WHITE);
						}
						else {
							out.setPixel(i, j, Color.BLACK);
						}
					}
					else {
						out.setPixel(i, j, Color.WHITE);
					}

				}
			}
			in = out;
		}
		return out;
	}

	public static float toGray(int color) {
		int red, green, blue;
		red = Color.red(color);
		green = Color.green(color);
		blue = Color.blue(color);
		return (red + green + blue) / (255.0f * 3.0f);
	}

	public static int toColor(float gray) {
		int color;
		color = (255 << 24) | ((int) gray * 255 << 16) | ((int) gray * 255 << 8) | (int) gray * 255;
		return color;
	}

	public static int code(Character i, int type) {
		// if (type == Classificator.ALPHA_AND_NUMBER) {
		// switch (i) {
		// case '0':
		// return 0;
		// case '1':
		// return 1;
		// case '2':
		// return 2;
		// case '3':
		// return 3;
		// case '4':
		// return 4;
		// case '5':
		// return 5;
		// case '6':
		// return 6;
		// case '7':
		// return 7;
		// case '8':
		// return 8;
		// case '9':
		// return 9;
		// case 'a':
		// return 10;
		// case 'ą':
		// return 11;
		// case 'b':
		// return 12;
		// case 'c':
		// return 13;
		// case 'ć':
		// return 14;
		// case 'd':
		// return 15;
		// case 'e':
		// return 16;
		// case 'ę':
		// return 17;
		// case 'f':
		// return 18;
		// case 'g':
		// return 19;
		// case 'h':
		// return 20;
		// case 'i':
		// return 21;
		// case 'j':
		// return 22;
		// case 'k':
		// return 23;
		// case 'l':
		// return 24;
		// case 'ł':
		// return 25;
		// case 'm':
		// return 26;
		// case 'n':
		// return 27;
		// case 'ń':
		// return 28;
		// case 'o':
		// return 29;
		// case 'ó':
		// return 30;
		// case 'p':
		// return 31;
		// case 'q':
		// return 32;
		// case 'r':
		// return 33;
		// case 's':
		// return 34;
		// case 'ś':
		// return 35;
		// case 't':
		// return 36;
		// case 'u':
		// return 37;
		// case 'v':
		// return 38;
		// case 'w':
		// return 39;
		// case 'x':
		// return 40;
		// case 'y':
		// return 41;
		// case 'z':
		// return 42;
		// case 'ź':
		// return 43;
		// case 'ż':
		// return 44;
		// }
		// }
		return 0;
	}

	public static Character decode(int i, int type) {
		if (type == Classificator.DIGIT) {
			switch (i) {
			case 0:
				return '0';
			case 1:
				return '1';
			case 2:
				return '2';
			case 3:
				return '3';
			case 4:
				return '4';
			case 5:
				return '5';
			case 6:
				return '6';
			case 7:
				return '7';
			case 8:
				return '8';
			case 9:
				return '9';
			}
		}
		else if (type == Classificator.SMALL_ALPHA) {
			switch (i) {
			case 0:
				return 'a';
			case 1:
				return 'ą';
			case 2:
				return 'b';
			case 3:
				return 'c';
			case 4:
				return 'ć';
			case 5:
				return 'd';
			case 6:
				return 'e';
			case 7:
				return 'ę';
			case 8:
				return 'f';
			case 9:
				return 'g';
			case 10:
				return 'h';
			case 11:
				return 'i';
			case 12:
				return 'j';
			case 13:
				return 'k';
			case 14:
				return 'l';
			case 15:
				return 'ł';
			case 16:
				return 'm';
			case 17:
				return 'n';
			case 18:
				return 'ń';
			case 19:
				return 'o';
			case 20:
				return 'ó';
			case 21:
				return 'p';
			case 22:
				return 'q';
			case 23:
				return 'r';
			case 24:
				return 's';
			case 25:
				return 'ś';
			case 26:
				return 't';
			case 27:
				return 'u';
			case 28:
				return 'v';
			case 29:
				return 'w';
			case 30:
				return 'x';
			case 31:
				return 'y';
			case 32:
				return 'z';
			case 33:
				return 'ź';
			case 34:
				return 'ż';
			}
		}
		else if (type == Classificator.CAPITAL_ALPHA) {
			return Character.toUpperCase(decode(i, Classificator.SMALL_ALPHA));
		}
		else if (type == (Classificator.CAPITAL_ALPHA | Classificator.SMALL_ALPHA)) {
			if (i < 35) return decode(i, Classificator.CAPITAL_ALPHA);
			else return decode(i - 35, Classificator.SMALL_ALPHA);
		}
		else if (type == (Classificator.CAPITAL_ALPHA | Classificator.SMALL_ALPHA | Classificator.DIGIT)) {
			if (i < 10) return decode(i, Classificator.DIGIT);
			if (i >= 10 && i < 45) return decode(i - 10, Classificator.CAPITAL_ALPHA);
			else return decode(i - 45, Classificator.SMALL_ALPHA);
		}
		return null;
	}

	public static Bitmap getBitmapFromGesture(Gesture in) {
		int width = (int) Math.abs(in.getBoundingBox().right - in.getBoundingBox().left);
		int height = (int) Math.abs(in.getBoundingBox().top - in.getBoundingBox().bottom);

		if (width <= 0 || height <= 0) return null;

		if (width > height) {
			height = 20 * height / width;
			width = 20;
		}
		else {
			width = 20 * width / height;
			height = 20;
		}

		return in.toBitmap(width + 10, height + 10, 2, Color.WHITE);
	}

	public static float[] getVectorFromBitmap(Bitmap in) {
		float[] out = new float[VECTOR_LENGTH];

		for (int i = 0; i < out.length; ++i) {
			out[i] = 1.0f;
		}

		Point center = getCenter(in);

		int startx = (int) (14 - center.x);
		int starty = (int) (14 - center.y);

		for (int y = 0; y < in.getHeight(); ++y) {
			for (int x = 0; x < in.getWidth(); ++x) {
				try {
					int c = in.getPixel(x, y);
					out[28 * (starty + y) + startx + x] = 1.0f - toGray(c);
				}
				catch (Exception e) {
				}
			}
		}

		return out;
	}

	public static Point getCenter(Bitmap in) {
		float centerx = 0;
		float centery = 0;
		float sum = 0;
		for (int y = 0; y < in.getHeight(); ++y) {
			for (int x = 0; x < in.getWidth(); ++x) {
				float gray = toGray(in.getPixel(x, y));
				centerx += x * gray;
				centery += y * gray;
				sum += gray;
			}
		}

		centerx = centerx / Math.max(sum, 1);
		centery = centery / Math.max(sum, 1);

		return new Point((int) centerx, (int) centery);
	}

	public static File saveBitmap(Bitmap in, String filename) throws IOException {
		File dir = new File(Environment.getExternalStorageDirectory() + "/scribedroid/");
		Log.d("FILE CREATE", dir.getAbsolutePath());
		if (!dir.exists()) dir.mkdirs();
		File file = new File(dir, filename);

		FileOutputStream os = new FileOutputStream(file+".png");
		in.compress(Bitmap.CompressFormat.PNG, 100, os);
		os.flush();
		os.close();

		return file;
	}

	public static File saveVector(float[] in, String filename) throws Exception {
		if (in.length != VECTOR_LENGTH) throw new Exception("Vector length " + in.length);
		int size = (int) Math.sqrt(in.length);
		Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

		for (int i = 0; i < in.length; ++i) {
			int y = (int) Math.floor(i / 28);
			int x = i - 28 * y;
			output.setPixel(x, y, toColor(in[i]));
		}
		return saveBitmap(output, filename);
	}

	public static List<Pair<Character, Float>> getBest(float[] in, int n, int type) {
		List<Pair<Character, Float>> out = new ArrayList<Pair<Character, Float>>();
		List<Integer> indexList = new ArrayList<Integer>();

		for (int r = 0; r < n; ++r) {
			int imax = -1;
			float max = -1.0f;

			for (int i = 0; i < in.length; ++i) {
				if (in[i] > max && !indexList.contains(i)) {
					max = in[i];
					imax = i;
					indexList.add(i);
				}
			}

			if (imax == -1) return out;
			out.add(new Pair<Character, Float>(decode(imax, type), in[imax]));
		}
		return out;
	}

	public static int getKeyCode(String label) {
		if (label.equals("!#space")) {
			return KeyEvent.KEYCODE_SPACE;
		}
		else if (label.equals("!#delete")) {
			return KeyEvent.KEYCODE_DEL;
		}
		else if (label.equals("a")) {
			return KeyEvent.KEYCODE_A;
		}
		else if (label.equals("b")) {
			return KeyEvent.KEYCODE_B;
		}
		else if (label.equals("c")) {
			return KeyEvent.KEYCODE_C;
		}
		else if (label.equals("d")) {
			return KeyEvent.KEYCODE_D;
		}
		else if (label.equals("e")) {
			return KeyEvent.KEYCODE_E;
		}
		else if (label.equals("f")) {
			return KeyEvent.KEYCODE_F;
		}
		else if (label.equals("g")) {
			return KeyEvent.KEYCODE_G;
		}
		else if (label.equals("h")) {
			return KeyEvent.KEYCODE_H;
		}
		else if (label.equals("i")) {
			return KeyEvent.KEYCODE_I;
		}
		else if (label.equals("j")) {
			return KeyEvent.KEYCODE_J;
		}
		else if (label.equals("k")) {
			return KeyEvent.KEYCODE_K;
		}
		else if (label.equals("l")) {
			return KeyEvent.KEYCODE_L;
		}
		else if (label.equals("m")) {
			return KeyEvent.KEYCODE_M;
		}
		else if (label.equals("n")) {
			return KeyEvent.KEYCODE_N;
		}
		else if (label.equals("o")) {
			return KeyEvent.KEYCODE_O;
		}
		else if (label.equals("p")) {
			return KeyEvent.KEYCODE_P;
		}
		else if (label.equals("q")) {
			return KeyEvent.KEYCODE_Q;
		}
		else if (label.equals("r")) {
			return KeyEvent.KEYCODE_R;
		}
		else if (label.equals("s")) {
			return KeyEvent.KEYCODE_S;
		}
		else if (label.equals("t")) {
			return KeyEvent.KEYCODE_T;
		}
		else if (label.equals("u")) {
			return KeyEvent.KEYCODE_U;
		}
		else if (label.equals("v")) {
			return KeyEvent.KEYCODE_V;
		}
		else if (label.equals("w")) {
			return KeyEvent.KEYCODE_W;
		}
		else if (label.equals("x")) {
			return KeyEvent.KEYCODE_X;
		}
		else if (label.equals("y")) {
			return KeyEvent.KEYCODE_Y;
		}
		else if (label.equals("z")) {
			return KeyEvent.KEYCODE_Z;
		}
		else if (label.equals("0")) {
			return KeyEvent.KEYCODE_0;
		}
		else if (label.equals("1")) {
			return KeyEvent.KEYCODE_1;
		}
		else if (label.equals("2")) {
			return KeyEvent.KEYCODE_2;
		}
		else if (label.equals("3")) {
			return KeyEvent.KEYCODE_3;
		}
		else if (label.equals("4")) {
			return KeyEvent.KEYCODE_4;
		}
		else if (label.equals("5")) {
			return KeyEvent.KEYCODE_5;
		}
		else if (label.equals("6")) {
			return KeyEvent.KEYCODE_6;
		}
		else if (label.equals("7")) {
			return KeyEvent.KEYCODE_7;
		}
		else if (label.equals("8")) {
			return KeyEvent.KEYCODE_8;
		}
		else if (label.equals("9")) {
			return KeyEvent.KEYCODE_9;
		}
		return KeyEvent.KEYCODE_UNKNOWN;

	}
}
