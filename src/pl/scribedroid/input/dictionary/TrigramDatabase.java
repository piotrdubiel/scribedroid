package pl.scribedroid.input.dictionary;

import java.util.ArrayList;

import pl.scribedroid.input.classificator.ClassificationResult.Label;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Klasa umożliwia dostęp do listy trigramów zapisanych w bazie danych.
 * 
 * <p>
 * See also the following files:
 * <ul>
 * <li>TrigramDbHelper.java</li>
 * </ul>
 */
public class TrigramDatabase {
	private static final String TAG = "TrigramDatabase";

	private TrigramDbHelper dbHelper;
	private SQLiteDatabase database;

	public TrigramDatabase(Context c) {
		dbHelper = new TrigramDbHelper(c);
		database = dbHelper.getWritableDatabase();
	}

	public void addTrigram(String trigram) {
		Cursor cursor = database.query(TrigramDbHelper.TABLE_GRAMS, TrigramDbHelper.ALL_COLUMNS, TrigramDbHelper.COLUMN_GRAM + "=?", new String[] { trigram }, null, null, null);
		Log.v("Add Word", "WORD: " + trigram);
		if (cursor.getCount() == 0) {
			Log.v("Add trigram", "No trigram found");
			ContentValues values = new ContentValues();
			values.put(TrigramDbHelper.COLUMN_GRAM, trigram);
			values.put(TrigramDbHelper.COLUMN_FREQUENCY, 0);
			database.insert(TrigramDbHelper.TABLE_GRAMS, null, values);
		}
		else {
			cursor.moveToFirst();
			ContentValues values = new ContentValues();
			int freq = cursor.getInt(cursor.getColumnIndexOrThrow(TrigramDbHelper.COLUMN_FREQUENCY));
			values.put(TrigramDbHelper.COLUMN_FREQUENCY, freq + 1);
			values.put(TrigramDbHelper.COLUMN_GRAM, trigram);

			Log.v("Add trigram", "Trigram exists Freq: " + freq);
			database.update(TrigramDbHelper.TABLE_GRAMS, values, TrigramDbHelper.COLUMN_GRAM + "=?", new String[] { trigram });
		}
		cursor.close();
	}

	public ArrayList<Label> getSuggestions(String prefix) {
		Log.d(TAG, "Suggest");
		int denom = computeNormalizer();
		Cursor cursor = database.query(TrigramDbHelper.TABLE_GRAMS, TrigramDbHelper.ALL_COLUMNS, TrigramDbHelper.COLUMN_GRAM + " LIKE ?", new String[] { prefix + "%" }, null, null, DictionaryDbHelper.COLUMN_FREQUENCY + " DESC");
		ArrayList<Label> result = new ArrayList<Label>();
		if (cursor.getCount() > 0) {
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
				float p = ((float) cursor.getInt(cursor.getColumnIndex(TrigramDbHelper.COLUMN_FREQUENCY))) / denom;
				result.add(new Label(cursor.getString(cursor.getColumnIndex(TrigramDbHelper.COLUMN_GRAM)).charAt(2), p));
			}
		}
		cursor.close();
		return result;
	}

	public void close() {
		database.close();
	}

	private int computeNormalizer() {
		Cursor cursor = database.rawQuery("SELECT SUM(" + TrigramDbHelper.COLUMN_FREQUENCY + ") FROM" + TrigramDbHelper.TABLE_GRAMS, null);
		if (cursor.getCount() == 1) return cursor.getInt(0);
		return 0;
	}
}
