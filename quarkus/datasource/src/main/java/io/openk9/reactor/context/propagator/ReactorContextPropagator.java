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

package io.openk9.reactor.context.propagator;

import org.eclipse.microprofile.context.ThreadContext;
import org.eclipse.microprofile.context.spi.ContextManager;
import org.eclipse.microprofile.context.spi.ContextManagerExtension;
import reactor.core.publisher.Hooks;

public class ReactorContextPropagator implements ContextManagerExtension {

	@Override
	public void setup(ContextManager manager) {
		ThreadContext threadContext = manager.newThreadContextBuilder().build();
		Hooks.onEachOperator(ThreadContextSubscriber.asOperator(threadContext));
		Hooks.onLastOperator(ThreadContextSubscriber.asOperator(threadContext));

	}

}
