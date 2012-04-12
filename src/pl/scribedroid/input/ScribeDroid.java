package pl.scribedroid.input;

import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureStore;
import android.gesture.Prediction;
import android.graphics.Bitmap;
import android.inputmethodservice.InputMethodService;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import pl.scribedroid.R;

public class ScribeDroid extends InputMethodService implements GestureOverlayView.OnGesturePerformedListener, OnClickListener, OnLongClickListener {
	private View inputView;
	private GestureLibrary alphaLibrary;
	private GestureLibrary numLibrary;
	private GestureLibrary specialLibrary;
	
	private GestureLibrary currentLibrary;
	
	private ToggleButton altKey;
	private ToggleButton shiftKey;
	private Button typeSwitch;
    
	@Override public void onCreate() {
        super.onCreate();
    }
	

	public void onStartInputView(EditorInfo info, boolean restarting) {
		super.onStartInputView(info, restarting);
        alphaLibrary = GestureLibraries.fromFile(new File(Environment.getExternalStorageDirectory(), "alpha"));
        numLibrary = GestureLibraries.fromRawResource(this, R.raw.num);
        specialLibrary = GestureLibraries.fromRawResource(this, R.raw.special);
        alphaLibrary.setOrientationStyle(GestureStore.ORIENTATION_SENSITIVE);
        alphaLibrary.setSequenceType(GestureStore.SEQUENCE_SENSITIVE);
        alphaLibrary.save();
        currentLibrary=alphaLibrary;
        if (!alphaLibrary.load()) {
        	Toast.makeText(this, "Can't load alpha gestures",Toast.LENGTH_LONG).show();
        }
	}
	
	@Override public void onInitializeInterface() {
    	
    }
    
    @Override public View onCreateInputView() {
        inputView = getLayoutInflater().inflate(R.layout.input, null);

        GestureOverlayView gestures = (GestureOverlayView) inputView.findViewById(R.id.gestures);
        gestures.addOnGesturePerformedListener(this);
        gestures.setGestureColor(R.color.gesture);
        
        altKey = (ToggleButton) inputView.findViewById(R.id.altKey);
        shiftKey = (ToggleButton) inputView.findViewById(R.id.shiftKey);
        typeSwitch = (Button) inputView.findViewById(R.id.typeSwitch);
        
        typeSwitch.setOnClickListener(this);
        
        return inputView;
    }

	@Override
	public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
	    ArrayList<Prediction> predictions=currentLibrary.recognize(gesture);
	    
	    if (predictions.size() > 0) {
	        Prediction prediction = predictions.get(0);
	        if (prediction.score>1.0) {
	        	Log.v("keyboardTest", "Prediction: "+prediction.name+" "+String.valueOf(prediction.score));
	        	Log.v("keyboardTest", "Second: "+predictions.get(1).name+" "+String.valueOf(predictions.get(1).score));
	        	
	        	if (prediction.name.startsWith("!#")) {
	        		if (prediction.name.endsWith("space")) {
	        			getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE));
	        			getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SPACE));
	        		}
	        		else if (prediction.name.endsWith("delete")) {
	        			Log.v("keyboardTest", "DELETE");
	        			getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
	        			getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
	        		}
	        	}
	        	else {
	        		getCurrentInputConnection().commitText(processText(prediction.name), prediction.name.length());	
	        	}	        	
	        }
	    }
    	Log.v("keyboardTest","Saving bitmap");
    	Bitmap gest=gesture.toBitmap(100, 100, 0, R.color.red);   	
    	File file=new File(Environment.getExternalStorageDirectory(), "gest.png");
    	try {
			FileOutputStream os=new FileOutputStream(file);
			gest.compress(Bitmap.CompressFormat.PNG, 100, os);
			os.flush();
			os.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			Log.e("keyboardTest", e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e("keyboardTest", e.getMessage());
		}
	}


	private String processText(String text) {
		if (altKey.isChecked()) {
			switch (text.charAt(0)) {
				case 'a': text="ą";	break;
				case 'c': text="ć";	break;
				case 'e': text="ę";	break;
				case 'l': text="ł";	break;
				case 'n': text="ń";	break;
				case 'o': text="ó";	break;
				case 's': text="ś";	break;
				case 'x': text="ź";	break;
				case 'z': text="ż";	break;
			}
			altKey.setChecked(false);
		}
		if (shiftKey.isChecked()) {
			text=text.toUpperCase();
		}
		return text;
	}


	@Override
	public void onClick(View v) {
		if (v.getId()==R.id.typeSwitch) {
			if (currentLibrary==alphaLibrary) {
				currentLibrary=numLibrary;
				typeSwitch.setText(R.string.numOn);
			}
			else if (currentLibrary==numLibrary) {
				currentLibrary=specialLibrary;
				typeSwitch.setText(R.string.specialOn);
			}
			else {
				currentLibrary=alphaLibrary;
				typeSwitch.setText(R.string.alphaOn);
			}
		}
		
	}


	@Override
	public boolean onLongClick(View v) {
		// TODO Auto-generated method stub
		return false;
	}
}
