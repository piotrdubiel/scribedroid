package pl.scribedroid.input;

import java.security.KeyStore.LoadStoreParameter;
import java.util.List;
import java.util.Map;

import com.google.inject.Key;

import pl.scribedroid.R;
import pl.scribedroid.input.dictionary.SuggestionManager;
import roboguice.RoboGuice;
import roboguice.inject.InjectResource;
import roboguice.util.RoboContext;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.KeyboardView;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedTextRequest;
import android.widget.Toast;

public class ScribeDroid extends InputMethodService implements OnSharedPreferenceChangeListener {
	private static final String TAG = "ScribeDroid";

	CandidateView candidateView;
	
	private InputMethodController currentInputMethod;
	private GestureInputMethod gestureInputMethod;
	private InputMethodController keyboardInputMethod;
	private KeyboardView standardKeyboardView;
	
    private boolean completionOn;
    
    private int lastWordStart = 0;
    
    private SuggestionManager suggest;

	private boolean vibrateOn;	

	@InjectResource(R.string.word_separators) String wordSeparators;
    
	@Override public void onCreate() {
        super.onCreate();
        RoboGuice.getBaseApplicationInjector(getApplication()).injectMembers(this);
    }

	@Override public void onInitializeInterface() {}
    
    @Override public View onCreateInputView() {       
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this); 

        gestureInputMethod = new GestureInputMethod(this);
        keyboardInputMethod = new GestureInputMethod(this);
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
            if (variation == EditorInfo.TYPE_TEXT_VARIATION_PASSWORD ||
            		variation == EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
            	completionOn = false;
            }
            
            if (variation == EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS 
                    || variation == EditorInfo.TYPE_TEXT_VARIATION_URI
                    || variation == EditorInfo.TYPE_TEXT_VARIATION_FILTER) {
                completionOn = false;
            }
            
            if ((info.inputType&EditorInfo.TYPE_TEXT_FLAG_AUTO_COMPLETE) != 0) {
            	completionOn = false;
            }
            break;
        }
		refreshSuggestions();
		

		Log.d(TAG,"Completion: "+String.valueOf(completionOn));
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
		//Debug.stopMethodTracing();
	}
	
//	@Override 
//	public void onDisplayCompletions(CompletionInfo[] c) {
//		Log.d(TAG, "OnDisplayCompletions");
//        if (completionOn) {
//            completions = c;
//            if (completions == null) {
//            	candidateView.setSuggestions(null, false);
//                return;
//            }
//            
//            List<String> stringList = new ArrayList<String>();
//            for (int i=0; i<(completions != null ? completions.length : 0); i++) {
//                CompletionInfo ci = completions[i];
//                if (ci != null) stringList.add(ci.getText().toString());
//            }
//            candidateView.setSuggestions(stringList, true);
//        }
//    }
	
	public void pickSuggestion(String word) {
        if (completionOn) {
        	getCurrentInputConnection().deleteSurroundingText(lastWordStart, 0);
        	getCurrentInputConnection().commitText(word, 0);
        	if (!suggest.isValid(word)) {
        		suggest.addToUserDictionary(word);
        		Toast.makeText(this, "Dodano "+word+" do słownika", Toast.LENGTH_SHORT).show();
        	}
        	setCandidatesViewShown(false);
        }
    }
	
	private void refreshSuggestions() {
		if (completionOn && suggest != null && suggest.isReady()) {
			if (getCurrentInputConnection().getExtractedText(new ExtractedTextRequest(), 0) == null) return;
			String text=getCurrentInputConnection().getExtractedText(new ExtractedTextRequest(), 0).text.toString();
			int n=1;
			if (text.length()>0) {
				if (!wordSeparators.contains(Character.toString(text.charAt(text.length()-1)))) {
					while (n<text.length() && !wordSeparators.contains(Character.toString(text.charAt(text.length()-n-1)))) n++;
					String word=(String) getCurrentInputConnection().getTextBeforeCursor(n, 0);
					lastWordStart=n;
					Log.d(TAG, "Word is valid: "+String.valueOf(suggest.isValid(word)));
					if (word.length()>1) {
						List<String> suggestions=suggest.getSuggestions(word);
						candidateView.setSuggestions(suggestions,suggest.isValid(word));
						setCandidatesViewShown(true);					    
					    return;
					}
				}
			}
		}
		setCandidatesViewShown(false);
	}
	
//	private void showKeyboard(boolean visible) {
//		Log.d(TAG, "Show keyboard");
//		if (visible) {
//			currentInputView = standardKeyboardView;
//		}
//		else {
//			currentInputView = gestureInputView;
//		}
//		 
//        setInputView(currentInputView);
//	}
	
	private void loadPreferences() {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		gestureInputMethod.gestureInterval = Integer.parseInt(sharedPrefs.getString("gesture_interval", "300"));
		gestureInputMethod.gestureView.setFadeOffset(gestureInputMethod.gestureInterval);
		Log.d(TAG,"Interval preference: "+String.valueOf(gestureInputMethod.gestureInterval));
		
		completionOn=sharedPrefs.getBoolean("use_dictionary", true);
		Log.d(TAG,"Completion: "+String.valueOf(completionOn));
		
		if (completionOn) {
			suggest = new SuggestionManager(this);
		}
		else {
			suggest = null;
		}
		
		vibrateOn=sharedPrefs.getBoolean("vibrate_on", true);
		Log.d(TAG,"Vibration: "+String.valueOf(vibrateOn));
	}
	
//	private void vibrate() {
//		if (vibrateOn && gestureInputView != null) {
//			gestureInputView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
//        }
//    }

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

		loadPreferences();
	}
}
