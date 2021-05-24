package io.openk9.util.script.groovy.internal;

import groovy.io.GroovyPrintStream;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import io.openk9.util.script.groovy.api.GroovyScriptExecutor;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import java.io.ByteArrayOutputStream;
import java.util.Map;

@Component(
	immediate = true,
	service = GroovyScriptExecutor.class
)
public class GroovyScriptExecutorImpl implements GroovyScriptExecutor {

	private BundleContext _bundleContext;

	@Activate
	void activate(BundleContext bundleContext) {
		_bundleContext = bundleContext;
	}

	@Override
	public byte[] execute(String script) {

		GroovyShell groovyShell = new GroovyShell(getClassLoader());

		Script compiledScript = groovyShell.parse(script);

		ByteArrayOutputStream response = new ByteArrayOutputStream();

		compiledScript.setBinding(
			new Binding(
				Map.of(
					"out", new GroovyPrintStream(response),
					"context", _bundleContext
				)
			)
		);

		compiledScript.run();

		return response.toByteArray();

	}

	ClassLoader getClassLoader() {
		Bundle bundle = _bundleContext.getBundle();

		BundleWiring bundleWiring = bundle.adapt(BundleWiring.class);

		return bundleWiring.getClassLoader();
	}


}
