package pl.scribedroid.input;

import java.util.ArrayList;
import java.util.List;

import pl.scribedroid.R;
import pl.scribedroid.input.classificator.ClassificationResult;
import pl.scribedroid.input.classificator.ClassificationResult.Label;
import pl.scribedroid.input.dictionary.SuggestionManager;
import pl.scribedroid.input.dictionary.TrigramDatabase;
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
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.widget.Toast;

public class ScribeDroid extends InputMethodService implements OnSharedPreferenceChangeListener {
	private static final String TAG = "ScribeDroid";

	SuggestionView suggestionView;

	private InputMethodController currentInputMethod;
	private GestureInputMethod gestureInputMethod;
	private InputMethodController keyboardInputMethod;

	private boolean completion_on;
	private boolean completion_settings;
	private boolean vibrateOn;
	private boolean trigramsOn;
	
	private SuggestionManager suggest;

	
	private TrigramDatabase trigram_database;

	StringBuilder composing_text = new StringBuilder();

	@InjectResource(R.string.word_separators)
	String word_separators;

	/** 
	 * Ładuje ustawienia i inicjuje pomocnicze narzędzia: słownik, bazę trigramów.
	 * Wstrzykuje zależności. 
	 * @see android.inputmethodservice.InputMethodService#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		RoboGuice.getBaseApplicationInjector(getApplication()).injectMembers(this);
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
		loadPreferences();		
		trigram_database = new TrigramDatabase(this);
	}
	
	/** 
	 * Tworzy widoki trybów wpisywania tekstu. 
	 * @see android.inputmethodservice.InputMethodService#onCreateInputView()
	 */
	@Override
	public View onCreateInputView() {
		
		gestureInputMethod = new GestureInputMethod(this);
		keyboardInputMethod = new KeyboardInputMethod(this);
		currentInputMethod = gestureInputMethod;

		return currentInputMethod.inputView;
	}

	/** 
	 * Metoda jest wywoływana przy rozpoczęciu wprowadzania  tekstu do innego pola. 
	 * Wykrywa typ pola i wyłącza sugestie dla pól, które oczekują na wpisanie hasła,  adresu e-mail czy adresu URL lub zażądały wyłączenia sugestii.
	 * @see android.inputmethodservice.InputMethodService#onStartInput(android.view.inputmethod.EditorInfo, boolean)
	 */
	@Override
	public void onStartInput(EditorInfo info, boolean restarting) {
		super.onStartInput(info, restarting);

		completion_on = true;
		switch (info.inputType & EditorInfo.TYPE_MASK_CLASS) {
		case EditorInfo.TYPE_CLASS_TEXT:
			int variation = info.inputType & EditorInfo.TYPE_MASK_VARIATION;
			if (variation == EditorInfo.TYPE_TEXT_VARIATION_PASSWORD || variation == EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
				completion_on = false;
			}

			if (variation == EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS || variation == EditorInfo.TYPE_TEXT_VARIATION_URI || variation == EditorInfo.TYPE_TEXT_VARIATION_FILTER) {
				completion_on = false;
			}

			if ((info.inputType & EditorInfo.TYPE_TEXT_FLAG_AUTO_COMPLETE) != 0) {
				completion_on = false;
			}			
			break;
		}
		composing_text.setLength(0);
		refreshSuggestions();

		Log.d(TAG, "Completion: " + String.valueOf(completion_settings && completion_on));
	}

	@Override
	public void onUpdateSelection(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd,
			int candidatesStart, int candidatesEnd) {
		super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd);
		if (composing_text.length() > 0 && (newSelStart != candidatesEnd || newSelEnd != candidatesEnd)) {
			composing_text.setLength(0);
			refreshSuggestions();
			InputConnection ic = getCurrentInputConnection();
			if (ic != null) {
				ic.finishComposingText();
			}
		}
	}

	/**
	 * Tworzy widok z listą sugestii
	 * @see android.inputmethodservice.InputMethodService#onCreateCandidatesView()
	 */
	@Override
	public View onCreateCandidatesView() {
		suggestionView = new SuggestionView(this);
		suggestionView.setService(this);
		return suggestionView;
	}

	/**
	 * Metoda jest wywoływana przy zakończeniu wprowadzania tekstu do pola. 
	 * Resetuje ona stan aplikacji, aby przygotować ją na przejście do następnego pola.
	 * @see android.inputmethodservice.InputMethodService#onFinishInput()
	 */
	@Override
	public void onFinishInput() {
		super.onFinishInput();
		composing_text.setLength(0);
		refreshSuggestions();
		setCandidatesViewShown(false);
	}

	/**
	 * Wprowadza aktualnie komponowany tekst do pola i odświeża listę sugestii.
	 */
	void commitText() {
		InputConnection ic = getCurrentInputConnection();
		if (composing_text.length() > 0) {
			ic.commitText(composing_text, composing_text.length());
			if (suggest != null && suggest.isReady()) {
				Log.i(TAG, "Word " + composing_text + " is valid: " + suggest.isValid(composing_text.toString()));
				if (suggest.isValid(composing_text.toString())) suggest.addToDictionary(composing_text.toString());
			}
			composing_text.setLength(0);
			refreshSuggestions();
		}
	}

	
	/**
	 * Wprowadza podaną literę do aktualnie komponowanego tekstu.
	 * Jeśli znak jest separatorem słowa, to wywołuje metodę commitText.
	 * @param c znak, który ma zostać wpisany
	 */
	void enterCharacter(Character c) {
		if (c == null) return;

		InputConnection ic = getCurrentInputConnection();

		composing_text.append(c);
		if (!word_separators.contains(c.toString())) {
			ic.setComposingText(composing_text, composing_text.length());
		}
		else {
			commitText();
			// ic.commitText(c.toString(), 1);
		}

		// recentLabel.setText(c.toString());
		vibrate();
		refreshSuggestions();
	}
	
	
	/**
	 * Przy włączonej analizie trigramów porównuje podany w argumencie rezultat (listę znaków zawartą w obiekcie ClassificationResult) z bazą trigramów 
	 * i wybiera najbardziej prawdopodobny wariant. 
	 * Jeśli ta opcja jest wyłączona, to wybiera najlepszy w klasyfikatora znak i wprowadza go do aktualnie komponowanego tekstu.
	 * Aktualizuje w obu przypadkach listę sugestii.
	 * @param result wynik rozpoznawania
	 */
	void enterCharacters(ClassificationResult result) {
		if (result == null) return;

		InputConnection ic = getCurrentInputConnection();

		String prefix = ic.getTextBeforeCursor(2, 0).toString();
		if (prefix.length() < 2 || trigramsOn == false) enterCharacter(result.getLabels()[0]);
		else {
			ArrayList<Label> trigrams = trigram_database.getSuggestions(prefix);
			Character c = result.combine(new ClassificationResult(trigrams, 0)).getLabels(1)[0];
			for (Label l : trigrams)
				Log.d(TAG, "Got trigram " + prefix+l.label+" "+l.belief);
			Log.d(TAG, "Got trigram " + c);
			enterCharacter(c);
		}
	}

	/**
	 * Zakończenie działania aplikacji skutkuje wywołaniem tej metody.
	 * Zamyka ona bazę danych trigramów oraz słownik.
	 * @see android.inputmethodservice.InputMethodService#onDestroy()
	 */
	@Override
	public void onDestroy() {
		Log.i(TAG, "Destroy called - closing databases");
		suggest.close();
		trigram_database.close();
		super.onDestroy();
	}

	/**
	 * Usuwa ostatni znak i odświeża listę sugestii.
	 */
	void delete() {
		Log.d(TAG, "delete");
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

	/**
	 * Usuwa ostatni wyraz za kursorem. Odświeża listę sugestii.
	 */
	void deleteAfterLongClick() {
		InputConnection ic = getCurrentInputConnection();
		if (composing_text.length() > 0) {
			composing_text.setLength(0);
			ic.setComposingText(composing_text, composing_text.length());
		}
		else {
			String text = getCurrentInputConnection().getExtractedText(new ExtractedTextRequest(), 0).text.toString();
			int n = 1;
			if (text.length() > 0) {
				if (word_separators.contains(Character.toString(text.charAt(text.length() - 1)))) {
					while (n < text.length() && word_separators.contains(Character.toString(text.charAt(text.length() - n - 1))))
						n++;
				}
				else {
					while (n < text.length() && !word_separators.contains(Character.toString(text.charAt(text.length() - n - 1))))
						n++;
				}
				Log.v(TAG, "To delete - " + getCurrentInputConnection().getTextBeforeCursor(n, 0));
				getCurrentInputConnection().deleteSurroundingText(n, 0);
			}
		}
		refreshSuggestions();
	}

	void pickSuggestion(String word) {
		if (completion_settings && completion_on) {
			getCurrentInputConnection().commitText(word, word.length());
			if (!suggest.isValid(word)) {
				suggest.addToUserDictionary(word);
				Toast.makeText(this, "Added " + word + " to dictionary", Toast.LENGTH_SHORT).show();
			}
			setCandidatesViewShown(false);
		}
	}

	
	/**
	 * Powoduje pobranie listy sugestii dla aktualnie wpisanego prefiksu 
	 * i wyświetlenie ich w widoku SuggestionView.
	 */
	void refreshSuggestions() {
		Log.i(TAG, "REFRESH Suggestions " + String.valueOf(suggest != null ? suggest.isReady() : false));
		if (completion_settings && completion_on && suggest != null && suggest.isReady()) {
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
				suggestionView.setSuggestions(suggestions, suggest.isValid(word));
				setCandidatesViewShown(true);

			}
			else setCandidatesViewShown(false);
		}
		else setCandidatesViewShown(false);
	}

	/**
	 * Powoduje przełączenie trybu wpisywania tekstu na następny.
	 * Jest wywoływany przez podklasy InputMethodController do zasygnalizowania zmiany trybu.
	 */
	public void switchInputMethod() {
		if (currentInputMethod == gestureInputMethod) currentInputMethod = keyboardInputMethod;
		else if (currentInputMethod == keyboardInputMethod) currentInputMethod = gestureInputMethod;
		setInputView(currentInputMethod.inputView);
		currentInputMethod.resetModifiers();
	}

	private void loadPreferences() {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		completion_settings = sharedPrefs.getBoolean("use_dictionary", true);
		Log.d(TAG, "Completion: " + String.valueOf(completion_settings));

		if (completion_settings) {
			suggest = new SuggestionManager(this);
		}
		else {
			suggest = null;
		}

		vibrateOn = sharedPrefs.getBoolean("vibrate_on", true);
		trigramsOn = sharedPrefs.getBoolean("use_trigrams", true);
		Log.d(TAG, "Vibration: " + String.valueOf(vibrateOn));
		Log.d(TAG, "Trigrams: " + String.valueOf(trigramsOn));
	}

	void vibrate() {
		if (vibrateOn) {
			currentInputMethod.inputView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
		}
	}

	
	/**
	 * Jeśli nastąpi zmiana w ustawieniach aplikacji, metoda ta zostanie wywołana, aby zasygnalizować potrzebę uaktualnienia. 
	 * Wszystkie ustawienia zostaną jeszcze raz załadowane.
	 * @see android.content.SharedPreferences.OnSharedPreferenceChangeListener#onSharedPreferenceChanged(android.content.SharedPreferences, java.lang.String)
	 */
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		loadPreferences();
	}
}
