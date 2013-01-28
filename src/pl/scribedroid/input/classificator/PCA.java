package pl.scribedroid.input.classificator;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.util.Log;

public class PCA {
	private static final String TAG = "PCA";

	private float[] mu;
	private float[][] trmx;

	public PCA(Context context) throws IOException {
		InputStream file_input = context.getAssets().open("pca");

		try {
			DataInputStream data_in = new DataInputStream(file_input);

			Log.v(TAG, "Input loaded");

			int muSize = data_in.readInt();
			mu = new float[muSize];

			for (int i = 0; i < muSize; ++i) {
				mu[i] = data_in.readFloat();
			}

			Log.v(TAG, "MU " + String.valueOf(muSize) + " loaded");

			int trmxRows = data_in.readInt();
			int trmxCols = data_in.readInt();

			trmx = new float[trmxRows][trmxCols];

			for (int i = 0; i < trmxRows; ++i) {
				for (int j = 0; j < trmxCols; ++j) {
					trmx[i][j] = data_in.readFloat();
				}
			}
			Log.v(TAG, "TRMX " + String.valueOf(trmxRows) + "x"
					+ String.valueOf(trmxCols) + " loaded");

			data_in.close();
		}
		catch (IOException e) {
			System.out.println("PCA IO Exception LOADING =: " + e);
		}
	}

	/**
	 * Przeprowadza operację PCA na podanym wektorze. 
	 * Wektor wejściowy zapisany jako tablica wartości float jest przetwarzany na nową tablicę o długości mniejszej niż tablica wejściowa zawierającą wynik analizy.
	 * @param in
	 * @return
	 */
	public float[] applyPCA(float[] in) {
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
}
