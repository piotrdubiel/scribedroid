package pl.scribedroid.input.classificator;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import pl.scribedroid.input.Utils;
import android.util.Log;

public class ClassificationResult {
	ArrayList<Label> result;
	int type;

	public ClassificationResult(float[] in, int type) {
		result = new ArrayList<Label>();
		if (in != null) {
			// float denom = new Vector(in).sum();
			for (int i = 0; i < in.length; ++i)
				// result.add(new Label(Utils.decode(i, type), in[i] / denom));
				result.add(new Label(Utils.decode(i, type), in[i]));
		}
		this.type = type;
	}

	public ClassificationResult(ArrayList<Label> result, int type) {
		this.result = result;
		this.type = type;
		Collections.sort(this.result, new CharacterComparator());
	}

	public ClassificationResult(Label[] result, int type) {
		this.result = new ArrayList<Label>();
		for (Label l : result)
			this.result.add(l);
		this.type = type;
		Collections.sort(this.result, new CharacterComparator());
	}

	public ClassificationResult combine(ClassificationResult b) {
		ArrayList<Label> new_result = new ArrayList<Label>(result.size());
		for (Label l : result)
			new_result.add(new Label(l.label, l.belief));
		Map<Character, Float> b_map = b.getLabelsAsMap();
		for (Label l : new_result) {
			if (b_map.containsKey(l.label)) l.belief *= b_map.get(l.label);
			else l.belief *= l.belief;

		}

		return new ClassificationResult(new_result, type | b.type);
	}
	
	public ClassificationResult filter(float threshold) {
		for (Iterator<Label> it = result.iterator(); it.hasNext(); ) {
		    Label l = it.next();
		    if (l.belief < threshold) {
		        it.remove();
		    }
		}
		return this;
	}

	public ClassificationResult pairsWith(ClassificationResult b) {
		ArrayList<Label> pairs = new ArrayList<ClassificationResult.Label>();
		Collections.sort(result, new LabelComparator());
		Collections.sort(b.result, new LabelComparator());

		List<Label> candidatesA = result.subList(0, Math.min(5, result.size()));
		List<Label> candidatesB = b.result.subList(0, Math.min(5, b.result.size()));

		Log.d("Best A: ", Arrays.toString(getLabels(5)));
		Log.d("Best B: ", Arrays.toString(b.getLabels(5)));

		for (Label lA : candidatesA) {
			for (Label lB : candidatesB) {
				Log.d("Is pair? ", lA.label.toString() + " " + lB.label.toString()+ " "+String.valueOf(lA.label.equals(lB.label)));
				if (lA.label.equals(lB.label)) {
					pairs.add(new Label(lA.label, lA.belief * lB.belief));
				}
			}
		}

		for (Label l : pairs)
			Log.d("Found pair: ", l.label.toString() + " " + l.belief);

		return new ClassificationResult(pairs, type & b.type);
	}

	public Character[] getLabels() {
		return getLabels(result.size());
	}

	public Character[] getLabels(int limit) {
		Collections.sort(result, new LabelComparator());
		Character[] chars = new Character[Math.min(limit, result.size())];

		for (int i = 0; i < Math.min(limit, result.size()); ++i)
			chars[i] = result.get(i).label;

		return chars;
	}

	public Label[] getLabelsWithBelief() {
		return getLabelsWithBelief(result.size());
	}

	public Label[] getLabelsWithBelief(int limit) {
		Collections.sort(result, new LabelComparator());
		return (Label[]) result.subList(0, Math.min(limit, result.size())).toArray(new Label[Math.min(limit, result.size())]);
	}

	public Map<Character, Float> getLabelsAsMap() {
		Map<Character, Float> map = new LinkedHashMap<Character, Float>();
		for (Label l : result)
			map.put(l.label, l.belief);
		return map;
	}
	
	public boolean isEmpty() {
		return result.isEmpty();
	}

	public static class Label {
		public Character label;
		public float belief;

		public Label(Character c, float b) {
			label = c;
			belief = b;
		}
	}

	public static class LabelComparator implements Comparator<Label> {
		@Override
		public int compare(Label a, Label b) {
			return a.belief > b.belief ? -1 : a.belief < b.belief ? 1 : 0;
		}

	}

	static class CharacterComparator implements Comparator<Label> {
		private Collator c = Collator.getInstance(new Locale("pl", "PL"));

		@Override
		public int compare(Label a, Label b) {
			if (Character.isUpperCase(a.label) && Character.isLowerCase(b.label)) return -1;
			else if (Character.isLowerCase(a.label) && Character.isUpperCase(b.label)) return 1;
			else return c.compare(a.label.toString(), b.label.toString());
		}

	}
}
