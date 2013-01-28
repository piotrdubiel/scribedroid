package pl.scribedroid.input.dictionary;

import java.util.List;

import android.content.Context;

public abstract class Dictionary {
	protected Context context;
	public Dictionary(Context c) {
		context=c;
	}
	/**
	 * Dodaje podane słowo do słownik, jeśli nie istnieje lub aktualizuje kolumnę z częstotliwością występowania słowa.
	 * @param word
	 */
	public abstract void addWord(String word);
	/**
	 * Dla podanego prefiksu zwraca możliwe zakończenia słowa.
	 * @param prefix
	 * @return
	 */
	public abstract List<String> getSuggestions(String prefix);
	/**
	 * Określa, czy dane słowo znajduje się w słowniku.
	 * @param word
	 * @return
	 */
	public abstract boolean isValid(String word);
	public abstract void close();
}
