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

import io.openk9.datasource.processor.payload.IngestionPayload;
import io.vavr.control.Try;
import org.graalvm.polyglot.Context;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class JavascriptService {

	@PostConstruct
	void init() {
		context = Context.newBuilder("js").build();
	}

	@PreDestroy
	void destroy() {
		context.close();
	}

	public Try<Boolean> isValidCondition(String script) {
		return executeScriptCondition(script, new IngestionPayload());
	}

	public Try<Boolean> executeScriptCondition(String script, IngestionPayload payload) {
		return Try.withResources(() -> context)
			.of(ctx -> {
				ctx
					.getBindings("js")
					.putMember("payload", payload);

				return ctx.eval("js", script).asBoolean();

			});
	}

	private Context context;

}
