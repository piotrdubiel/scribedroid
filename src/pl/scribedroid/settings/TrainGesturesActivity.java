package pl.scribedroid.settings;

import java.util.ArrayList;
import pl.scribedroid.R;
import pl.scribedroid.input.Utils;
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
    private static final float LENGTH_THRESHOLD = 120.0f;
	private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz0123456789";
	private TextView characterLabel;
	private Button nextButton;
	private Button backButton;
	private Button finishButton;
	private int currentChar;
	private GestureOverlayView gestureView;
	private GestureLibrary currentLibrary;
	private Gesture currentGesture;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    setContentView(R.layout.train_gestures);
	    
	    characterLabel=(TextView) findViewById(R.id.characterLabel);
	    currentChar=0;
	    characterLabel.setText(Character.toString(CHARACTERS.charAt(currentChar)));
	    
	    nextButton = (Button) findViewById(R.id.nextButton);
	    backButton = (Button) findViewById(R.id.backButton);
	    finishButton = (Button) findViewById(R.id.finishButton);
	    
	    nextButton.setOnClickListener(this);
	    backButton.setOnClickListener(this);
	    finishButton.setOnClickListener(this);

	    gestureView = (GestureOverlayView) findViewById(R.id.gestures_overlay);
	    gestureView.addOnGestureListener(new GesturesProcessor());
	    
	    //getFileStreamPath(ALPHA_FILENAME).delete();
	    //getFileStreamPath(NUMBER_FILENAME).delete();
	    
	    currentLibrary=GestureLibraries.fromFile(getFileStreamPath(Utils.USER_ALPHA_FILENAME));
	    currentLibrary.load();
	    
	    currentGesture=loadGesture(Character.toString(CHARACTERS.charAt(currentChar)));
	    if (currentGesture!=null) {
	    	gestureView.post(new Runnable() {
	            public void run() {
	            	gestureView.setGesture(currentGesture);
	            }
	        });
	    }

    	Log.d(TAG, "Number of gestures "+String.valueOf(currentLibrary.getGestureEntries().size()));
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
		if (v.getId()==R.id.nextButton) {
			if (currentGesture!=null) {
				saveGesture();
				
				gestureView.clear(false);
				
				currentChar++;
				if (currentChar>=CHARACTERS.length()) currentChar=CHARACTERS.length()-1;
				characterLabel.setText(Character.toString(CHARACTERS.charAt(currentChar)));				
				if (CHARACTERS.charAt(currentChar)=='0') {
					currentLibrary=GestureLibraries.fromFile(getFileStreamPath(Utils.USER_NUMBER_FILENAME));
					currentLibrary.load();
				}
				
				Gesture nextGesture=loadGesture(Character.toString(CHARACTERS.charAt(currentChar)));
				if (nextGesture!=null) {
					gestureView.setGesture(nextGesture);
					currentGesture=nextGesture;
				}
				else {
					currentGesture=null;
				}
				
			}
			else {
				Toast.makeText(this, getResources().getString(R.string.no_gesture_alert), Toast.LENGTH_LONG).show();
			}
		}
		if (v.getId()==R.id.backButton) {
			if (currentGesture!=null) {
				saveGesture();
			}
			
			currentChar--;
			if (currentChar<0) currentChar=0;
		    characterLabel.setText(Character.toString(CHARACTERS.charAt(currentChar)));
		    if (CHARACTERS.charAt(currentChar)=='z') {
		    	currentLibrary=GestureLibraries.fromFile(getFileStreamPath(Utils.USER_ALPHA_FILENAME));
		    	currentLibrary.load();
		    }
		    
			currentGesture=null;

			Gesture prevGesture=loadGesture(Character.toString(CHARACTERS.charAt(currentChar)));
			if (prevGesture!=null) {
				gestureView.setGesture(prevGesture);
				currentGesture=prevGesture;
			}
			else {
				currentGesture=null;
			}
		}
		if (v.getId()==R.id.finishButton) {
			if (currentGesture!=null) {
				saveGesture();
				finish();				
			}
			else {
				Toast.makeText(this, getResources().getString(R.string.no_gesture_alert), Toast.LENGTH_LONG).show();
			}
		}
		handleEnabled(nextButton);
    	handleEnabled(backButton);
    	
    	Log.d(TAG, "Number of gestures "+String.valueOf(currentLibrary.getGestureEntries().size()));
	}
	
	private Gesture loadGesture(String label) {
		ArrayList<Gesture> result=currentLibrary.getGestures(Character.toString(CHARACTERS.charAt(currentChar)));
		if (result==null || result.isEmpty()) return null;
		else return result.get(0);
	}
	
	private void saveGesture() {
		Gesture lastGesture=loadGesture(Character.toString(CHARACTERS.charAt(currentChar)));
		if (lastGesture!=null) {
			currentLibrary.removeGesture(Character.toString(CHARACTERS.charAt(currentChar)), lastGesture);
		}
		currentLibrary.addGesture(Character.toString(CHARACTERS.charAt(currentChar)), currentGesture);
		currentLibrary.save();
	}
	
	private void handleEnabled(Button b) {
		if (b==nextButton) {
			if (currentChar==CHARACTERS.length()-1) {
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
		else if (b==backButton) {
			if (currentChar==0) {
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
            currentGesture=null;
        }

        public void onGesture(GestureOverlayView overlay, MotionEvent event) {}

        public void onGestureEnded(GestureOverlayView overlay, MotionEvent event) {
            currentGesture= overlay.getGesture();
            if (currentGesture.getLength() < LENGTH_THRESHOLD) {
                overlay.clear(false);
            }
            handleEnabled(nextButton);
        	handleEnabled(backButton);
        }

        public void onGestureCancelled(GestureOverlayView overlay, MotionEvent event) {
        }
    }
}
