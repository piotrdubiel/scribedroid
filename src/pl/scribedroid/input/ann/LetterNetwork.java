package pl.scribedroid.input.ann;

import java.util.ArrayList;
import java.util.List;

import pl.scribedroid.input.classificator.ClassificationResult;
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
		float[] result = answer(in);
		return new ClassificationResult() {
			List<Pair<String,Float>> results;			
			@Override
			public Pair<String, Float> getResult(int index) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Pair<String, Float> getBestResult() {
				// TODO Auto-generated method stub
				return null;
			}
			
			private void prepare() {
				results = new ArrayList<Pair<String,Float>>();
			    List<Integer> indexList=new ArrayList<Integer>();
			      
			    
			}
		};		
	}
}
