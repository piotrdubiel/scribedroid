package pl.scribedroid.training;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;

import pl.scribedroid.input.Utils;
import pl.scribedroid.input.ann.Network;
import pl.scribedroid.input.ann.NetworkImpl;
import pl.scribedroid.input.classificator.Classificator;
import android.app.IntentService;
import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureUtils;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

public class TrainingService extends IntentService {
	private Network network;

	public TrainingService() {
		super("TrainingService");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		// network.load(this.)
	}

	@Override
	protected void onHandleIntent(final Intent intent) {
		Log.i("TRAIN", "Train Service started");
		final Gesture gesture = intent.getParcelableExtra("gesture");
		final Character label = intent.getCharExtra("label", '?');
		new File(Network.DEFAULT_FILENAME).delete();
		new AsyncTask<Void, Void, Void>() {
			float[] sample;
			@Override
			protected Void doInBackground(final Void... params) {
				sample = GestureUtils.spatialSampling(gesture,
						28);
				if (!Arrays.asList(fileList()).contains(
						Network.DEFAULT_FILENAME)) {
					network = new NetworkImpl(Network.DEFAULT_TOPOLOGY);
				} else {
					try {
						network.load(openFileInput(Network.DEFAULT_FILENAME));
					} catch (final FileNotFoundException e) {
						e.printStackTrace();
					}
				}

				try {
					network.train(sample,
							Utils.code(label, Classificator.ALPHA_AND_NUMBER));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				return null;
			}

			@Override
			protected void onPostExecute(final Void x) {
				super.onPostExecute(x);

				final Pair<Character, Float> result = Utils.getBest(
						network.answer(sample), 1,
						Classificator.ALPHA_AND_NUMBER).get(0);
				Toast.makeText(
						TrainingService.this,
						"After training: " + result.first + " " + result.second,
						Toast.LENGTH_LONG).show();
			}

		}.execute(null);

	}

	@Override
	public void onDestroy() {
		super.onDestroy();

	}
}
