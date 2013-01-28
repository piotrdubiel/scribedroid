package pl.scribedroid.input;

import java.util.Arrays;
import java.util.List;

import pl.scribedroid.R;
import pl.scribedroid.input.keyboard.StandardKeyboard;
import roboguice.inject.InjectResource;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
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

	private static final int NORMAL = 0;
	private static final int SHIFTED = 1;
	private static final int CAPSLOCK = 2;

	private int shift_state = NORMAL;

	private int last_keycode = 0;
	private int current_keycode = 0;
	
	// TODO
	@SuppressWarnings("unused")
	private boolean postponed_reset = false;

	@InjectResource(R.string.word_separators)
	String word_separators;

	/**
	 * Konstruktor inicjuje elementy widoku i ładuje klawiatury (znakową, symboli i symboli z wciśniętym klawiszem Shift).
	 * Wymaga podania klasy ScribeDroid, z którą będzie powiązany.
	 * @param s
	 */
	public KeyboardInputMethod(ScribeDroid s) {
		super(s, R.layout.standard_keyboard);
		keyboard_view = (KeyboardView) inputView.findViewById(R.id.keyboard);

		alpha_keyboard = new StandardKeyboard(service, R.xml.qwerty_keyboard);
		symbols_keyboard = new StandardKeyboard(service, R.xml.symbols_keyboard);
		symbols_shift_keyboard = new StandardKeyboard(service, R.xml.symbols_shift_keyboard);

		keyboard_view.setKeyboard(alpha_keyboard);
		keyboard_view.setOnKeyboardActionListener(this);
	}

	/**
	 * Resetuje stan klawisza Shift.
	 * @see pl.scribedroid.input.InputMethodController#resetModifiers()
	 */
	@Override
	public void resetModifiers() {
		Log.d(TAG, "Last keycode: " + last_keycode + " Current keycode: " + current_keycode+ " " +isCodeComplex(current_keycode));
		if (shift_state == SHIFTED) {
//			if (isCodeComplex(current_keycode)) {
//				postponed_reset = true;
//				return;
//			}
			
//			if (postponed_reset && current_keycode == Keyboard.KEYCODE_DELETE) {}
//			
				shift_state = NORMAL;
				((KeyboardView) inputView).setShifted(false);
//			}
//			else if (!postponed_reset)
		}
	}

	/**
	 * Obsługuje zdarzenie naciśnięcia na przycisk. 
	 * Inne, możliwe klawisze w zmiennej keyCodes nie są brane pod uwagę
	 * @see android.inputmethodservice.KeyboardView.OnKeyboardActionListener#onKey(int, int[])
	 */
	@Override
	public void onKey(int primaryCode, int[] keyCodes) {
		Log.v(TAG, "OnKey: " + primaryCode + " " + (char) primaryCode);
		last_keycode = current_keycode;
		current_keycode = primaryCode;
		Log.d(TAG, "Last keycode: " + last_keycode + " Current keycode: " + current_keycode);
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
			Log.v(TAG, "OnKey: " + primaryCode + " " + (char) primaryCode);
			if (shift_state == SHIFTED || shift_state == CAPSLOCK) service.enterCharacter(Character.toUpperCase((char) primaryCode));
			else service.enterCharacter((char) primaryCode);

			resetModifiers();
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
	}

	@Override
	public void onRelease(int primaryCode) {
	}

	@Override
	public void onText(CharSequence text) {
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
		//handleShift();
		Log.d(TAG, "Up");
	}

	boolean isCodeComplex(int keycode) {
		Keyboard current_keyboard = ((KeyboardView) inputView).getKeyboard();
		List<Key> keys = current_keyboard.getKeys();

		for (Key k : keys) {
			if (Arrays.binarySearch(k.codes, keycode) >= 0 && k.codes.length > 1) return true;
		}

		return false;
	}
	
	boolean isCodeInOneKey(int keycode_a, int keycode_b) {
		Keyboard current_keyboard = ((KeyboardView) inputView).getKeyboard();
		List<Key> keys = current_keyboard.getKeys();

		for (Key k : keys) {
			if (Arrays.binarySearch(k.codes, keycode_a) >= 0 &&Arrays.binarySearch(k.codes, keycode_b) >= 0) return true;
		}

		return false;
	}
}
