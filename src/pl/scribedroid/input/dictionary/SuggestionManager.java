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
	private NativeDictionary nativeDict;
	private UserDictionary userDict;
	private Context context;
	private int maxSuggestions = 10;
	private boolean ready;
	
	public SuggestionManager(Context c) {
		ready=false;
		context=c;
		new AsyncTask<Void,Void,Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				nativeDict = new NativeDictionary(context, maxSuggestions);
				userDict = new UserDictionary(context);
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				ready=true;
			}			
		}.execute();
	}
	
	public List<String> getSuggestions(String word) {
		Set<String> result = new LinkedHashSet<String>();
		result.add(word);
		
		List<String> nativeList = nativeDict.getSuggestions(word);
		List<String> userList = userDict.getSuggestions(word);
		
		while (!userList.isEmpty() && !nativeList.isEmpty()) {
			if (result.size()>=maxSuggestions) break;
			result.add(userList.get(0));
			userList.remove(0);
			if (result.size()>=maxSuggestions) break;
			result.add(nativeList.get(0));
			nativeList.remove(0);
		}
		
		if (result.size()<maxSuggestions) {
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
		Log.v(TAG, "Native valid: "+String.valueOf(nativeDict.isValid(word)));
		Log.v(TAG, "User valid: "+String.valueOf(userDict.isValid(word)));
		return nativeDict.isValid(word) || userDict.isValid(word);
	}
	
	public boolean isReady() {
		return ready;
	}
	
	public void addToUserDictionary(String word) {
		userDict.addWord(word);
	}
}
