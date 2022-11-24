package io.openk9.datasource.script;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import io.openk9.datasource.processor.payload.DataPayload;
import io.vavr.control.Try;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.util.Map;

@ApplicationScoped
public class GroovyService {

	@PostConstruct
	void init() {
		groovyShell = new GroovyShell();
	}

	public Try<Boolean> isValidCondition(String script) {
		return executeScriptCondition(script, new DataPayload());
	}

	public Try<Boolean> executeScriptCondition(String script, DataPayload payload) {
		return Try.of(() -> {

			Script parse = groovyShell.parse(script);

			parse.setBinding(new Binding(Map.of("payload", payload)));

			return (Boolean)parse.run();

		});
	}

	private GroovyShell groovyShell;

}
