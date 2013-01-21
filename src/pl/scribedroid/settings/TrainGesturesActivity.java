package pl.scribedroid.settings;

import java.util.ArrayList;
import pl.scribedroid.R;
import pl.scribedroid.input.Utils;
import pl.scribedroid.input.classificator.GestureLibraryClassificator;
import android.app.Activity;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class TrainGesturesActivity extends Activity implements OnClickListener {
	private static final String TAG = "TrainGestures";
	private static final float LENGTH_THRESHOLD = 5.0f;
	private TextView characterLabel;
	private Button nextButton;
	private Button backButton;
	private Button finishButton;
	private int currentChar;
	private GestureOverlayView gestureView;
	private GestureLibrary small_library;
	private GestureLibrary capital_library;
	private GestureLibrary digit_library;
	private Gesture currentGesture;

	private String characters;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.train_gestures);

		characters = "0123456789";
		for (char c : Utils.LETTERS) {
			characters += c;
			characters += Character.toUpperCase(c);
		}

		characterLabel = (TextView) findViewById(R.id.characterLabel);
		currentChar = 0;
		characterLabel.setText(Character.toString(characters.charAt(currentChar)));

		nextButton = (Button) findViewById(R.id.nextButton);
		backButton = (Button) findViewById(R.id.backButton);
		finishButton = (Button) findViewById(R.id.finishButton);

		nextButton.setOnClickListener(this);
		backButton.setOnClickListener(this);
		finishButton.setOnClickListener(this);

		gestureView = (GestureOverlayView) findViewById(R.id.gestures_overlay);
		gestureView.addOnGestureListener(new GesturesProcessor());

		// getFileStreamPath(ALPHA_FILENAME).delete();
		// getFileStreamPath(NUMBER_FILENAME).delete();

		small_library = GestureLibraries.fromFile(getFileStreamPath(GestureLibraryClassificator.USER_SMALL_FILENAME));
		small_library.load();

		capital_library = GestureLibraries.fromFile(getFileStreamPath(GestureLibraryClassificator.USER_CAPITAL_FILENAME));
		capital_library.load();

		digit_library = GestureLibraries.fromFile(getFileStreamPath(GestureLibraryClassificator.USER_DIGIT_FILENAME));
		digit_library.load();

		currentGesture = loadGesture(characters.charAt(currentChar));
		if (currentGesture != null) {
			gestureView.post(new Runnable() {
				public void run() {
					gestureView.setGesture(currentGesture);
				}
			});
		}

		Log.d(TAG, "Number of gestures in small_lib " + small_library.getGestureEntries().size());
		Log.d(TAG, "Number of gestures in capital_lib " + capital_library.getGestureEntries().size());
		Log.d(TAG, "Number of gestures in digit_lib " + digit_library.getGestureEntries().size());
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (currentGesture != null) {
			outState.putParcelable("gesture", currentGesture);
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		currentGesture = savedInstanceState.getParcelable("gesture");
		if (currentGesture != null) {
			gestureView.post(new Runnable() {
				public void run() {
					gestureView.setGesture(currentGesture);
				}
			});

			handleEnabled(nextButton);
			handleEnabled(backButton);
		}
	}

	public void onClick(View v) {
		if (v.getId() == R.id.nextButton) {
			if (currentGesture != null) {
				saveGesture();

				gestureView.clear(false);

				currentChar++;
				if (currentChar >= characters.length()) currentChar = characters.length() - 1;
				characterLabel.setText(Character.toString(characters.charAt(currentChar)));				

				Gesture nextGesture = loadGesture(characters.charAt(currentChar));
				if (nextGesture != null) {
					gestureView.setGesture(nextGesture);
					currentGesture = nextGesture;
				}
				else {
					currentGesture = null;
				}

			}
			else {
				Toast.makeText(this, getResources().getString(R.string.no_gesture_alert), Toast.LENGTH_LONG).show();
			}
		}
		if (v.getId() == R.id.backButton) {
			if (currentGesture != null) {
				saveGesture();
			}

			currentChar--;
			if (currentChar < 0) currentChar = 0;
			characterLabel.setText(Character.toString(characters.charAt(currentChar)));		

			currentGesture = null;

			Gesture prevGesture = loadGesture(characters.charAt(currentChar));
			if (prevGesture != null) {
				gestureView.setGesture(prevGesture);
				currentGesture = prevGesture;
			}
			else {
				currentGesture = null;
			}
		}
		if (v.getId() == R.id.finishButton) {
			if (currentGesture != null) {
				saveGesture();
				finish();
			}
			else {
				Toast.makeText(this, getResources().getString(R.string.no_gesture_alert), Toast.LENGTH_LONG).show();
			}
		}
		handleEnabled(nextButton);
		handleEnabled(backButton);
	}

	private Gesture loadGesture(Character label) {
		GestureLibrary current_library = null;
		if (Character.isDigit(label)) {
			current_library = digit_library;
		}
		else if (Character.isUpperCase(label)) {
			current_library = capital_library;
		}
		else if (Character.isLowerCase(label)) {
			current_library = small_library;
		}
		else return null;
		
		ArrayList<Gesture> result = current_library.getGestures(Character.toString(characters.charAt(currentChar)));
		if (result == null || result.isEmpty()) return null;
		else return result.get(0);
	}

	private void saveGesture() {
		Character label = characters.charAt(currentChar);
		Gesture lastGesture = loadGesture(label);	

		GestureLibrary current_library = null;
		if (Character.isDigit(label)) {
			current_library = digit_library;
		}
		else if (Character.isUpperCase(label)) {
			current_library = capital_library;
		}
		else if (Character.isLowerCase(label)) {
			current_library = small_library;
		}
		else return;
		
		if (lastGesture != null) {
			current_library.removeGesture(Character.toString(label), lastGesture);
		}
		current_library.addGesture(Character.toString(characters.charAt(currentChar)), currentGesture);
		current_library.save();
	}

	private void handleEnabled(Button b) {
		if (b == nextButton) {
			if (currentChar == characters.length() - 1) {
				nextButton.setEnabled(false);
				nextButton.setVisibility(View.GONE);
				finishButton.setVisibility(View.VISIBLE);
			}
			else {
				nextButton.setEnabled(true);
				nextButton.setVisibility(View.VISIBLE);
				finishButton.setVisibility(View.GONE);
			}
		}
		else if (b == backButton) {
			if (currentChar == 0) {
				backButton.setEnabled(false);
			}
			else {
				backButton.setEnabled(true);
			}
		}
	}

	private class GesturesProcessor implements GestureOverlayView.OnGestureListener {
		public void onGestureStarted(GestureOverlayView overlay, MotionEvent event) {
			nextButton.setEnabled(false);
			backButton.setEnabled(false);
			currentGesture = null;
		}

		public void onGesture(GestureOverlayView overlay, MotionEvent event) {}

		public void onGestureEnded(GestureOverlayView overlay, MotionEvent event) {
			currentGesture = overlay.getGesture();
			if (currentGesture.getLength() < LENGTH_THRESHOLD) {
				overlay.clear(false);
			}
			handleEnabled(nextButton);
			handleEnabled(backButton);
		}

		public void onGestureCancelled(GestureOverlayView overlay, MotionEvent event) {}
	}
}
