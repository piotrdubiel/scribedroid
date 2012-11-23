package pl.scribedroid.app;

import pl.scribedroid.module.ScribeDroidModule;
import roboguice.RoboGuice;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.util.Modules;

import android.app.Application;

public class ScribeDroidApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
        setModules(new ScribeDroidModule());
	}
	
	public void setModules(final AbstractModule... newModule) {
        final Module modules = Modules.override(RoboGuice.newDefaultRoboModule(this)).with(newModule);
        RoboGuice.setBaseApplicationInjector(this, RoboGuice.DEFAULT_STAGE, modules);
    }
}
