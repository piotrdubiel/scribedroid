package pl.scribedroid.input.classificator;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pl.scribedroid.R;
import pl.scribedroid.input.Utils;
import pl.scribedroid.input.ann.Network;
import android.content.Context;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.Prediction;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

import com.google.inject.Inject;

public class ClassificatorImpl implements Classificator {
	private static final boolean DEBUG = true;
	
	private Network alphaNet;
	private Network numberNet;
	
	private GestureLibrary alphaLibrary;
	private GestureLibrary numberLibrary;
	
	private float[] mu;
	private float[][] trmx;
	private Context context;
	
	private static final String TAG = "Classificator";

	@Inject
	public ClassificatorImpl(Context c) {
		
		context = c;
		new AsyncTask<Void,Void,Void>() {
			@Override
			protected Void doInBackground(Void... params) {

				alphaNet=Network.createFromRawResource(context, R.raw.alphanet);		
				numberNet=Network.createFromRawResource(context, R.raw.numnet);
						
				loadLibrary();
				
				if (DEBUG) {
					Log.d(TAG,String.valueOf(alphaLibrary.getGestureEntries().size())+" gestures in alpha library");
					Log.d(TAG,String.valueOf(numberLibrary.getGestureEntries().size())+" gestures in number library");
				}
				
				loadPCA();
        		return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				Log.i(TAG,"Classificator loaded");
				super.onPostExecute(result);
				//inputView.setEnabled(true);
			}
        }.execute();
	}
	
	@Override
	public Character classify(Gesture gesture,int type) {		
		if (DEBUG) {
			Log.d(TAG,"Classification start - type "+String.valueOf(type));
		}
		
		long startTime=System.currentTimeMillis();
		
		Network currentNet=null;
		GestureLibrary currentLibrary=null;
		
		Network ln = Network.createFromRawResource(context, R.raw.l_plus_n_net);
		Network lon = Network.createFromRawResource(context, R.raw.l_or_n_net);
		
		Bitmap in=Utils.getBitmapFromGesture(gesture);
		
		if (in==null) return null;

		if (DEBUG) {
			Utils.saveBitmap(in,"input.png");
		}
		
        if (in.getWidth()>in.getHeight()) {
        	in=Bitmap.createScaledBitmap(in, 20, Math.max(20*in.getHeight()/in.getWidth(),1) , true);
        }
        else {
        	in=Bitmap.createScaledBitmap(in, Math.max(20*in.getWidth()/in.getHeight(),1), 20, true);
        }
		
        float[] sample=Utils.getVectorFromBitmap(in);
        
        if (DEBUG) {
        	Utils.saveVector(sample,"output.png");
        }
        
		//PCA        
        sample=Utils.applyPCA(sample,mu,trmx);
        

        float[] lonY = lon.answer(sample);
        
        if (type == Classificator.ALPHA_AND_NUMBER) {
	        if (lonY[0] > lonY[1]) type = Classificator.ALPHA;
	        else type = Classificator.NUMBER;
        }
        
        switch (type) {
        	case ALPHA: currentNet=alphaNet; currentLibrary=alphaLibrary; break;
        	case NUMBER: currentNet=numberNet; currentLibrary=numberLibrary; break;
        	default: currentNet=alphaNet; currentLibrary=alphaLibrary;
        }


	    ArrayList<Prediction> libraryAnswer=currentLibrary.recognize(gesture);
	    
	    if (DEBUG) {
	    	Log.d(TAG,String.valueOf(libraryAnswer.size())+" predictions");
	    }
        
        float[] netAnswer=currentNet.answer(sample);
        
        float[] lnY = ln.answer(sample);
        
        List<Pair<Float,String>> lnStr = Utils.getBest(lnY,Utils.RESULT_COUNT,Classificator.ALPHA_AND_NUMBER);
        
        Log.v(TAG,"LN: "+lnStr.get(0).second+" "+lnStr.get(1).second+" "+lnStr.get(2).second+" "+lnStr.get(3).second);
        Log.v(TAG,"LON: "+Arrays.toString(lonY));

        
        
        Character character=metaVote(netAnswer,libraryAnswer,type);

        
        Log.v(TAG, "Classification time: "+String.valueOf((System.currentTimeMillis()-startTime)/1000.0)+"s");
        
		return character;
	}
	
	private Character metaVote(float[] netResult,ArrayList<Prediction> libraryResult,int type) {
		List<Pair<Float,String>> netStrings=Utils.getBest(netResult,Utils.RESULT_COUNT,type);		

		boolean libraryValid=libraryResult!=null && !libraryResult.isEmpty() && libraryResult.get(0).score>2.0;
		boolean netValid=netStrings!=null && !netStrings.isEmpty() && netStrings.get(0).first>0.1;
		
		if (DEBUG) {
			Log.v(TAG, "LIBRARY Valid "+String.valueOf(libraryValid));
			if (libraryValid)
			for (int i=0;i<Math.min(Utils.RESULT_COUNT,libraryResult.size());++i) {
				Log.v(TAG,libraryResult.get(i).name+" "+String.valueOf(libraryResult.get(i).score));
			}
		
			Log.v(TAG, "NET Valid "+String.valueOf(netValid));
			if (netValid)
			for (Pair<Float,String> n :netStrings) {
				Log.v(TAG,n.second+" "+String.valueOf(n.first));
			}
		}
		
		List<String> netPriorities=new ArrayList<String>();
		
		List<String> libraryPriorities=new ArrayList<String>();	
		
		
		if (libraryValid && netValid) {
			if (libraryPriorities.contains(libraryResult.get(0).name)) {
				if (DEBUG) Log.v(TAG, "Library priority");
				return getCharacter(libraryResult.get(0).name);
			}
			else if (netPriorities.contains(netStrings.get(0).second)) {
				if (DEBUG) Log.v(TAG, "Net priority");
				return getCharacter(netStrings.get(0).second);
			}
			else {
				List<Pair<Integer,String>> pairs=new ArrayList<Pair<Integer,String>>();
				for (int i=0;i<Math.min(netStrings.size(),libraryResult.size());++i) {
					for (int j=0;j<Math.min(netStrings.size(),libraryResult.size());++j) {
						if (netStrings.get(i).second.equals(libraryResult.get(j).name) &&
								netStrings.get(i).first>0.2 && libraryResult.get(j).score>2.0) {
							if (DEBUG) Log.v(TAG, "AGREED");
							pairs.add(new Pair<Integer,String>(i+j,libraryResult.get(j).name));
						}
					}
				}
				if (!pairs.isEmpty()) {
					//Find best pair
					int score=Integer.MAX_VALUE;
					String bestMatch="";
					for (Pair<Integer,String> p : pairs) {
						if (p.first<score) {
							score=p.first;
							bestMatch=p.second;
						}
					}
					if (DEBUG) Log.v(TAG, "Pair - "+bestMatch);
					return getCharacter(bestMatch);
				}
			}
			if (netStrings.get(0).first<0.75) {
				if (DEBUG) Log.v(TAG,"Library's better");
				return getCharacter(libraryResult.get(0).name);
			}
			else {
				if (DEBUG) Log.v(TAG,"Net's better");
				return getCharacter(netStrings.get(0).second);
			}
		}
		else if (libraryValid) {
			if (DEBUG) Log.v(TAG, "Only library valid");
			//return getCharacter(libraryResult.get(0).name);
			return null;
		}
		else if (netValid) {
			if (DEBUG) Log.v(TAG, "Only net valid");
			return getCharacter(netStrings.get(0).second);
		}
		else return null;
	}
		
	private void loadPCA() {
		InputStream file_input=context.getResources().openRawResource(R.raw.pca);
		
		try {
			DataInputStream data_in=new DataInputStream(file_input);
			
			if (DEBUG) Log.v(TAG, "Input loaded");
			
			int muSize=data_in.readInt();
			mu=new float[muSize];
			
			for (int i=0;i<muSize;++i) {
				mu[i]=data_in.readFloat();
			}
			
			if (DEBUG) Log.v(TAG, "MU "+String.valueOf(muSize)+" loaded");
			
			int trmxRows=data_in.readInt();
			int trmxCols=data_in.readInt();
			
			trmx=new float[trmxRows][trmxCols];
			
			for (int i=0;i<trmxRows;++i) {
				for (int j=0;j<trmxCols;++j) {
					trmx[i][j]=data_in.readFloat();
				}
			}
			if (DEBUG) Log.v(TAG, "TRMX "+String.valueOf(trmxRows)+"x"+String.valueOf(trmxCols)+" loaded");
			
			data_in.close ();
		} catch  (IOException e) {
			System.out.println ( "PCA IO Exception LOADING =: " + e );
		}		
	}
	
	private Character getCharacter(String name) {
		return name.charAt(0);
	}
	
	private void loadLibrary() {
		GestureLibrary userAlpha = GestureLibraries.fromPrivateFile(context, Utils.USER_ALPHA_FILENAME);
		if (!validLibrary(userAlpha)) {
			alphaLibrary = GestureLibraries.fromRawResource(context, R.raw.default_alpha_lib);
			if (DEBUG) Log.d(TAG, "USER lib for alphas not valid - using default");
		}
		else {
			alphaLibrary = userAlpha;
			if (DEBUG) Log.d(TAG, "USER lib for alphas - OK");
		}
		
		GestureLibrary userNumber = GestureLibraries.fromPrivateFile(context, Utils.USER_NUMBER_FILENAME);
		if (!validLibrary(userNumber)) {
			numberLibrary = GestureLibraries.fromRawResource(context, R.raw.default_number_lib);
			if (DEBUG) Log.d(TAG, "USER lib for numbers not valid - using default");
		}
		else {
			numberLibrary = userNumber;
			if (DEBUG) Log.d(TAG, "USER lib for numbers - OK");
		}
		
		alphaLibrary.load();
		numberLibrary.load();
	}
	
	private boolean validLibrary(GestureLibrary lib) {
		if (lib!=null) {
			if (lib.load() && lib.getGestureEntries().size()>0) {
				if (lib.getGestureEntries().contains("a") && lib.getGestureEntries().size()==26) {
					return true;
				}
				else if (lib.getGestureEntries().contains("0") && lib.getGestureEntries().size()==10) {
					return true;
				}
				return false;
			}
		}
		return false;
	}
}

