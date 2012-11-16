package pl.scribedroid.input;

import pl.scribedroid.R;
import roboguice.RoboGuice;
import android.view.View;

public abstract class InputMethodController {
	public View inputView;
	ScribeDroid service;
	
	public InputMethodController(ScribeDroid s,int viewId) {
		service = s;
		inputView = service.getLayoutInflater().inflate(viewId, null);
		RoboGuice.getBaseApplicationInjector(service.getApplication()).injectMembers(this);
	}
	
	public abstract void resetModifiers();
}
