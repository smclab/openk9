/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.openk9.datasource.pipeline.actor.enrichitem;

import java.util.Map;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * A Groovy enrich item script that resolves a JDK class must compile on the
 * runtime the datasource actually runs on.
 *
 * <p>The bundled ASM has to read the class files of that JDK: with Groovy 4.0.6
 * on Java 21 (major version 65) it does not, and every script fails in semantic
 * analysis with {@code BUG! exception in phase 'semantic analysis' ...
 * Unsupported class file major version 65} — so no Groovy enrich item runs at
 * all. This test fails on a Groovy too old for the current Java.
 */
public class GroovyScriptCompilationTest {

	@Test
	void should_compile_a_script_resolving_a_jdk_class() {
		// uno script come quelli reali: usa una classe del JDK per comporre
		// il valore che restituisce
		String script = """
			def encoded = java.net.URLEncoder.encode("a b", "UTF-8")
			def out = new java.util.LinkedHashMap()
			out.encoded = encoded
			[document: out]
			""";

		var shell = new GroovyShell();

		var parsed = Assertions.assertDoesNotThrow(() -> shell.parse(script));

		// e deve anche eseguire: la compilazione da sola non basta a dire che
		// l'item produrra' un risultato
		parsed.setBinding(new Binding(Map.of()));

		var response = parsed.run();

		Assertions.assertInstanceOf(Map.class, response);
		Assertions.assertEquals(
			Map.of("encoded", "a+b"),
			((Map<?, ?>) response).get("document"));
	}

}
