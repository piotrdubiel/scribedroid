package pl.scribedroid.input.dictionary;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class UserDictionary extends Dictionary {
	private static final String TAG = "UserDictionary";
	private static final Uri URI = android.provider.UserDictionary.Words.CONTENT_URI;
	private static final String[] COLUMNS = { android.provider.UserDictionary.Words.WORD };
	private static final String SELECTION = android.provider.UserDictionary.Words.WORD
			+ " LIKE ? AND (("
			+ android.provider.UserDictionary.Words.LOCALE
			+ " IS NULL) or ("
			+ android.provider.UserDictionary.Words.LOCALE
			+ "=?))";
	private static final String EXACT_SELECT = android.provider.UserDictionary.Words.WORD
			+ " = ? AND (("
			+ android.provider.UserDictionary.Words.LOCALE
			+ " IS NULL) or ("
			+ android.provider.UserDictionary.Words.LOCALE
			+ "=?))";

	private static final String ORDER = android.provider.UserDictionary.Words.DEFAULT_SORT_ORDER;

	private Locale locale;

	public UserDictionary(Context c, Locale l) {
		super(c);
		locale = l;
	}

	public UserDictionary(Context c) {
		super(c);
		locale = Locale.getDefault();
	}

	@Override
	public void addWord(String word) {
		Log.i(TAG, "Added " + word + " to user dictionary");
		android.provider.UserDictionary.Words.addWord(context, word, 128, android.provider.UserDictionary.Words.LOCALE_TYPE_CURRENT);
	}

	@Override
	public List<String> getSuggestions(String prefix) {
		Cursor suggestions = context.getContentResolver().query(URI, COLUMNS, SELECTION, new String[] {
				prefix + "%", locale.toString() }, ORDER);
		List<String> result = new ArrayList<String>();
		suggestions.moveToFirst();
		int column = suggestions.getColumnIndex(android.provider.UserDictionary.Words.WORD);
		while (!suggestions.isAfterLast()) {
			result.add(suggestions.getString(column));
			suggestions.moveToNext();
		}
		suggestions.close();
		return result;
	}

	@Override
	public boolean isValid(String word) {
		Cursor suggestions = context.getContentResolver().query(URI, COLUMNS, EXACT_SELECT, new String[] {
				word, locale.toString() }, null);
		return suggestions.getCount() > 0;
	}

	@Override
	public void close() {
	}
}
