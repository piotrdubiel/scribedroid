package pl.scribedroid.settings;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import pl.scribedroid.R;

public class SettingsActivity extends PreferenceActivity {
	
	 @Override
	 public void onCreate(Bundle savedInstanceState) {       
		 super.onCreate(savedInstanceState);       
		 addPreferencesFromResource(R.xml.preferences);		 
	 }
	 
}
