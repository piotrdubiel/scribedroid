package pl.scribedroid.input.classificator;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import pl.scribedroid.R;
import pl.scribedroid.input.Utils;
import pl.scribedroid.input.ann.NetworkImpl;

import com.google.inject.Inject;

import android.content.Context;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.os.AsyncTask;
import android.util.Log;

public class MetaClassificator implements Classificator {
private static final boolean DEBUG = true;
	
	private NetworkImpl alphaNet;
	private NetworkImpl numberNet;
	
	private GestureLibrary alphaLibrary;
	private GestureLibrary numberLibrary;
	
	private float[] mu;
	private float[][] trmx;
	private Context context;
	
	private static final String TAG = "MetaClassificator";

	@Inject
	public MetaClassificator(Context c) {
		
		context = c;
		new AsyncTask<Void,Void,Void>() {
			@Override
			protected Void doInBackground(Void... params) {

				alphaNet=NetworkImpl.createFromRawResource(context, R.raw.alphanet);		
				numberNet=NetworkImpl.createFromRawResource(context, R.raw.numnet);
						
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
	public List<Character> classify(Gesture gesture, int type) {
		return null;
	}

	@Override
	public ClassificationResult classifiy(Gesture gesture, int type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClassificationResult classifiy(List<ClassificationResult> inputs) {
		// TODO Auto-generated method stub
		return null;
	}

	private void loadPCA() {
		InputStream file_input=context.getResources().openRawResource(R.raw.pca);
		
		try {
			DataInputStream data_in = new DataInputStream(file_input);
			
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
