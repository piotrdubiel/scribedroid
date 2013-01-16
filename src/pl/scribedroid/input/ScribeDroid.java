package pl.scribedroid.input;

import java.util.List;

import pl.scribedroid.R;
import pl.scribedroid.input.dictionary.SuggestionManager;
import roboguice.RoboGuice;
import roboguice.inject.InjectResource;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.inputmethodservice.InputMethodService;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.Toast;

public class ScribeDroid extends InputMethodService implements
		OnSharedPreferenceChangeListener {
	private static final String TAG = "ScribeDroid";

	CandidateView candidateView;

	private InputMethodController currentInputMethod;
	private GestureInputMethod gestureInputMethod;
	private InputMethodController keyboardInputMethod;

	private boolean completionOn;

	private SuggestionManager suggest;

	private boolean vibrateOn;

	StringBuilder composing_text = new StringBuilder();

	@InjectResource(R.string.word_separators)
	String word_separators;

	@Override
	public void onCreate() {
		super.onCreate();
		RoboGuice.getBaseApplicationInjector(getApplication()).injectMembers(this);
	}

	@Override
	public void onInitializeInterface() {
	}

	@Override
	public View onCreateInputView() {
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

		gestureInputMethod = new GestureInputMethod(this);
		keyboardInputMethod = new KeyboardInputMethod(this);
		currentInputMethod = gestureInputMethod;

		loadPreferences();

		return currentInputMethod.inputView;
	}

	@Override
	public void onStartInput(EditorInfo info, boolean restarting) {
		super.onStartInput(info, restarting);

		switch (info.inputType & EditorInfo.TYPE_MASK_CLASS) {
		case EditorInfo.TYPE_CLASS_TEXT:
			int variation = info.inputType & EditorInfo.TYPE_MASK_VARIATION;
			if (variation == EditorInfo.TYPE_TEXT_VARIATION_PASSWORD
					|| variation == EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
				completionOn = false;
			}

			if (variation == EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
					|| variation == EditorInfo.TYPE_TEXT_VARIATION_URI
					|| variation == EditorInfo.TYPE_TEXT_VARIATION_FILTER) {
				completionOn = false;
			}

			if ((info.inputType & EditorInfo.TYPE_TEXT_FLAG_AUTO_COMPLETE) != 0) {
				completionOn = false;
			}
			break;
		}
		composing_text.setLength(0);
		refreshSuggestions();

		Log.d(TAG, "Completion: " + String.valueOf(completionOn));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.inputmethodservice.InputMethodService#onUpdateSelection(int,
	 * int, int, int, int, int)
	 */
	@Override
	public void onUpdateSelection(int oldSelStart, int oldSelEnd,
			int newSelStart, int newSelEnd, int candidatesStart,
			int candidatesEnd) {
		super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd);
		if (composing_text.length() > 0
				&& (newSelStart != candidatesEnd || newSelEnd != candidatesEnd)) {
			composing_text.setLength(0);
			refreshSuggestions();
			InputConnection ic = getCurrentInputConnection();
			if (ic != null) {
				ic.finishComposingText();
			}
		}
	}

	public void onStartInputView(EditorInfo info, boolean restarting) {
		super.onStartInputView(info, restarting);
	}

	@Override
	public View onCreateCandidatesView() {
		candidateView = new CandidateView(this);
		candidateView.setService(this);
		return candidateView;
	}

	@Override
	public void onFinishInput() {
		super.onFinishInput();
		composing_text.setLength(0);
		refreshSuggestions();
		setCandidatesViewShown(false);
	}

	// @Override
	// public void onDisplayCompletions(CompletionInfo[] c) {
	// Log.d(TAG, "OnDisplayCompletions");
	// if (completionOn) {
	// completions = c;
	// if (completions == null) {
	// candidateView.setSuggestions(null, false);
	// return;
	// }
	//
	// List<String> stringList = new ArrayList<String>();
	// for (int i=0; i<(completions != null ? completions.length : 0); i++) {
	// CompletionInfo ci = completions[i];
	// if (ci != null) stringList.add(ci.getText().toString());
	// }
	// candidateView.setSuggestions(stringList, true);
	// }
	// }

	void commitText() {
		InputConnection ic = getCurrentInputConnection();
		if (composing_text.length() > 0) {
			ic.commitText(composing_text, composing_text.length());
			if (suggest != null & suggest.isReady()) {
				Log.i(TAG, "Word " + composing_text + " is valid: "
						+ suggest.isValid(composing_text.toString()));
				if (suggest.isValid(composing_text.toString())) suggest.addToDictionary(composing_text.toString());
			}
			composing_text.setLength(0);
			refreshSuggestions();
		}
	}

	void enterCharacter(Character c) {
		if (c == null) return;

		InputConnection ic = getCurrentInputConnection();

		composing_text.append(c);
		if (!word_separators.contains(c.toString())) {
			ic.setComposingText(composing_text, composing_text.length());
		}
		else {
			commitText();
			//ic.commitText(c.toString(), 1);
		}

		// recentLabel.setText(c.toString());
		vibrate();
		refreshSuggestions();
		currentInputMethod.resetModifiers();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.inputmethodservice.InputMethodService#onDestroy()
	 */
	@Override
	public void onDestroy() {
		Log.i(TAG, "Destroy called - closing databases");
		super.onDestroy();
		suggest.close();
	}

	void delete() {
		InputConnection ic = getCurrentInputConnection();
		if (composing_text.length() > 0) {
			composing_text.deleteCharAt(composing_text.length() - 1);
			ic.setComposingText(composing_text, composing_text.length());
		}
		else {
			ic.deleteSurroundingText(1, 0);
		}
		refreshSuggestions();
	}

	void pickSuggestion(String word) {
		if (completionOn) {
			getCurrentInputConnection().commitText(word, word.length());
			if (!suggest.isValid(word)) {
				suggest.addToUserDictionary(word);
				Toast.makeText(this, "Added " + word + " to dictionary", Toast.LENGTH_SHORT).show();
			}
			setCandidatesViewShown(false);
		}
	}

	void refreshSuggestions() {
		Log.i(TAG, "REFRESH Suggestions");
		if (completionOn && suggest != null && suggest.isReady()) {
			// if (getCurrentInputConnection().getExtractedText(
			// new ExtractedTextRequest(), 0) == null) return;
			// String text = getCurrentInputConnection().getExtractedText(
			// new ExtractedTextRequest(), 0).text.toString();
			// int n = 1;
			// if (text.length() > 0) {
			// if (!wordSeparators.contains(Character.toString(text
			// .charAt(text.length() - 1)))) {
			// while (n < text.length()
			// && !wordSeparators.contains(Character.toString(text
			// .charAt(text.length() - n - 1))))
			// n++;
			// String word = (String) getCurrentInputConnection()
			// .getTextBeforeCursor(n, 0);
			// lastWordStart = n;
			// Log.d(TAG,
			// "Word is valid: "
			// + String.valueOf(suggest.isValid(word)));
			// if (word.length() > 1) {
			// List<String> suggestions = suggest.getSuggestions(word);
			// candidateView.setSuggestions(suggestions,
			// suggest.isValid(word));
			// setCandidatesViewShown(true);
			// return;
			// }
			// }
			// }
			if (composing_text.length() > 0) {
				String word = composing_text.toString();
				List<String> suggestions = suggest.getSuggestions(word);
				candidateView.setSuggestions(suggestions, suggest.isValid(word));
				setCandidatesViewShown(true);

			}
			else setCandidatesViewShown(false);
		}
		else setCandidatesViewShown(false);
	}

	public void switchInputMethod() {
		if (currentInputMethod == gestureInputMethod) currentInputMethod = keyboardInputMethod;
		else if (currentInputMethod == keyboardInputMethod) currentInputMethod = gestureInputMethod;
		setInputView(currentInputMethod.inputView);
	}

	private void loadPreferences() {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		gestureInputMethod.gestureInterval = Integer.parseInt(sharedPrefs.getString("gesture_interval", "300"));
		gestureInputMethod.gestureView.setFadeOffset(gestureInputMethod.gestureInterval);
		Log.d(TAG, "Interval preference: "
				+ String.valueOf(gestureInputMethod.gestureInterval));

		completionOn = sharedPrefs.getBoolean("use_dictionary", true);
		Log.d(TAG, "Completion: " + String.valueOf(completionOn));

		if (completionOn) {
			suggest = new SuggestionManager(this);
		}
		else {
			suggest = null;
		}

		vibrateOn = sharedPrefs.getBoolean("vibrate_on", true);
		Log.d(TAG, "Vibration: " + String.valueOf(vibrateOn));
	}

	void vibrate() {
		if (vibrateOn) {
			currentInputMethod.inputView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
		}
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		loadPreferences();
	}
}
