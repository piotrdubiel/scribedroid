package pl.scribedroid.input;

import pl.scribedroid.R;
import pl.scribedroid.input.classificator.ClassificationResult;
import pl.scribedroid.input.classificator.ClassificationResult.Label;
import pl.scribedroid.input.classificator.Classificator;
import pl.scribedroid.settings.SettingsActivity;
import roboguice.inject.InjectResource;
import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGestureListener;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.os.AsyncTask;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.inputmethod.ExtractedTextRequest;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.inject.Inject;

public class GestureInputMethod extends InputMethodController implements
		OnClickListener, OnLongClickListener {
	private static final String TAG = "GestureInput";
	ToggleButton altKey;
	ToggleButton shiftKey;
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

	@Inject
	Classificator classHandler;

	int gestureInterval;
	boolean capsLock = false;

	private Character current_result;

	public GestureInputMethod(ScribeDroid s) {
		super(s, R.layout.gesture_input_view);

		altKey = (ToggleButton) inputView.findViewById(R.id.altKey);
		shiftKey = (ToggleButton) inputView.findViewById(R.id.shiftKey);
		typeSwitch = (Button) inputView.findViewById(R.id.typeSwitch);
		deleteKey = (ImageButton) inputView.findViewById(R.id.deleteKey);
		enterKey = (ImageButton) inputView.findViewById(R.id.enterKey);
		spaceKey = (ImageButton) inputView.findViewById(R.id.spaceKey);
		symbolSwitch = (ToggleButton) inputView.findViewById(R.id.symbolSwitch);
		supportSymbolKeyboardView = (KeyboardView) inputView.findViewById(R.id.support_keyboard);
		gestureView = (GestureOverlayView) inputView.findViewById(R.id.gesture_overlay);
		recentLabel = (TextView) inputView.findViewById(R.id.recentLabel);
		keyboardSwitch = (ImageButton) inputView.findViewById(R.id.keyboardToggle);
		
		altKey.setOnClickListener(this);
		shiftKey.setOnClickListener(this);
		shiftKey.setOnLongClickListener(this);
		typeSwitch.setOnClickListener(this);
		deleteKey.setOnClickListener(this);
		deleteKey.setOnLongClickListener(this);
		enterKey.setOnClickListener(this);
		enterKey.setOnLongClickListener(this);
		spaceKey.setOnClickListener(this);
		symbolSwitch.setOnClickListener(this);
		keyboardSwitch.setOnClickListener(this);

		gestureView.addOnGestureListener(new GestureProcessor());

		supportSymbolKeyboardView.setOnKeyboardActionListener(new SymbolProcessor());
		supportSymbolKeyboardView.setKeyboard(new Keyboard(service, R.xml.symbols));

		//currentType = Classificator.ALPHA;
		typeSwitch.setText(R.string.alphaOn);
		recentLabel.setText("");
	}

	public boolean onLongClick(View v) {
		if (v.getId() == R.id.shiftKey) {
			if (!capsLock) {
				shiftKey.setChecked(true);
				capsLock = true;
				service.getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_LEFT));

				Toast.makeText(service, "CapsLock", Toast.LENGTH_SHORT).show();
			}
			else {
				shiftKey.setChecked(false);
				capsLock = false;
				service.getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SHIFT_LEFT));
			}
		}
		if (v.getId() == R.id.enterKey) {
			Intent intent = new Intent(service.getBaseContext(), SettingsActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			service.getApplication().startActivity(intent);
		}
		if (v.getId() == R.id.deleteKey) {
			Log.d(TAG, "DELETE LONG CLICK");
			Toast.makeText(service, R.string.remove_word, Toast.LENGTH_SHORT).show();
			String text = service.getCurrentInputConnection().getExtractedText(new ExtractedTextRequest(), 0).text.toString();
			int n = 1;
			if (text.length() > 0) {
				if (word_separators.contains(Character.toString(text.charAt(text.length() - 1)))) {
					while (n < text.length()
							&& word_separators.contains(Character.toString(text.charAt(text.length()
									- n - 1))))
						n++;
				}
				else {
					while (n < text.length()
							&& !word_separators.contains(Character.toString(text.charAt(text.length()
									- n - 1))))
						n++;
				}
				Log.v(TAG, "To delete - "
						+ service.getCurrentInputConnection().getTextBeforeCursor(n, 0));
				service.getCurrentInputConnection().deleteSurroundingText(n, 0);
			}
		}
		return true;
	}

	public void onClick(View v) {
		if (v.getId() == R.id.typeSwitch) {
//			if (currentType == Classificator.SMALL_ALPHA) {
//				currentType = Classificator.NUMBER;
//				typeSwitch.setText(R.string.numOn);
//				Log.v(TAG, "Type - num");
//			}
//			else if (currentType == Classificator.NUMBER) {
//				currentType = Classificator.ALPHA_AND_NUMBER;
//				typeSwitch.setText(R.string.autoOn);
//				Log.v(TAG, "Type - auto");
//			}
//			else {
//				currentType = Classificator.ALPHA;
//				typeSwitch.setText(R.string.alphaOn);
//				Log.v(TAG, "Type - alpha");
//			}
		}
		if (v.getId() == R.id.deleteKey) {
			Log.d(TAG, "delete key");
			service.delete();
		}
		if (v.getId() == R.id.enterKey) {
			Log.d(TAG, "enter key");
			service.sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER);
			recentLabel.setText("");
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

	public void resetModifiers() {
		if (!capsLock) {
			service.getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SHIFT_LEFT));
			shiftKey.setChecked(false);
		}
		service.getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ALT_LEFT));
		altKey.setChecked(false);
	}

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

		public void onGestureStarted(GestureOverlayView overlay,
				MotionEvent event) {
			Log.d(TAG, "GESTURE STARTED " + System.currentTimeMillis());
			altKey.setEnabled(false);
			shiftKey.setEnabled(false);
			typeSwitch.setEnabled(false);
			symbolSwitch.setEnabled(false);
			deleteKey.setEnabled(false);
			spaceKey.setEnabled(false);
			enterKey.setEnabled(false);

			synchronized (this) {
				if (current_task != null) overlay.removeCallbacks(current_task);
			}
		}

		public void onGestureEnded(GestureOverlayView overlay, MotionEvent event) {
			Log.d(TAG, "GESTURE ENDED " + System.currentTimeMillis());

			altKey.setEnabled(true);
			shiftKey.setEnabled(true);
			typeSwitch.setEnabled(true);
			symbolSwitch.setEnabled(true);
			deleteKey.setEnabled(true);
			spaceKey.setEnabled(true);
			enterKey.setEnabled(true);

			new RecognitionTask().execute(overlay.getGesture());

			synchronized (this) {
				overlay.removeCallbacks(current_task);
				current_task = new CommitTextTask();
				overlay.postDelayed(current_task, gestureInterval);
			}
		}

		public void onGesture(GestureOverlayView overlay, MotionEvent event) {
		}

		public void onGestureCancelled(GestureOverlayView overlay,
				MotionEvent event) {
		}

		private class RecognitionTask extends
				AsyncTask<Gesture, Void, Label> {

			@Override
			protected Label doInBackground(Gesture... gestures) {
				ClassificationResult r = classHandler.classify(gestures[0], Classificator.SMALL_ALPHA | Classificator.CAPITAL_ALPHA | Classificator.NUMBER);
				if (r == null) return null;
				Label[] labels = r.getLabelsWithBelief();				
				return	labels[0];
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
			 */
			@Override
			protected void onPostExecute(Label result) {
				super.onPostExecute(result);
				synchronized (this) {
					if (result != null) current_result = result.label;
				}
			}
		}

		private class CommitTextTask implements Runnable {
			@Override
			public void run() {
				Log.d(TAG, "Commit text");
				synchronized (this) {
					service.enterCharacter(current_result);
					current_result = null;
					current_task = null;
					gestureView.clear(false);
				}
			}
		}

		// private class ClassificationTask extends
		// AsyncTask<Pair<Gesture, Long>, Void, Pair<Character, Gesture>> {
		// Intent intent = new Intent(service, TrainingService.class);
		// long timestamp;
		//
		// @Override
		// protected Pair<Character, Gesture> doInBackground(
		// Pair<Gesture, Long>... gestures) {
		// intent.putExtra("gesture", gestures[0].first);
		// timestamp = gestures[0].second;
		// return classHandler.classify(gestures[0].first, currentType);
		// }
		//
		// @Override
		// protected void onPostExecute(Pair<Character, Gesture> result) {
		// if (result != null) {
		// service.enterCharacter(result.first);
		// if (System.currentTimeMillis() - timestamp > gestureInterval) {
		// gestureView.clear(true);
		//
		// }
		// intent.putExtra("label", result.first);
		// // service.startService(intent);
		// }
		// }
		// }
	}

	private class SymbolProcessor implements OnKeyboardActionListener {
		public void onKey(int primaryCode, int[] keyCodes) {
			//service.getCurrentInputConnection().commitText(Character.toString((char) primaryCode), 1);
			service.enterCharacter((char) primaryCode);
			showSymbols(false);
			recentLabel.setText(Character.toString((char) primaryCode));
		}

		public void onPress(int primaryCode) {
		}

		public void onRelease(int primaryCode) {
		}

		public void onText(CharSequence text) {
		}

		public void swipeDown() {
		}

		public void swipeLeft() {
		}

		public void swipeRight() {
		}

		public void swipeUp() {
		}
	}
}