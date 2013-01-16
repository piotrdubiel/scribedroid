package pl.scribedroid.input.classificator;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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
	}

	public ClassificationResult combine(ClassificationResult r) throws Exception {
		// if (type == r.type) {
		// if (result.size() != r.result.size()) throw new
		// Exception("Result size mismatch");
		// Collections.sort(result, new CharacterComparator());
		// Collections.sort(r.result, new CharacterComparator());
		// float[] y = new float[result.size()];
		// for (int i = 0; i < result.size(); ++i) {
		// y[i] = result.get(i).belief * r.result.get(i).belief;
		// }
		// return new ClassificationResult(y, type);
		// }
		// else if ((type & r.type) == 0) {
		// ArrayList<Label> new_result = new ArrayList<Label>(result);
		// new_result.addAll(r.result);
		// return new ClassificationResult(new_result, type | r.type);
		// }
		// else {
		// Collections.sort(result, new
		// ClassificationResult.CharacterComparator());
		// Collections.sort(r.result, new
		// ClassificationResult.CharacterComparator());
		//
		// ArrayList<Label> smaller_set = null;
		// ArrayList<Label> larger_set = null;
		//
		// if (result.size() > r.result.size()) {
		// smaller_set = r.result;
		// larger_set = result;
		// }
		// else {
		// smaller_set = result;
		// larger_set = r.result;
		// }
		// int start = 0;
		// while (larger_set.get(start).label != smaller_set.get(0).label)
		// start++;
		//
		// ArrayList<Label> new_result = new ArrayList<Label>(larger_set);
		// for (int i = 0; i < smaller_set.size(); ++i)
		// new_result.get(start + i).belief *= smaller_set.get(i).belief;
		//
		// return new ClassificationResult(new_result, type | r.type);
		// }
		Map<Character, Float> a = getLabelsAsMap();
		Map<Character, Float> b = r.getLabelsAsMap();
		Set<Character> union_keys = a.keySet();
		for (Character c : b.keySet())
			if (!union_keys.contains(c)) union_keys.add(c);
			
		ArrayList<Label> new_result = new ArrayList<ClassificationResult.Label>();
		Character[] union_keys_array = union_keys.toArray(new Character[union_keys.size()]);
		for (Character c : union_keys_array) {
			if (a.get(c) != null && b.get(c) != null) {
				new_result.add(new Label(c, a.get(c) * b.get(c)));
			}
			else if (a.get(c) == null && b.get(c) != null) {
				new_result.add(new Label(c, b.get(c) * b.get(c)));
			}
			else if (a.get(c) != null && b.get(c) == null) {
				new_result.add(new Label(c, a.get(c) * a.get(c)));
			}
		}
		return new ClassificationResult(new_result, type | r.type);
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
