package pl.scribedroid.input.classificator;

import java.util.Comparator;
import java.util.List;
import android.util.Pair;

public interface ClassificationResult {
	Pair<Character,Float> getBestResult();
	Pair<Character,Float> getResult(int index);
	List<Pair<Character,Float>> getBestResults(int max);
	
	public class ResultComparator implements Comparator<Pair<Character,Float>> {
		@Override
		public int compare(Pair<Character, Float> a, Pair<Character, Float> b) {
			return a.second < b.second ? -1 : a.second > b.second ? 1 : 0;
		}
	}
}
