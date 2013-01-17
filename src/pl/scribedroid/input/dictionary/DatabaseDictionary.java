package pl.scribedroid.input.dictionary;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Klasa umożliwia dostęp do słownika zapisanego w bazie danych SQLite w pamięci wewnętrznej.
 *
 * <p>See also the following files:
 * <ul>
 *   <li>Dictionary.java</li>
 * </ul>
 */
public class DatabaseDictionary extends Dictionary {
	private static final String TAG = "DatabaseDictionary";

	private int maxSuggestions;

	private DictionaryDbHelper dbHelper;
	private SQLiteDatabase database;

	public DatabaseDictionary(Context c, int limit) {
		super(c);
		maxSuggestions = limit;
		dbHelper = new DictionaryDbHelper(c);
		try {
		database = dbHelper.getWritableDatabase();
		Cursor cursor = database.rawQuery("select count(*) from words", null);
		cursor.moveToFirst();
		Log.d(TAG, "Words in database: " + cursor.getInt(0));
		cursor.close();
		}
		catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
	}

	@Override
	public void addWord(String word) {
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
	}

	@Override
	public List<String> getSuggestions(String prefix) {
		Log.d(TAG, "Suggest");
		Cursor cursor = database.query(DictionaryDbHelper.TABLE_WORDS, new String[] { DictionaryDbHelper.COLUMN_WORD }, DictionaryDbHelper.COLUMN_WORD
				+ " LIKE ?", new String[] { prefix + "%" }, null, null, DictionaryDbHelper.COLUMN_FREQUENCY + " DESC", String.valueOf(maxSuggestions));
		List<String> result = new ArrayList<String>();
		if (cursor.getCount() > 0) {
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
				result.add(cursor.getString(0));
			}			
		}
		cursor.close();
		return result;
	}

	@Override
	public boolean isValid(String word) {
		Cursor cursor = database.query(DictionaryDbHelper.TABLE_WORDS, new String[] { DictionaryDbHelper.COLUMN_WORD }, DictionaryDbHelper.COLUMN_WORD
				+ "=?", new String[] { word }, null, null, null);
		int count = cursor.getCount();
		cursor.close();
		return count > 0;
	}

	public void getWords() {
		Cursor cursor = database.query(DictionaryDbHelper.TABLE_WORDS, DictionaryDbHelper.ALL_COLUMNS, null, null, null, null, null);
		Log.v("Get Words", "Found " + cursor.getCount() + " words");
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Log.i(TAG, cursor.getString(cursor.getColumnIndex(DictionaryDbHelper.COLUMN_WORD)));
			cursor.moveToNext();
		}
	}

	@Override
	public void close() {
		database.close();
	}

}
