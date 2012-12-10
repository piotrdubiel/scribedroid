package pl.scribedroid.input.ann;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pl.scribedroid.input.Utils;
import pl.scribedroid.input.classificator.ClassificationResult;
import pl.scribedroid.input.classificator.Classificator;
import android.content.Context;
import android.util.Pair;

public class LetterNetwork extends Network {

	public LetterNetwork(int[] arch) {
		super(arch);
	}

	private LetterNetwork() {
		super();
	}

	public static Network createFromRawResource(Context context,int rid) {
		return new LetterNetwork().load(context.getResources().openRawResource(rid));
	}
	
	public ClassificationResult classify(float[] in) {
		final float[] result = answer(in);
		return new ClassificationResult() {
			List<Pair<Character, Float>> results;
			
			@Override
			public Pair<Character, Float> getResult(int index) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Pair<Character, Float> getBestResult() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public List<Pair<Character, Float>> getBestResults(int max) {
				// TODO Auto-generated method stub
				return null;
			}

			public ClassificationResult prepare() {
				results = new ArrayList<Pair<Character,Float>>();
				
			    for (int i=0; i < result.length; i++) {
			    	results.add(new Pair<Character, Float>(Utils.decode(i, Classificator.ALPHA), result[i]));
			    }
			    
			    Collections.sort(results, new ResultComparator());
			    
			    return this;
			}
			
			

		}.prepare();		
	}
}
