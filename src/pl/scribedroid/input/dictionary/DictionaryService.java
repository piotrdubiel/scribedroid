package pl.scribedroid.input.dictionary;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import pl.scribedroid.R;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

public class DictionaryService extends IntentService {
	private static final String TAG = "DictionaryService";
	private static final int NOTIFICATION_ID = 20060;

	private NotificationManager notification_manager;
	private NativeDictionary native_dictionary; 

	public DictionaryService() {
		super("DictionaryService");
	}

	/* (non-Javadoc)
	 * @see android.app.IntentService#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		notification_manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		native_dictionary = new NativeDictionary(this, 0);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String filename = intent.getStringExtra("filename");
		Log.d(TAG, "Filename: " + filename);
		try {
			InputStreamReader in = new InputStreamReader(new FileInputStream(filename));
			LineNumberReader lnr = new LineNumberReader(in);
			lnr.skip(Long.MAX_VALUE);
			int line_count = lnr.getLineNumber();
			Log.d(TAG, "Line count: " + line_count);
			lnr.close();
			
			in = new InputStreamReader(new FileInputStream(filename));
			BufferedReader reader = new BufferedReader(in);

			notify(0, line_count);

			String line = null;
			int i = 0;
			while ((line = reader.readLine()) != null) {
				String[] words = line.split(" ");
				i++;

				for (String word : words) {
					native_dictionary.addWord(word);
				}

				if (i % 50 == 0) {
					notify(i, line_count);
				}
			}
			reader.close();
			notification_manager.cancel(NOTIFICATION_ID);
		}
		catch (FileNotFoundException e) {
			// Log.e(TAG, e.getMessage());
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			// Log.e(TAG, e.getMessage());
			// e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see android.app.IntentService#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		native_dictionary.close();
		notification_manager.cancelAll();
	}

	private void notify(int progress, int max) {
		Intent notification_intent = new Intent(this, DictionaryService.class);

		Notification notification = new Notification(R.drawable.sym_notification, getString(R.string.dict_notification_title_text), System.currentTimeMillis());

		RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.dictionary_notification);
		contentView.setProgressBar(R.id.progress_bar, max, progress, false);
		contentView.setTextViewText(R.id.progress_text, String.valueOf(progress
				* 100 / max)
				+ "%");
		notification.flags |= Notification.FLAG_ONGOING_EVENT;

		notification.contentView = contentView;

		PendingIntent content_intent = PendingIntent.getService(this, 0, notification_intent, 0);
		notification.contentIntent = content_intent;

		notification_manager.notify(NOTIFICATION_ID, notification);
	}
}
