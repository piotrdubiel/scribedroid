package pl.scribedroid.input.dictionary;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class SuggestionManager {
	private static final String TAG = "SuggestionManager";
	private Dictionary native_dictionary;
	private Dictionary user_dictionary;
	// private DatabaseDictionary db_dictionary;
	private Context context;
	private int maxSuggestions = 10;
	private boolean ready;

	public SuggestionManager(Context c) {
		ready = false;
		context = c;
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				native_dictionary = new NativeDictionary(context, maxSuggestions);
				user_dictionary = new UserDictionary(context);
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				ready = true;
			}
		}.execute();
	}

	/**
	 * Przeszukuje UserDictionary i NativeDictionary pod kątem sugestii dla podanego w argumencie słowa, 
	 * a następnie zwraca listę sugestii
	 * @param word
	 * @return
	 */
	public List<String> getSuggestions(String word) {
		Set<String> result = new LinkedHashSet<String>();
		result.add(word);

		List<String> nativeList = native_dictionary.getSuggestions(word);
		List<String> userList = user_dictionary.getSuggestions(word);

		while (!userList.isEmpty() && !nativeList.isEmpty()) {
			if (result.size() >= maxSuggestions) break;
			result.add(userList.get(0));
			userList.remove(0);
			if (result.size() >= maxSuggestions) break;
			result.add(nativeList.get(0));
			nativeList.remove(0);
		}

		if (result.size() < maxSuggestions) {
			if (!userList.isEmpty()) {
				result.addAll(userList);
			}
			if (!nativeList.isEmpty()) {
				result.addAll(nativeList);
			}
		}

		return new ArrayList<String>(result);
	}

	/**
	 * Zwraca true, jeśli podane słowo istnieje w słownik użytkownika lub w wbudowanym słowniku
	 * @param word
	 * @return
	 */
	public boolean isValid(String word) {
		Log.v(TAG, "Native valid: " + String.valueOf(native_dictionary.isValid(word)));
		Log.v(TAG, "User valid: " + String.valueOf(user_dictionary.isValid(word)));
		return native_dictionary.isValid(word) || user_dictionary.isValid(word);
	}

	/**
	 * Zwraca true, jeśli podane słowo istnieje w słownik użytkownika lub w wbudowanym słowniku
	 * @return
	 */
	public boolean isReady() {
		return ready;
	}

	/**
	 * Dodaje podane słowo do słownika  UserDictionary
	 * @param word
	 */
	public void addToUserDictionary(String word) {
		user_dictionary.addWord(word);
	}

	/**
	 * Dodaje podane słowo do słownika   NativeDictionary
	 * @param word
	 */
	public void addToDictionary(String word) {
		native_dictionary.addWord(word);
	}

	/**
	 * Zamyka słownik NativeDictionary (słownik UserDictionary nie wymaga zamknięcia)
	 */
	public void close() {
		if (native_dictionary != null) native_dictionary.close();
	}
}
