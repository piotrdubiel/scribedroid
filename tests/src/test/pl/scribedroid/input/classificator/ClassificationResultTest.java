package pl.scribedroid.input.classificator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import pl.scribedroid.input.Utils;
import pl.scribedroid.input.classificator.ClassificationResult.Label;
import roboguice.test.RobolectricRoboTestRunner;

@RunWith(RobolectricRoboTestRunner.class)
public class ClassificationResultTest {
	private static final float EPS = 0.0001f;

	@Before
	public void setUp() {}

	@Test
	public void testFlags() {
		Assert.assertEquals(1, Classificator.SMALL_ALPHA);
		Assert.assertEquals(2, Classificator.CAPITAL_ALPHA);
		Assert.assertEquals(4, Classificator.DIGIT);
		Assert.assertEquals(3, Classificator.SMALL_ALPHA | Classificator.CAPITAL_ALPHA);
	}

	@Test
	public void testLabelComparator() {
		// given
		ArrayList<Label> list = new ArrayList<Label>();
		list.add(new Label('a', 0.1f));
		list.add(new Label('b', 0.5f));
		list.add(new Label('c', 0.3f));
		list.add(new Label('ą', 0.9f));

		// when
		Collections.sort(list, new ClassificationResult.LabelComparator());

		// then
		Assert.assertEquals('ą', list.get(0).label.charValue());
		Assert.assertEquals(0.9f, list.get(0).belief);

		Assert.assertEquals('b', list.get(1).label.charValue());
		Assert.assertEquals(0.5f, list.get(1).belief);

		Assert.assertEquals('c', list.get(2).label.charValue());
		Assert.assertEquals(0.3f, list.get(2).belief);

		Assert.assertEquals('a', list.get(3).label.charValue());
		Assert.assertEquals(0.1f, list.get(3).belief);
	}

	@Test
	public void testCharacterComparator() {
		// given
		ArrayList<Label> list = new ArrayList<Label>();
		list.add(new Label('a', 0.1f));
		list.add(new Label('d', 0.5f));
		list.add(new Label('Z', 0.3f));
		list.add(new Label('ą', 0.9f));

		// when
		Collections.sort(list, new ClassificationResult.CharacterComparator());

		// then
		Assert.assertEquals('Z', list.get(0).label.charValue());
		Assert.assertEquals(0.3f, list.get(0).belief);

		Assert.assertEquals('a', list.get(1).label.charValue());
		Assert.assertEquals(0.1f, list.get(1).belief);

		Assert.assertEquals('ą', list.get(2).label.charValue());
		Assert.assertEquals(0.9f, list.get(2).belief);

		Assert.assertEquals('d', list.get(3).label.charValue());
		Assert.assertEquals(0.5f, list.get(3).belief);
	}

	@Test
	public void testGetLabels() {
		// given
		float[] in = { 0.001f, 0.01f, 0.02f, 0.03f, 0.04f, 0.05f, 0.06f, 0.07f, 0.08f, 0.09f };

		// when
		ClassificationResult cr = new ClassificationResult(in, Classificator.DIGIT);
		Character[] labels = cr.getLabels();

		// then
		Assert.assertEquals('9', labels[0].charValue());
		Assert.assertEquals('8', labels[1].charValue());
		Assert.assertEquals('7', labels[2].charValue());
		Assert.assertEquals('6', labels[3].charValue());
		Assert.assertEquals('5', labels[4].charValue());
		Assert.assertEquals('4', labels[5].charValue());
		Assert.assertEquals('3', labels[6].charValue());
		Assert.assertEquals('2', labels[7].charValue());
		Assert.assertEquals('1', labels[8].charValue());
		Assert.assertEquals('0', labels[9].charValue());

	}

	@Test
	public void testGetLabelsWithBelief() {
		// given
		float[] in = { 0.001f, 0.01f, 0.02f, 0.03f, 0.04f, 0.05f, 0.06f, 0.07f, 0.08f, 0.09f };

		// when
		ClassificationResult cr = new ClassificationResult(in, Classificator.DIGIT);
		Label[] labels = cr.getLabelsWithBelief();

		// then
		Assert.assertEquals('9', labels[0].label.charValue());
		Assert.assertEquals(in[9], labels[0].belief);

		Assert.assertEquals('8', labels[1].label.charValue());
		Assert.assertEquals(in[8], labels[1].belief);

		Assert.assertEquals('7', labels[2].label.charValue());
		Assert.assertEquals(in[7], labels[2].belief);

		Assert.assertEquals('6', labels[3].label.charValue());
		Assert.assertEquals(in[6], labels[3].belief);

		Assert.assertEquals('5', labels[4].label.charValue());
		Assert.assertEquals(in[5], labels[4].belief);

		Assert.assertEquals('4', labels[5].label.charValue());
		Assert.assertEquals(in[4], labels[5].belief);

		Assert.assertEquals('3', labels[6].label.charValue());
		Assert.assertEquals(in[3], labels[6].belief);

		Assert.assertEquals('2', labels[7].label.charValue());
		Assert.assertEquals(in[2], labels[7].belief);

		Assert.assertEquals('1', labels[8].label.charValue());
		Assert.assertEquals(in[1], labels[8].belief);

		Assert.assertEquals('0', labels[9].label.charValue());
		Assert.assertEquals(in[0], labels[9].belief);
	}

	@Test
	public void testCombineSameType() throws Exception {
		// given
		float[] a = { 0.0f, 0.01f, 0.02f, 0.03f, 0.04f, 0.05f, 0.06f, 0.07f, 0.08f, 0.09f };
		float[] b = { 1.0f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f };

		// when
		ClassificationResult A = new ClassificationResult(a, Classificator.DIGIT);
		ClassificationResult B = new ClassificationResult(b, Classificator.DIGIT);

		ClassificationResult AB = A.combine(B);
		ClassificationResult BA = B.combine(A);
		
		// then
		Assert.assertEquals('0', AB.result.get(0).label, EPS);
		Assert.assertEquals('1', AB.result.get(1).label, EPS);
		Assert.assertEquals('2', AB.result.get(2).label, EPS);
		Assert.assertEquals('3', AB.result.get(3).label, EPS);
		Assert.assertEquals('4', AB.result.get(4).label, EPS);
		Assert.assertEquals('5', AB.result.get(5).label, EPS);
		Assert.assertEquals('6', AB.result.get(6).label, EPS);
		Assert.assertEquals('7', AB.result.get(7).label, EPS);
		Assert.assertEquals('8', AB.result.get(8).label, EPS);
		Assert.assertEquals('9', AB.result.get(9).label, EPS);
		
		Assert.assertEquals(0.0f, AB.result.get(0).belief, EPS);
		Assert.assertEquals(0.001f, AB.result.get(1).belief, EPS);
		Assert.assertEquals(0.004f, AB.result.get(2).belief, EPS);
		Assert.assertEquals(0.009f, AB.result.get(3).belief, EPS);
		Assert.assertEquals(0.016f, AB.result.get(4).belief, EPS);
		Assert.assertEquals(0.025f, AB.result.get(5).belief, EPS);
		Assert.assertEquals(0.036f, AB.result.get(6).belief, EPS);
		Assert.assertEquals(0.049f, AB.result.get(7).belief, EPS);
		Assert.assertEquals(0.064f, AB.result.get(8).belief, EPS);
		Assert.assertEquals(0.081f, AB.result.get(9).belief, EPS);

		Assert.assertEquals(0.0f, BA.result.get(0).belief, EPS);
		Assert.assertEquals(0.001f, BA.result.get(1).belief, EPS);
		Assert.assertEquals(0.004f, BA.result.get(2).belief, EPS);
		Assert.assertEquals(0.009f, BA.result.get(3).belief, EPS);
		Assert.assertEquals(0.016f, BA.result.get(4).belief, EPS);
		Assert.assertEquals(0.025f, BA.result.get(5).belief, EPS);
		Assert.assertEquals(0.036f, BA.result.get(6).belief, EPS);
		Assert.assertEquals(0.049f, BA.result.get(7).belief, EPS);
		Assert.assertEquals(0.064f, BA.result.get(8).belief, EPS);
		Assert.assertEquals(0.081f, BA.result.get(9).belief, EPS);
	}

	@Test
	public void testCombineDifferentType() throws Exception {
		// given
		float[] a = new float[35];
		for (int i = 0; i < a.length; ++i)
			a[i] = 1.0f;
		ArrayList<Label> b = new ArrayList<ClassificationResult.Label>();
		for (char c : Utils.LETTERS) {
			b.add(new Label(c,0.0f));
			b.add(new Label(Character.toUpperCase(c),1.0f));
		}

		// when
		ClassificationResult A = new ClassificationResult(a, Classificator.SMALL_ALPHA);
		ClassificationResult B = new ClassificationResult(b, Classificator.SMALL_ALPHA | Classificator.CAPITAL_ALPHA | Classificator.GROUP);

		Assert.assertNotNull(A.result);
		Assert.assertNotNull(B.result);

		ClassificationResult AB = B.combine(A);

		// then
		for (Label l : AB.result) {
			System.out.println(l.label + " " + l.belief);
			//Assert.assertEquals(0.0f, l.belief, EPS);
		}
		Assert.assertEquals(B.result.size(), AB.result.size());
	}
	
	@Test
	public void testPairsWith() {
		// given
		float[] a = { 0.0f, 0.3f, 0.0f, 0.0f, 0.04f, 0.05f, 0.06f, 0.07f, 0.08f, 0.09f };
		float[] b = { 0.0f, 0.1f, 0.0f, 0.5f, 0.7f, 0.3f, 0.6f, 0.7f, 0.8f, 0.9f };
		
		// when
		ClassificationResult A = new ClassificationResult(a, Classificator.DIGIT);
		ClassificationResult B = new ClassificationResult(b, Classificator.DIGIT);
		
		// then
		ClassificationResult pairs = A.pairsWith(B);
		
		Assert.assertEquals(4, pairs.result.size());
		
		Map<Character, Float> pairs_map = pairs.getLabelsAsMap();
		
		Assert.assertEquals(0.081f, pairs_map.get('9'), EPS);
		Assert.assertEquals(0.064f, pairs_map.get('8'), EPS);
		Assert.assertEquals(0.049f, pairs_map.get('7'), EPS);
		Assert.assertEquals(0.036f, pairs_map.get('6'), EPS);
		
	}
}
