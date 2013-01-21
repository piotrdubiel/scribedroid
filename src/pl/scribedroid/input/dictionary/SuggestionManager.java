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
	private NativeDictionary native_dictionary;
	private UserDictionary user_dictionary;
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
				// db_dictionary = new DatabaseDictionary(context,
				// maxSuggestions);
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				ready = true;
			}
		}.execute();
	}

	public List<String> getSuggestions(String word) {
		Set<String> result = new LinkedHashSet<String>();
		result.add(word);

		List<String> nativeList = native_dictionary.getSuggestions(word);
		List<String> userList = user_dictionary.getSuggestions(word);
		// List<String> dbList = db_dictionary.getSuggestions(word);

		// while (!userList.isEmpty() && !dbList.isEmpty()) {
		// if (result.size() >= maxSuggestions)
		// break;
		// result.add(userList.get(0));
		// userList.remove(0);
		// if (result.size() >= maxSuggestions)
		// break;
		// result.add(dbList.get(0));
		// dbList.remove(0);
		// }
		//
		// if (result.size() < maxSuggestions) {
		// if (!userList.isEmpty()) {
		// result.addAll(userList);
		// }
		// if (!dbList.isEmpty()) {
		// result.addAll(dbList);
		// }
		// }

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

	public boolean isValid(String word) {
		// Log.v(TAG, "Database valid: " +
		// String.valueOf(db_dictionary.isValid(word)));
		Log.v(TAG, "Native valid: " + String.valueOf(native_dictionary.isValid(word)));
		Log.v(TAG, "User valid: " + String.valueOf(user_dictionary.isValid(word)));
		return native_dictionary.isValid(word) || user_dictionary.isValid(word);
	}

	public boolean isReady() {
		return ready;
	}

	public void addToUserDictionary(String word) {
		user_dictionary.addWord(word);
	}

	public void addToDictionary(String word) {
		native_dictionary.addWord(word);
	}

	public void close() {
		if (native_dictionary != null) native_dictionary.close();
	}
}
