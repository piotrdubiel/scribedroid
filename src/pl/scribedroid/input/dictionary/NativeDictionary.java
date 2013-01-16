package pl.scribedroid.input.dictionary;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
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

	private native int createDictionary(String filename, int freq_limit);

	private native String[] suggest(int dict, String prefix, int limit);

	private native boolean isValid(int dict, String word);

	private int dictionary;
	private int maxSuggestions;
	private DictionaryDbHelper dbHelper;
	private SQLiteDatabase database;

	public NativeDictionary(Context c, int limit, String filename) {
		super(c);
		maxSuggestions = limit;
		dbHelper = new DictionaryDbHelper(c);
		database = dbHelper.getWritableDatabase();
		String db_filename = database.getPath();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
			String word = null;
			while ((word = reader.readLine()) != null) {
				addWord(word);
			}
		}
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		dictionary = createDictionary(db_filename, freq_limit);
	}

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
