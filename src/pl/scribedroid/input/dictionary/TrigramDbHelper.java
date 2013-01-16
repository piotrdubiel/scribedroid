package pl.scribedroid.input.dictionary;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class TrigramDbHelper extends SQLiteOpenHelper {
	public static final String TABLE_GRAMS = "grams";
	public static final String COLUMN_GRAM = "gram";
	public static final String COLUMN_FREQUENCY = "frequency";
	public static String[] ALL_COLUMNS = { COLUMN_GRAM, COLUMN_FREQUENCY };

	private final static String DATABASE_PATH = "/data/data/pl.scribedroid/databases/";

	private static final String DATABASE_NAME = "trigrams.db";
	private static final int DATABASE_VERSION = 1;

	private static final String DATABASE_CREATE = "create table if not exists " + TABLE_GRAMS
			+ " (" + COLUMN_GRAM + " text not null, " + COLUMN_FREQUENCY
			+ " integer not null);";

	private Context context;

	public TrigramDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(TrigramDbHelper.class.getName(), "Upgrading database from version "
				+ oldVersion
				+ " to "
				+ newVersion
				+ ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_GRAMS);
		onCreate(db);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.database.sqlite.SQLiteOpenHelper#getReadableDatabase()
	 */
	@Override
	public synchronized SQLiteDatabase getReadableDatabase() {
		File db_file = new File(DATABASE_PATH + DATABASE_NAME);
		if (!db_file.exists()) {
			copyDatabase(db_file);
		}
		return super.getReadableDatabase();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.database.sqlite.SQLiteOpenHelper#getWritableDatabase()
	 */
	@Override
	public synchronized SQLiteDatabase getWritableDatabase() {
		Log.v("Database", "Get writable database");
		File db_file = new File(DATABASE_PATH + DATABASE_NAME);
		Log.v("Database", "DB file: " + db_file.getPath());
		if (!db_file.exists()) {
			copyDatabase(db_file);
		}
		return super.getWritableDatabase();
	}

	private void copyDatabase(File db_file) {
		Log.v("Database", " COPYING TRIGRAMS FROM ASSETS...");
		try {
			InputStream is = context.getAssets().open(DATABASE_NAME);
			OutputStream os = new FileOutputStream(db_file);

			byte[] buffer = new byte[1024];
			while (is.read(buffer) > 0) {
				os.write(buffer);
			}

			os.flush();
			os.close();
			is.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

	}
}
