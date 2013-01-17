package pl.scribedroid.input.classificator;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import pl.scribedroid.input.Utils;

public class ClassificationResult {
	ArrayList<Label> result;
	int type;

	public ClassificationResult(float[] in, int type) {
		result = new ArrayList<Label>();
		if (in != null) {
			for (int i = 0; i < in.length; ++i)
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
			new_result.add(new Label(l.label,l.belief));
		Map<Character, Float> b_map = b.getLabelsAsMap();
		float denom = 0.0f;
		for (Label l : new_result) {
			if (b_map.containsKey(l.label))
				l.belief *= b_map.get(l.label);
			else
				l.belief *= l.belief;
//			if (b_map.containsKey(l.label))
//				l.belief += b_map.get(l.label);
//			else
//				l.belief *= 2;
//			denom += l.belief;
		}

//		if (denom != 0.0f)
//		for (Label l : new_result)
//			l.belief /= denom;
		
		return new ClassificationResult(new_result, type | b.type);
	}

	public Character[] getLabels() {
		return getLabels(result.size());
	}
	
	public Character[] getLabels(int limit) {
		Collections.sort(result, new LabelComparator());
		Character[] chars = new Character[result.size()];

		for (int i = 0; i < result.size() && i < limit; ++i)
			chars[i] = result.get(i).label;

		return chars;
	}

	public Label[] getLabelsWithBelief() {
		return getLabelsWithBelief(result.size());
	}
	
	public Label[] getLabelsWithBelief(int limit) {
		Collections.sort(result, new LabelComparator());
		return (Label[]) result.subList(0, limit).toArray(new Label[result.size()]);
	}

	public Map<Character, Float> getLabelsAsMap() {
		Map<Character, Float> map = new LinkedHashMap<Character, Float>();
		for (Label l : result)
			map.put(l.label, l.belief);
		return map;
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
