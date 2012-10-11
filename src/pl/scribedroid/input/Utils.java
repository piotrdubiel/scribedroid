package pl.scribedroid.input;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.gesture.Gesture;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Environment;
import android.util.Pair;
import android.view.KeyEvent;

public class Utils {
	public static final String USER_ALPHA_FILENAME = "user_alpha_lib";
	public static final String USER_NUMBER_FILENAME = "user_number_lib";
	public static final int TRAINING_CYCLES = 100;

	private static final int VECTOR_LENGTH = 784;
	public static final int RESULT_COUNT = 4;
	
	public static float[] applyPCA(float[] in, float[] mu,float[][] trmx) {
		for (int i=0;i<in.length;++i) {
			in[i]-=mu[i];
			 
		}
		
		float[] out=new float[trmx.length];
		for (int i=0;i<trmx.length;++i) {
			out[i]=0.0f;
			for (int j=0;j<trmx[i].length;++j) {
				out[i]+=in[j]*trmx[i][j];
			}
		}
		return out;
	}
	
	public static Bitmap dilation(Bitmap in,int n,float threshold) {
		Bitmap out=null;
		for (int k=0;k<n;++k) {
			out=Bitmap.createBitmap(in);
			for (int i=1;i<in.getWidth()-1;++i) {
		        for (int j=1;j<in.getHeight()-1;++j) {
	            	int c=in.getPixel(i, j);            	
	            	float gray=toGray(c);
	            	if (gray<threshold) {
	            		if (toGray(in.getPixel(i-1, j))>=threshold ||
	            			toGray(in.getPixel(i, j-1))>=threshold ||
	            			toGray(in.getPixel(i+1, j))>=threshold ||
	            			toGray(in.getPixel(i, j+1))>=threshold) 
	            		{
	            				out.setPixel(i, j, Color.WHITE);
	            			}
	            		else {
	                		out.setPixel(i,j,Color.BLACK);
	            		}
	            	}
	            	else {
	            		out.setPixel(i,j,Color.WHITE);
	            	}
	
		        }
			}
			in=out;
		}
		return out;		
	}
	
	public static float toGray(int color) {
		int red,green,blue;
    	red=Color.red(color);
    	green=Color.green(color);
    	blue=Color.blue(color);
		return (red+green+blue)/(255.0f*3.0f);
	}
	
	public static int toColor(float gray) {
		int color;
		color=(255 << 24) | ((int)gray*255 << 16) | ((int)gray*255 << 8) | (int)gray*255;
		return color;
	}
	
	public static String decode(int i,int type) {
		if (type==Classificator.ALPHA) {
			switch (i) {
				case 0: return "a";
				case 1: return "b";
				case 2: return "c";
				case 3: return "d";
				case 4: return "e";
				case 5: return "f";
				case 6: return "g";
				case 7: return "h";
				case 8: return "i";
				case 9: return "j";
				case 10: return "k";
				case 11: return "l";
				case 12: return "m";
				case 13: return "n";
				case 14: return "o";
				case 15: return "p";
				case 16: return "q";
				case 17: return "r";
				case 18: return "s";
				case 19: return "t";
				case 20: return "u";
				case 21: return "v";
				case 22: return "w";
				case 23: return "x";
				case 24: return "y";
				case 25: return "z";
			}
		}
		else if (type==Classificator.NUMBER) {
			switch (i) {
				case 0: return "0";
				case 1: return "1";
				case 2: return "2";
				case 3: return "3";
				case 4: return "4";
				case 5: return "5";
				case 6: return "6";
				case 7: return "7";
				case 8: return "8";
				case 9: return "9";
			}
		}
		return "";
	}

	public static Bitmap getBitmapFromGesture(Gesture in) {
		int width=(int)Math.abs(in.getBoundingBox().right-in.getBoundingBox().left);
	  	int height=(int) Math.abs(in.getBoundingBox().top-in.getBoundingBox().bottom);
	  	
	  	if (width<=0 || height<=0) return null; 
	  	
	  	if (width>height) {
	  		height=20*height/width;
	  		width=20;
	  	}
	  	else {
	  		width=20*width/height;
	  		height=20;
	  	}
	  	
	    return in.toBitmap(width+10,height+10,2,Color.WHITE);
	}
	
	public static float[] getVectorFromBitmap(Bitmap in) {
		float[] out=new float[VECTOR_LENGTH];
		
		for (int i=0;i<out.length;++i) {
        	out[i]=1.0f;
        }
		
        Point center=getCenter(in);
        
        int startx=(int) (14-center.x);
        int starty=(int) (14-center.y);
        
        for (int y=0;y<in.getHeight();++y) {
        	for (int x=0;x<in.getWidth();++x) {
				try {
            	int c=in.getPixel(x, y);
            	out[28*(starty+y)+startx+x]=1.0f-toGray(c);
				}
				catch (Exception e) {}
            }
        }
		
		return out;
	}
	
	public static Point getCenter(Bitmap in) {
		float centerx=0;
        float centery=0;
        float sum=0;
        for (int y=0;y<in.getHeight();++y) {
        	for (int x=0;x<in.getWidth();++x) {
        		float gray=toGray(in.getPixel(x, y));
        		centerx+=x*gray;
        		centery+=y*gray;
        		sum+=gray;
        	}
        }
        
        centerx=centerx/Math.max(sum,1);
		centery=centery/Math.max(sum,1);
		
		return new Point((int)centerx,(int)centery);
	}
	
	public static void saveBitmap(Bitmap in,String filename) {
		File file=new File(Environment.getExternalStorageDirectory(), filename);
    	try {
			FileOutputStream os=new FileOutputStream(file);
			in.compress(Bitmap.CompressFormat.PNG, 100, os);
			os.flush();
			os.close();
		}
		catch (FileNotFoundException e) {} 
		catch (IOException e) {}
	}
	
	public static void saveVector(float[] in,String filename) {
		if (in.length!=VECTOR_LENGTH) return;
		int size=(int) Math.sqrt(in.length);
		Bitmap output=Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
		
		for (int i=0;i<in.length;++i) {
			int y=(int) Math.floor(i/28);
			int x=i-28*y;
			output.setPixel(x, y, toColor(in[i]));
		}
		saveBitmap(output,filename);
	}
	
	public static List<Pair<Float,String>> getBest(float[] in,int n,int type) {
		List<Pair<Float,String>> out=new ArrayList<Pair<Float,String>>();
	    List<Integer> indexList=new ArrayList<Integer>();
	      
	    for (int r=0;r<n;++r) {        
	    	int imax=-1;
		    float max=-1.0f;
		        
		    for (int i=0;i<in.length;++i) {
		    	if (in[i]>max && !indexList.contains(i)) {
		    		max=in[i];
		        	imax=i;
		        	indexList.add(i);
		    	}
		    }
		    
		    if (imax==-1) return out;
		    out.add(new Pair<Float,String>(in[imax],decode(imax,type)));
	    }
	    return out;
	}
	
	public static int getKeyCode(String label) {		
		if (label.equals("!#space")) {
			return KeyEvent.KEYCODE_SPACE;
		}
		else if (label.equals("!#delete")) {
			return KeyEvent.KEYCODE_DEL;
		}
		else if (label.equals("a")) {
			return KeyEvent.KEYCODE_A;
		}
		else if (label.equals("b")) {
			return KeyEvent.KEYCODE_B;
		}
		else if (label.equals("c")) {
			return KeyEvent.KEYCODE_C;
		}
		else if (label.equals("d")) {
			return KeyEvent.KEYCODE_D;
		}
		else if (label.equals("e")) {
			return KeyEvent.KEYCODE_E;
		}
		else if (label.equals("f")) {
			return KeyEvent.KEYCODE_F;
		}
		else if (label.equals("g")) {
			return KeyEvent.KEYCODE_G;
		}
		else if (label.equals("h")) {
			return KeyEvent.KEYCODE_H;
		}
		else if (label.equals("i")) {
			return KeyEvent.KEYCODE_I;
		}
		else if (label.equals("j")) {
			return KeyEvent.KEYCODE_J;
		}
		else if (label.equals("k")) {
			return KeyEvent.KEYCODE_K;
		}
		else if (label.equals("l")) {
			return KeyEvent.KEYCODE_L;
		}
		else if (label.equals("m")) {
			return KeyEvent.KEYCODE_M;
		}
		else if (label.equals("n")) {
			return KeyEvent.KEYCODE_N;
		}
		else if (label.equals("o")) {
			return KeyEvent.KEYCODE_O;
		}
		else if (label.equals("p")) {
			return KeyEvent.KEYCODE_P;
		}
		else if (label.equals("q")) {
			return KeyEvent.KEYCODE_Q;
		}
		else if (label.equals("r")) {
			return KeyEvent.KEYCODE_R;
		}
		else if (label.equals("s")) {
			return KeyEvent.KEYCODE_S;
		}
		else if (label.equals("t")) {
			return KeyEvent.KEYCODE_T;
		}
		else if (label.equals("u")) {
			return KeyEvent.KEYCODE_U;
		}
		else if (label.equals("v")) {
			return KeyEvent.KEYCODE_V;
		}
		else if (label.equals("w")) {
			return KeyEvent.KEYCODE_W;
		}
		else if (label.equals("x")) {
			return KeyEvent.KEYCODE_X;
		}
		else if (label.equals("y")) {
			return KeyEvent.KEYCODE_Y;
		}
		else if (label.equals("z")) {
			return KeyEvent.KEYCODE_Z;
		}
		else if (label.equals("0")) {
			return KeyEvent.KEYCODE_0;
		}
		else if (label.equals("1")) {
			return KeyEvent.KEYCODE_1;
		}
		else if (label.equals("2")) {
			return KeyEvent.KEYCODE_2;
		}
		else if (label.equals("3")) {
			return KeyEvent.KEYCODE_3;
		}
		else if (label.equals("4")) {
			return KeyEvent.KEYCODE_4;
		}
		else if (label.equals("5")) {
			return KeyEvent.KEYCODE_5;
		}
		else if (label.equals("6")) {
			return KeyEvent.KEYCODE_6;
		}
		else if (label.equals("7")) {
			return KeyEvent.KEYCODE_7;
		}
		else if (label.equals("8")) {
			return KeyEvent.KEYCODE_8;
		}
		else if (label.equals("9")) {
			return KeyEvent.KEYCODE_9;
		}
		return KeyEvent.KEYCODE_UNKNOWN;
		
	}
}