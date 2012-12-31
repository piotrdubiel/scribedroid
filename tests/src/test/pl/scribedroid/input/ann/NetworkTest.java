package pl.scribedroid.input.ann;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import pl.scribedroid.R;
import roboguice.test.RobolectricRoboTestRunner;
import android.app.Activity;
import android.content.Context;

@RunWith(RobolectricRoboTestRunner.class)
public class NetworkTest {
	Context context;

	@Before
	public void setUp() {
		context = new Activity();
	}

	@Test
	public void testNetworkLoad() {
		// when
		NetworkImpl alphaNet = NetworkImpl.createFromRawResource(context, R.raw.alphanet);
		
		// then
		Assert.assertNotNull(alphaNet);
	}
}
