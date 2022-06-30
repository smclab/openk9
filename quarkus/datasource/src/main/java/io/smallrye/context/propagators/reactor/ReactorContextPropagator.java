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

package io.smallrye.context.propagators.reactor;

import org.eclipse.microprofile.context.ThreadContext;
import org.eclipse.microprofile.context.spi.ContextManager;
import org.eclipse.microprofile.context.spi.ContextManagerExtension;
import org.jboss.logging.Logger;
import reactor.core.scheduler.Schedulers;

public class ReactorContextPropagator implements ContextManagerExtension {

	@Override
	public void setup(ContextManager manager) {

		Logger.getLogger(ReactorContextPropagator.class).info("ReactorContextPropagator setup");

		ThreadContext threadContext = manager.newThreadContextBuilder().build();

		Schedulers.onScheduleHook(
			ReactorContextPropagator.class.getName(),
			threadContext::contextualRunnable);

	}

}