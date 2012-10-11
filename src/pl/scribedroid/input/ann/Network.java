package pl.scribedroid.input.ann;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import android.content.Context;

public class Network {
	private ArrayList<Layer> layers;
	
	private Network() {}
	
	public Network(int[] arch) {
		layers=new ArrayList<Layer>();
		for (int i=1;i<arch.length;++i) {
	        layers.add(new Layer(arch[i],arch[i-1]));
		}
	}
	
	public float[] classify(float[] in) {
		float[] result=in;
	    for (Layer l : layers) {
	    	result=l.answer(result);
	    }

	    return result;
	}
	
	public List<List<Vector>> train(float[] in,int label) {
		List<Vector> y=new ArrayList<Vector>();
		List<Vector> dy=new ArrayList<Vector>();
		
		y.add(new Vector(in));
	    for (Layer l : layers) {
	    	dy.add(new Vector(l.dy(y.get(y.size()-1).toArray())));
	    	y.add(new Vector(l.answer(y.get(y.size()-1).toArray())));	    	
	    }
	    
	    List<Vector> d=new ArrayList<Vector>();
	    int lastLayer=dy.get(dy.size()-1).size();
	    float[] l=new float[lastLayer];
		    
	    for (int i=0;i<lastLayer;++i) {
	    	if (i==label) l[i]=1;
	    	else l[i]=0;
	    }

	    d.add(new Vector(l).substract(y.get(y.size()-1)).multiply(dy.get(dy.size()-1)));
	    
	    for (int i=layers.size()-2;i>=0;--i) {
	    	Vector D=new Vector(0,layers.get(i+1).get(0).size()-1);
    		for (int q=0;q<layers.get(i+1).get(0).size()-1;++q) {
    			float sum=0;
    			for (int p=0;p<layers.get(i+1).size();++p) {
	    			sum+=layers.get(i+1).get(p).get(q)*d.get(layers.size()-i-2).get(p);
	    		}
    			D.set(sum*dy.get(i).get(q),q);
	    	}
    		d.add(D);
	    }
	    
	    for (int i=0;i<layers.size();++i) {
	    	for (int j=0;j<layers.get(i).size();++j) {
	    		Vector Y=new Vector(y.get(i)).append(1.0f);
	    		Y.multiply(d.get(layers.size()-i-1).get(j));
	    		layers.get(i).get(j).add(Y);
	    	}
	    }
	    
	    List<List<Vector>> result=new ArrayList<List<Vector>>();
	    result.add(y);
	    result.add(dy);
	    result.add(d);
	    
	    return result;
	}
	
	public Network load(InputStream file_input) {
	    try {
	        DataInputStream data_in    = new DataInputStream (file_input);

	        int layer=data_in.readInt();
	        int[] arch=new int[layer];
	        
	        for (int i=0;i<layer;++i) {
	        	arch[i]=data_in.readInt();
	        }
	        
	        layers=new ArrayList<Layer>();
	        
	        for (int l=0;l<layer-1;++l) {
	        	layers.add(new Layer());
	        }
	    	
	    	for (int i=0;i<arch.length-2;++i) {
	    		float[][] w=new float[arch[i+1]-1][arch[i]];
	    		for (int j=0;j<arch[i];++j) {
	    			for (int k=0;k<arch[i+1]-1;++k) {
	    				w[k][j]=data_in.readFloat();
	    			}
	    		}
	    		layers.get(i).set(w);
	    	}

    		float[][] w=new float[arch[arch.length-1]][arch[arch.length-2]];
	    	for (int j=0;j<arch[arch.length-2];++j) {
	    		for (int k=0;k<arch[arch.length-1];++k) {
	    			w[k][j]=data_in.readFloat();
	    		}
	    	}
    		layers.get(arch.length-2).set(w);
	        
	        data_in.close ();
	    } catch  (IOException e) {
	    	System.out.println ( "IO Exception LOADING =: " + e );
	    }		
		
		return this;
	}
	
	public static Network createFromRawResource(Context context,int rid) {
		return new Network().load(context.getResources().openRawResource(rid));
	}
	
	public Layer get(int location) {
		return layers.get(location);
	}
	
	public int size() {
		return layers.size();
	}
}
