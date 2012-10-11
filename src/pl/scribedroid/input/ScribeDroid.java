package pl.scribedroid.input;

import java.util.List;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.gesture.Gesture;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGestureListener;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedTextRequest;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import pl.scribedroid.R;
import pl.scribedroid.input.dictionary.SuggestionManager;
import pl.scribedroid.settings.SettingsActivity;

public class ScribeDroid extends InputMethodService implements OnClickListener, OnLongClickListener, OnSharedPreferenceChangeListener {
	private static final String TAG = "ScribeDroid";

	private View inputView;
	private ToggleButton altKey;
	private ToggleButton shiftKey;
	private Button typeSwitch;
	private ImageButton deleteKey;
	private ImageButton enterKey;
	private ImageButton spaceKey;
	private ToggleButton symbolSwitch;	
	private KeyboardView symbolKeyboard;
	private GestureOverlayView gestureView;
	private TextView recentLabel;
	private CandidateView candidateView;
	
	private Classificator classHandler;
	
    private boolean capsLock = false;   
    
    private boolean completionOn;
    
    private int lastWordStart = 0;
	
    private int currentType;
    
    private int gestureInterval;
    
    private String wordSeparators;
    
    private SuggestionManager suggest;

	private boolean vibrateOn;
    
	@Override public void onCreate() {
        super.onCreate();
        //Debug.startMethodTracing("scribedroid");

        wordSeparators = getResources().getString(R.string.word_separators);
    }

	@Override public void onInitializeInterface() {}
    
    @Override public View onCreateInputView() {
        inputView = getLayoutInflater().inflate(R.layout.input, null);
        
        altKey = (ToggleButton) inputView.findViewById(R.id.altKey);
        shiftKey = (ToggleButton) inputView.findViewById(R.id.shiftKey);
        typeSwitch = (Button) inputView.findViewById(R.id.typeSwitch);
        deleteKey = (ImageButton) inputView.findViewById(R.id.deleteKey);
        enterKey = (ImageButton) inputView.findViewById(R.id.enterKey);
        spaceKey = (ImageButton) inputView.findViewById(R.id.spaceKey);
        symbolSwitch = (ToggleButton) inputView.findViewById(R.id.symbolSwitch);
        
        recentLabel = (TextView) inputView.findViewById(R.id.recentLabel);
        
        typeSwitch.setOnClickListener(this);
        
        altKey.setOnClickListener(this);
        
        shiftKey.setOnClickListener(this);
        shiftKey.setOnLongClickListener(this);
        
        deleteKey.setOnClickListener(this);
        deleteKey.setOnLongClickListener(this);
        
        enterKey.setOnClickListener(this);
        enterKey.setOnLongClickListener(this);
        
        spaceKey.setOnClickListener(this);
        
        symbolSwitch.setOnClickListener(this);

        gestureView = (GestureOverlayView) inputView.findViewById(R.id.gestures);
        gestureView.setGestureStrokeWidth(10.0f);
        
        GestureProcessor gestureProcessor = new GestureProcessor();
        
        gestureView.addOnGestureListener(gestureProcessor);
        
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        
        symbolKeyboard = (KeyboardView) inputView.findViewById(R.id.keyboard);
        symbolKeyboard.setOnKeyboardActionListener(new SymbolProcessor());
        symbolKeyboard.setKeyboard(new Keyboard(this,R.xml.symbols));
        
        new AsyncTask<Void,Void,Void>() {
			@Override
			protected Void doInBackground(Void... params) {
        		classHandler=new Classificator(ScribeDroid.this);
        		return null;
			}

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				inputView.setEnabled(false);
			}

			@Override
			protected void onPostExecute(Void result) {
				Log.i(TAG,"Classificator loaded");
				super.onPostExecute(result);
				inputView.setEnabled(true);
			}
        }.execute();
        
        loadPreferences();
        
		return inputView;
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
			
		currentType=Classificator.ALPHA;
		typeSwitch.setText(R.string.alphaOn);
		recentLabel.setText("");
		Log.v(TAG, "Type - alpha");
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
	
	public void onClick(View v) {
		vibrate();
		if (v.getId()==R.id.typeSwitch) {
			if (currentType==Classificator.ALPHA) {
				currentType=Classificator.NUMBER;
				typeSwitch.setText(R.string.numOn);
				Log.v(TAG, "Type - num");
			}
			else {
				currentType=Classificator.ALPHA;
				typeSwitch.setText(R.string.alphaOn);
				Log.v(TAG, "Type - alpha");
			}
		}
		if (v.getId()==R.id.deleteKey) {
			Log.d(TAG, "delete key");
			getCurrentInputConnection().deleteSurroundingText(1, 0);
			recentLabel.setText("");
			refreshSuggestions();
		}
		if (v.getId()==R.id.enterKey) {
			Log.d(TAG, "enter key");
			keyDownUp(KeyEvent.KEYCODE_ENTER);
			recentLabel.setText("");
			refreshSuggestions();
		}
		if (v.getId()==R.id.spaceKey) {
			Log.d(TAG, "space key");
			getCurrentInputConnection().commitText(" ", 1);
			recentLabel.setText("");
			refreshSuggestions();
		}
		if (v.getId()==R.id.symbolSwitch) {
			showSymbols(symbolSwitch.isChecked());
		}
	}
	
	public boolean onLongClick(View v) {
		if (v.getId()==R.id.shiftKey) {
			if (!capsLock) {
				shiftKey.setChecked(true);
				capsLock=true;
				getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_LEFT));
				
				Toast.makeText(this, "CapsLock", Toast.LENGTH_SHORT).show();
			}
			else {
				shiftKey.setChecked(false);
				capsLock=false;
				getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SHIFT_LEFT));
			}
		}
		if (v.getId()==R.id.enterKey) {
			Intent intent = new Intent(getBaseContext(), SettingsActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			getApplication().startActivity(intent);
		}
		if (v.getId()==R.id.deleteKey) {
			Log.d(TAG, "DELETE LONG CLICK");
			Toast.makeText(this, R.string.remove_word, Toast.LENGTH_SHORT).show();
			String text=getCurrentInputConnection().getExtractedText(new ExtractedTextRequest(), 0).text.toString();
			int n=1;
			if (text.length()>0) {
				if (wordSeparators.contains(Character.toString(text.charAt(text.length()-1)))) {
					while (n<text.length() && wordSeparators.contains(Character.toString(text.charAt(text.length()-n-1)))) n++;
				}
				else {
					while (n<text.length() && !wordSeparators.contains(Character.toString(text.charAt(text.length()-n-1)))) n++;
				}
				Log.v(TAG, "To delete - "+getCurrentInputConnection().getTextBeforeCursor(n, 0));
				getCurrentInputConnection().deleteSurroundingText(n, 0);
			}
			refreshSuggestions();
		}
		return true;
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
            
        	recentLabel.setText("");
            resetModifiers();
        }
    }

	private void keyDownUp(int keyCode) {
		getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
		getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyCode));
    }
	
	private void enterCharacter(Character c) {
		if (c==null) return;
		
		getCurrentInputConnection().commitText(processCharacter(c).toString(), processCharacter(c).toString().length());		
		
		recentLabel.setText(c.toString());
		vibrate();
		refreshSuggestions();
		resetModifiers();
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
		
	private Character processCharacter(Character c) {
		if (altKey.isChecked()) {
			switch (c) {
			case 'a': c='ą';	break;
			case 'c': c='ć';	break;
			case 'e': c='ę';	break;
			case 'l': c='ł';	break;
			case 'n': c='ń';	break;
			case 'o': c='ó';	break;
			case 's': c='ś';	break;
			case 'x': c='ź';	break;
			case 'z': c='ż';	break;
			}			
		}
		if (shiftKey.isChecked()) {
			c=Character.toUpperCase(c);
		}
		return c;
	}

	private void resetModifiers() {
		if (!capsLock) {
			getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SHIFT_LEFT));
			shiftKey.setChecked(false);
		}
		getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ALT_LEFT));
		altKey.setChecked(false);
	}
	
	
	private void showSymbols(boolean visible) {
		if (visible) {
			symbolKeyboard.setVisibility(View.VISIBLE);
			gestureView.setVisibility(View.GONE);
		}
		else {
			symbolKeyboard.setVisibility(View.GONE);
			gestureView.setVisibility(View.VISIBLE);
		}
		symbolSwitch.setChecked(visible);
	}
	
	private void loadPreferences() {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		gestureInterval=Integer.parseInt(sharedPrefs.getString("gesture_interval", "300"));
		gestureView.setFadeOffset(gestureInterval);
		Log.d(TAG,"Interval preference: "+String.valueOf(gestureInterval));
		
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
	
	private void vibrate() {
		if (vibrateOn && inputView != null) {
			inputView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        }
    }
	
	private class GestureProcessor implements OnGestureListener {
		private RecognitionTask currentTask;
		
		public void onGestureStarted(GestureOverlayView overlay, MotionEvent event) {
			Log.d(TAG, "GESTURE STARTED"+" "+String.valueOf(event.getEventTime()));
			altKey.setEnabled(false);
			shiftKey.setEnabled(false);
			typeSwitch.setEnabled(false);
			symbolSwitch.setEnabled(false);
			deleteKey.setEnabled(false);
			spaceKey.setEnabled(false);
			enterKey.setEnabled(false);

			if (currentTask!=null) overlay.removeCallbacks(currentTask);
		}
		
		public void onGestureEnded(GestureOverlayView overlay, MotionEvent event) {
			Log.d(TAG, "GESTURE ENDED"+" "+String.valueOf(event.getEventTime()));
			currentTask=new RecognitionTask(overlay.getGesture());
			overlay.postDelayed(currentTask,gestureInterval);
		}
		
		public void onGesture(GestureOverlayView overlay, MotionEvent event) {}
		public void onGestureCancelled(GestureOverlayView overlay, MotionEvent event) {}
		
		private class RecognitionTask implements Runnable {
			private Gesture gesture;
			public RecognitionTask(Gesture gesture) {
				this.gesture=gesture;
			}
			
			public void run() {
				GestureOverlayView gesturesView = (GestureOverlayView) inputView.findViewById(R.id.gestures);

				gesturesView.clear(false);
								
				new ClassificationTask().execute(gesture);
			}
		}
		
		private class ClassificationTask extends AsyncTask<Gesture, Void, Character> {
			@Override
			protected Character doInBackground(Gesture... gestures) {
				return classHandler.classify(gestures[0], currentType);
			}

			@Override
			protected void onPostExecute(Character result) {
				enterCharacter(result);
				
				altKey.setEnabled(true);
				shiftKey.setEnabled(true);
				typeSwitch.setEnabled(true);
				symbolSwitch.setEnabled(true);
				deleteKey.setEnabled(true);
				spaceKey.setEnabled(true);
				enterKey.setEnabled(true);
			}			
		}
	}
	
	private class SymbolProcessor implements OnKeyboardActionListener {
		public void onKey(int primaryCode, int[] keyCodes) {
			getCurrentInputConnection().commitText(Character.toString((char)primaryCode), 1);
			showSymbols(false);
			recentLabel.setText(Character.toString((char)primaryCode));
			vibrate();
		}

		public void onPress(int primaryCode) {}
		public void onRelease(int primaryCode) {}
		public void onText(CharSequence text) {}
		public void swipeDown() {}
		public void swipeLeft() {}
		public void swipeRight() {}
		public void swipeUp() {}
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Log.v(TAG, "Preference changed "+key);
		loadPreferences();
	}
}
