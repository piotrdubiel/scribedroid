package pl.scribedroid;

import org.junit.runners.model.InitializationError;

import pl.scribedroid.app.ScribeDroidApplication;
import pl.scribedroid.input.GestureShadow;

import roboguice.RoboGuice;
import roboguice.test.RobolectricRoboTestRunner;

import com.xtremelabs.robolectric.Robolectric;

public class ScribeDroidTestRunner extends RobolectricRoboTestRunner {

	public ScribeDroidTestRunner(final Class<?> testClass)
			throws InitializationError {
		super(testClass);
	}

	@Override
	public void prepareTest(final Object test) {
		final ScribeDroidApplication application = (ScribeDroidApplication) Robolectric.application;
		RoboGuice.injectMembers(application, test);
	}

	@Override
	protected void bindShadowClasses() {
		Robolectric.bindShadowClass(GestureShadow.class);
	}
}
