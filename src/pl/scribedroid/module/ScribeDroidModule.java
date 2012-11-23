package pl.scribedroid.module;

import pl.scribedroid.input.classificator.Classificator;
import pl.scribedroid.input.classificator.ClassificatorImpl;

import com.google.inject.AbstractModule;

public class ScribeDroidModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(Classificator.class).to(ClassificatorImpl.class);
	}

}
