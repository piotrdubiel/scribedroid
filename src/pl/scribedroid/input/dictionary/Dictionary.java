package pl.scribedroid.input.dictionary;

import java.util.List;

import android.content.Context;

public abstract class Dictionary {
	protected Context context;
	public Dictionary(Context c) {
		context=c;
	}
//	public abstract void open();
//	public abstract void close();
	public abstract void addWord(String word);
	public abstract List<String> getSuggestions(String prefix);
	public abstract boolean isValid(String word);
}
