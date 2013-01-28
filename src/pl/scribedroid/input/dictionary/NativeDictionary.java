package pl.scribedroid.input.dictionary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class NativeDictionary extends Dictionary {
	private static final String TAG = "NativeDictionary";
	static {
		System.loadLibrary("dictionary");
	}

	public int freq_limit = 3;

	/**
	 * Tworzy drzewo Trie z wyrazów w bazie danych zapisanych w pliku o nazwie filename i ustala dolnym limit częstotliwości powtórzenia się słowa, aby odfiltrować słowa spotykane bardzo rzadko.
	 * Zwraca wskaźnik na obiekt jako wartość int.
	 * @param filename
	 * @param freq_limit
	 * @return
	 */
	private native int createDictionary(String filename, int freq_limit);

	/**
	 * Zwraca listę słów mogących być zakończeniem podanego prefiksu.
	 * @param dict
	 * @param prefix
	 * @param limit
	 * @return
	 */
	private native String[] suggest(int dict, String prefix, int limit);

	/**
	 * Zwraca prawdę jeśli słowo występuje w słowniku.
	 * @param dict
	 * @param word
	 * @return
	 */
	private native boolean isValid(int dict, String word);

	private int dictionary;
	private int maxSuggestions;
	private DictionaryDbHelper dbHelper;
	private SQLiteDatabase database;

	public NativeDictionary(Context c, int limit) {
		super(c);
		maxSuggestions = limit;
		dbHelper = new DictionaryDbHelper(c);
		database = dbHelper.getWritableDatabase();
		String db_filename = database.getPath();
		dictionary = createDictionary(db_filename, freq_limit);
	}

	@Override
	public void addWord(String word) {
		// database = dbHelper.getWritableDatabase();
		Cursor cursor = database.query(DictionaryDbHelper.TABLE_WORDS, DictionaryDbHelper.ALL_COLUMNS, DictionaryDbHelper.COLUMN_WORD
				+ "=?", new String[] { word }, null, null, null);
		Log.v("Add Word", "WORD: " + word);
		if (cursor.getCount() == 0) {
			Log.v("Add Word", "No word found");
			ContentValues values = new ContentValues();
			values.put(DictionaryDbHelper.COLUMN_WORD, word);
			values.put(DictionaryDbHelper.COLUMN_FREQUENCY, 0);
			database.insert(DictionaryDbHelper.TABLE_WORDS, null, values);
		}
		else {
			cursor.moveToFirst();
			ContentValues values = new ContentValues();
			int freq = cursor.getInt(cursor.getColumnIndexOrThrow(DictionaryDbHelper.COLUMN_FREQUENCY));
			values.put(DictionaryDbHelper.COLUMN_FREQUENCY, freq + 1);
			values.put(DictionaryDbHelper.COLUMN_WORD, word);

			Log.v("Add Word", "Word exists Freq: " + freq);
			database.update(DictionaryDbHelper.TABLE_WORDS, values, DictionaryDbHelper.COLUMN_WORD
					+ "=?", new String[] { word });
		}
		cursor.close();
		// database.close();
	}

	@Override
	public List<String> getSuggestions(String prefix) {
		Log.d(TAG, "Suggest");
		String[] tmpSuggestions = suggest(dictionary, prefix, maxSuggestions);

		if (tmpSuggestions != null) return new ArrayList<String>(Arrays.asList(tmpSuggestions));
		else return new ArrayList<String>();
	}

	@Override
	public boolean isValid(String word) {
		return isValid(dictionary, word);
	}

	public void getWords() {
		// database = dbHelper.getReadableDatabase();
		Cursor cursor = database.query(DictionaryDbHelper.TABLE_WORDS, DictionaryDbHelper.ALL_COLUMNS, null, null, null, null, null);
		Log.v("Get Words", "Found " + cursor.getCount() + " words");
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Log.i(TAG, cursor.getString(cursor.getColumnIndex(DictionaryDbHelper.COLUMN_WORD)));
			cursor.moveToNext();
		}
		// database.close();
	}

	@Override
	public void close() {
		database.close();
	}

}
