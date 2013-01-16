package pl.scribedroid.input.keyboard;

import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.Keyboard.Key;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;

public class StandardKeyboardViewDepracted extends KeyboardView {
	private InputConnection inputConnection;	

	public StandardKeyboardViewDepracted(Context context, AttributeSet attrs, InputConnection in) {
		super(context, attrs);
		inputConnection = in;
		setOnKeyboardActionListener(new StandardKeyboardActionListener());
	}
    
    private class StandardKeyboardActionListener implements OnKeyboardActionListener {
		public void onKey(int primaryCode, int[] arg1) {
			Log.i("Keyboard", "Key: "+primaryCode+" "+String.valueOf(primaryCode==KeyEvent.KEYCODE_Q));
			Log.i("Keyboard",String.valueOf((char) primaryCode));
			inputConnection.commitText(String.valueOf((char) primaryCode), 1);
		}

		public void onPress(int primaryCode) {
			// TODO Auto-generated method stub
			
		}

		public void onRelease(int primaryCode) {
			// TODO Auto-generated method stub
			
		}

		public void onText(CharSequence text) {
			Log.i("Keyboard",text.toString());
			
		}

		public void swipeDown() {
			// TODO Auto-generated method stub
			
		}

		public void swipeLeft() {
			// TODO Auto-generated method stub
			
		}

		public void swipeRight() {
			// TODO Auto-generated method stub
			
		}

		public void swipeUp() {
			// TODO Auto-generated method stub
			
		}
    	
    }
}
