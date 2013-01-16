package pl.scribedroid.input;

import pl.scribedroid.R;
import pl.scribedroid.input.keyboard.StandardKeyboard;
import roboguice.inject.InjectResource;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.Log;
import android.widget.Toast;

public class KeyboardInputMethod extends InputMethodController implements
		KeyboardView.OnKeyboardActionListener {
	private static final String TAG = "KeyboardInputMethod";
	KeyboardView keyboard_view;

	Keyboard alpha_keyboard;
	Keyboard symbols_keyboard;
	Keyboard symbols_shift_keyboard;	

	private static final int NORMAL 	= 0;
	private static final int SHIFTED 	= 1;
	private static final int CAPSLOCK 	= 2;
	
	private int shift_state = NORMAL;

	@InjectResource(R.string.word_separators)
	String word_separators;
	

	public KeyboardInputMethod(ScribeDroid s) {
		super(s, R.layout.standard_keyboard);
		keyboard_view = (KeyboardView) inputView.findViewById(R.id.keyboard);

		alpha_keyboard = new StandardKeyboard(service, R.xml.qwerty_keyboard);
		symbols_keyboard = new StandardKeyboard(service, R.xml.symbols_keyboard);
		symbols_shift_keyboard = new StandardKeyboard(service, R.xml.symbols_shift_keyboard);

		keyboard_view.setKeyboard(alpha_keyboard);
		keyboard_view.setOnKeyboardActionListener(this);
	}

	@Override
	public void resetModifiers() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onKey(int primaryCode, int[] keyCodes) {		
		if (primaryCode == Keyboard.KEYCODE_DELETE) {
			service.delete();
		}
		else if (primaryCode == Keyboard.KEYCODE_SHIFT) {
			handleShift();
		}
		else if (primaryCode == Keyboard.KEYCODE_CANCEL) {
			Log.i(TAG, "Input switch requested");
			service.switchInputMethod();
			return;
		}
		else if (primaryCode == Keyboard.KEYCODE_MODE_CHANGE && inputView != null) {
			Keyboard current = ((KeyboardView) inputView).getKeyboard();
			if (current == symbols_keyboard || current == symbols_shift_keyboard) current = alpha_keyboard;
			else current = symbols_keyboard;

			((KeyboardView) inputView).setKeyboard(current);
			if (current == symbols_keyboard) {
				current.setShifted(false);
			}
		}
		else {
			Log.v(TAG, "OnKey: " + primaryCode+ " " + (char) primaryCode);
			service.enterCharacter((char) primaryCode);
			// handleCharacter(primaryCode, keyCodes);
		}
	}

	private void handleShift() {
		if (inputView == null) {
			return;
		}

		Keyboard currentKeyboard = ((KeyboardView) inputView).getKeyboard();
		if (currentKeyboard == alpha_keyboard) {
			if (shift_state == NORMAL) {
				shift_state = SHIFTED;
			}
			else if (shift_state == SHIFTED) {
				shift_state = CAPSLOCK;
				Toast.makeText(service, "CapsLock", Toast.LENGTH_SHORT).show();
			}
			else if (shift_state == CAPSLOCK) {
				shift_state = NORMAL;
			}
			((KeyboardView) inputView).setShifted(shift_state == CAPSLOCK || !((KeyboardView) inputView).isShifted());
		}
		else if (currentKeyboard == symbols_keyboard) {
			symbols_keyboard.setShifted(true);
			((KeyboardView) inputView).setKeyboard(symbols_shift_keyboard);
			symbols_shift_keyboard.setShifted(true);
		}
		else if (currentKeyboard == symbols_shift_keyboard) {
			symbols_shift_keyboard.setShifted(false);
			((KeyboardView) inputView).setKeyboard(symbols_keyboard);
			symbols_keyboard.setShifted(false);
		}
	}
	
	@Override
	public void onPress(int primaryCode) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onRelease(int primaryCode) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onText(CharSequence text) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void swipeDown() {
		service.switchInputMethod();
	}

	@Override
	public void swipeLeft() {
		Log.d(TAG, "Left");
	}

	@Override
	public void swipeRight() {
		Log.d(TAG, "Right");
	}

	@Override
	public void swipeUp() {
		Log.d(TAG, "Up");
	}

}
