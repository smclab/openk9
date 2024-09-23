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

package io.openk9.datasource.script;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import io.openk9.datasource.processor.payload.DataPayload;
import io.vavr.control.Try;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

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
