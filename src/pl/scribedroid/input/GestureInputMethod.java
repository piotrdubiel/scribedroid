package pl.scribedroid.input;

import pl.scribedroid.R;
import pl.scribedroid.input.classificator.ClassificationResult;
import pl.scribedroid.input.classificator.Classificator;
import pl.scribedroid.settings.SettingsActivity;
import roboguice.inject.InjectResource;
import android.content.Intent;
import android.content.SharedPreferences;
import android.gesture.Gesture;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGestureListener;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.inject.Inject;

public class GestureInputMethod extends InputMethodController implements OnClickListener,
		OnLongClickListener {
	private static final String TAG = "GestureInput";
	Button typeSwitch;
	ImageButton deleteKey;
	ImageButton enterKey;
	ImageButton spaceKey;
	ToggleButton symbolSwitch;
	KeyboardView supportSymbolKeyboardView;
	GestureOverlayView gestureView;
	TextView recentLabel;
	ImageButton keyboardSwitch;

	@InjectResource(R.string.word_separators)
	String word_separators;

	@InjectResource(R.array.input_modes)
	String[] input_modes;

	@Inject
	Classificator classHandler;

	int current_mode;

	private int[] modes = { Classificator.CAPITAL_ALPHA, Classificator.SMALL_ALPHA,
			Classificator.DIGIT };

	int gestureInterval;
	boolean capsLock = false;

	private Object recognition_lock = new Object();

	private ClassificationResult current_result;

	/**
	 * Konstruktor inicjuje elementy widoku i ładuje klasyfikatory. 
	 * Wymaga podania klasy ScribeDroid, z którą będzie powiązany.
	 */
	public GestureInputMethod(ScribeDroid s) {
		super(s, R.layout.gesture_input_view);

		// altKey = (ToggleButton) inputView.findViewById(R.id.altKey);
		// shiftKey = (ToggleButton) inputView.findViewById(R.id.shiftKey);
		typeSwitch = (Button) inputView.findViewById(R.id.typeSwitch);
		deleteKey = (ImageButton) inputView.findViewById(R.id.deleteKey);
		enterKey = (ImageButton) inputView.findViewById(R.id.enterKey);
		spaceKey = (ImageButton) inputView.findViewById(R.id.spaceKey);
		symbolSwitch = (ToggleButton) inputView.findViewById(R.id.symbolSwitch);
		supportSymbolKeyboardView = (KeyboardView) inputView.findViewById(R.id.support_keyboard);
		gestureView = (GestureOverlayView) inputView.findViewById(R.id.gesture_overlay);
		recentLabel = (TextView) inputView.findViewById(R.id.recentLabel);
		keyboardSwitch = (ImageButton) inputView.findViewById(R.id.keyboardToggle);

		// altKey.setOnClickListener(this);
		// shiftKey.setOnClickListener(this);
		// shiftKey.setOnLongClickListener(this);
		typeSwitch.setOnClickListener(this);
		deleteKey.setOnClickListener(this);
		deleteKey.setOnLongClickListener(this);
		enterKey.setOnClickListener(this);
		enterKey.setOnLongClickListener(this);
		spaceKey.setOnClickListener(this);
		symbolSwitch.setOnClickListener(this);
		keyboardSwitch.setOnClickListener(this);

		gestureView.addOnGestureListener(new GestureProcessor());
		gestureView.setGestureStrokeLengthThreshold(0.0f);
		gestureView.setGestureStrokeType(GestureOverlayView.GESTURE_STROKE_TYPE_MULTIPLE);

		supportSymbolKeyboardView.setOnKeyboardActionListener(new SymbolProcessor());
		supportSymbolKeyboardView.setKeyboard(new Keyboard(service, R.xml.symbols));

		// currentType = Classificator.ALPHA;
		current_mode = 0;
		typeSwitch.setText(input_modes[0]);

		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(service);

		gestureInterval = Integer.parseInt(sharedPrefs.getString("gesture_interval", "300"));
		gestureView.setFadeOffset(gestureInterval);

		Log.d(TAG, "Interval preference: " + String.valueOf(gestureInterval));
	}

	
	/**
	 * Obsługuje długie wciśnięcie przycisków:
	 * - Delete – usunięcie ostatniego wyrazu
	 * - Return – przejście do ustawień
	 * @see android.view.View.OnLongClickListener#onLongClick(android.view.View)
	 */
	public boolean onLongClick(View v) {
		if (v.getId() == R.id.enterKey) {
			Intent intent = new Intent(service.getBaseContext(), SettingsActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			service.getApplication().startActivity(intent);
		}
		if (v.getId() == R.id.deleteKey) {
			Log.d(TAG, "DELETE LONG CLICK");
			Toast.makeText(service, R.string.remove_word, Toast.LENGTH_SHORT).show();
			service.deleteAfterLongClick();
		}
		return true;
	}

	
	/**
	 * Obsługuje wciśnięcie przycisków:
	 * - zmiany widoku
	 * - przycisku Delete
	 * - zmiany typu rozpoznawanych gestów
	 * - pokazania/schowania klawiatury z symbolami
	 * - przycisku Spacja
	 * - przycisku Return 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	public void onClick(View v) {
		if (v.getId() == R.id.typeSwitch) {
			current_mode++;
			if (current_mode >= modes.length) current_mode = 0;

			typeSwitch.setText(input_modes[current_mode]);
		}
		if (v.getId() == R.id.deleteKey) {
			Log.d(TAG, "delete key");
			service.delete();
		}
		if (v.getId() == R.id.enterKey) {
			Log.d(TAG, "enter key");
			service.sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER);
			service.refreshSuggestions();
		}
		if (v.getId() == R.id.spaceKey) {
			Log.d(TAG, "space key");
			service.enterCharacter('\u0020');
		}
		if (v.getId() == R.id.symbolSwitch) {
			showSymbols(symbolSwitch.isChecked());
		}
		if (v.getId() == R.id.keyboardToggle) {
			Log.i(TAG, "Input switch requested");
			service.switchInputMethod();
		}
	}

	/**
	 * Pokazuje lub chowa klawiaturę z symbolami
	 * @param visible true, jeśli klawiatura ma być widoczna, false w przeciwnym przypadku
	 */
	private void showSymbols(boolean visible) {
		if (visible) {
			supportSymbolKeyboardView.setVisibility(View.VISIBLE);
			gestureView.setVisibility(View.GONE);
		}
		else {
			supportSymbolKeyboardView.setVisibility(View.GONE);
			gestureView.setVisibility(View.VISIBLE);
		}
		symbolSwitch.setChecked(visible);
	}

	private class GestureProcessor implements OnGestureListener {
		private CommitTextTask current_task;

		public void onGestureStarted(GestureOverlayView overlay, MotionEvent event) {
			Log.d(TAG, "GESTURE STARTED " + System.currentTimeMillis());

			keyboardSwitch.setEnabled(false);
			typeSwitch.setEnabled(false);
			symbolSwitch.setEnabled(false);
			deleteKey.setEnabled(false);
			spaceKey.setEnabled(false);
			enterKey.setEnabled(false);

			synchronized (recognition_lock) {
				if (current_task != null) overlay.removeCallbacks(current_task);
			}
		}

		public void onGestureEnded(GestureOverlayView overlay, MotionEvent event) {
			Log.d(TAG, "GESTURE ENDED " + System.currentTimeMillis());

			keyboardSwitch.setEnabled(true);
			typeSwitch.setEnabled(true);
			symbolSwitch.setEnabled(true);
			deleteKey.setEnabled(true);
			spaceKey.setEnabled(true);
			enterKey.setEnabled(true);

			new RecognitionTask().execute(overlay.getGesture());

			synchronized (recognition_lock) {
				overlay.removeCallbacks(current_task);
				current_task = new CommitTextTask();
				overlay.postDelayed(current_task, gestureInterval);
			}
		}

		public void onGesture(GestureOverlayView overlay, MotionEvent event) {}

		public void onGestureCancelled(GestureOverlayView overlay, MotionEvent event) {}

		private class RecognitionTask extends AsyncTask<Gesture, Void, ClassificationResult> {

			@Override
			protected ClassificationResult doInBackground(Gesture... gestures) {
				return classHandler.classify(gestures[0], modes[current_mode]);
			}

			@Override
			protected void onPostExecute(ClassificationResult result) {
				super.onPostExecute(result);
				synchronized (recognition_lock) {
					current_result = result;
				}
			}
		}

		private class CommitTextTask implements Runnable {
			@Override
			public void run() {
				Log.d(TAG, "Commit text");
				synchronized (recognition_lock) {
					service.enterCharacters(current_result);
					current_result = null;
					current_task = null;
					gestureView.clear(false);
				}
			}
		}
	}

	private class SymbolProcessor implements OnKeyboardActionListener {
		public void onKey(int primaryCode, int[] keyCodes) {
			service.enterCharacter((char) primaryCode);
			showSymbols(false);
		}

		public void onPress(int primaryCode) {}

		public void onRelease(int primaryCode) {}

		public void onText(CharSequence text) {}

		public void swipeDown() {}

		public void swipeLeft() {}

		public void swipeRight() {}

		public void swipeUp() {}
	}
}