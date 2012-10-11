package pl.scribedroid.input.dictionary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pl.scribedroid.R;
import android.content.Context;
import android.util.Log;

public class NativeDictionary extends Dictionary {
	private static final String TAG = "NativeDictionary";
    static {
    	System.loadLibrary("dictionary");
    }
    
    private native int createDictionary(String filename);
//    private native int openDictionary(String filename);
//    private native void closeDictionary(int dictionary);
    private native String[] suggest(int dict,String prefix,int limit);
    private native boolean isValid(int dict,String word);
    
    private int dictionary;
    private int maxSuggestions;
    
    public NativeDictionary(Context c,int limit) {
    	super(c);
    	dictionary=createDictionary("/sdcard/pl_small.txt");
    	maxSuggestions = limit;
    }
    
	@Override
	public void addWord(String word) {}

	@Override
	public List<String> getSuggestions(String prefix) {
		Log.d(TAG, "Suggest");
		String[] tmpSuggestions=suggest(dictionary, prefix,maxSuggestions);

		if (tmpSuggestions != null)
			return new ArrayList<String>(Arrays.asList(tmpSuggestions));
		else
			return new ArrayList<String>();
	}
	
	@Override
	public boolean isValid(String word) {
		return isValid(dictionary,word);
	}
	
}
