package pl.scribedroid.input.dictionary;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DictionaryDbHelper extends SQLiteOpenHelper {
	public static final String TABLE_WORDS = "words";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_WORD = "word";
	public static final String COLUMN_FREQUENCY = "frequency";
	public static String[] ALL_COLUMNS = { COLUMN_ID, COLUMN_WORD, COLUMN_FREQUENCY };
	
	private static final String DATABASE_NAME = "dictionary.db";
	private static final int DATABASE_VERSION = 1;

	private static final String DATABASE_CREATE = "create table " + TABLE_WORDS
			+ "(" + COLUMN_ID + " integer primary key autoincrement, "
			+ COLUMN_WORD + " text not null, "
			+ COLUMN_FREQUENCY + " integer not null);";

	public DictionaryDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(DictionaryDbHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORDS);
		onCreate(db);
	}

}
