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

package io.quarkus.hibernate.reactive.deployment;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.deployment.logging.LogCleanupFilterBuildItem;

@BuildSteps(onlyIf = HibernateReactiveEnabled.class)
public final class HibernateReactiveLogFilter {

	@BuildStep
	void setupLogFilters(BuildProducer<LogCleanupFilterBuildItem> filters) {
		filters.produce(new LogCleanupFilterBuildItem(
			"org.hibernate.engine.jdbc.connections.internal.ConnectionProviderInitiator",
			"HHH000181"
		));
		//See https://hibernate.atlassian.net/browse/HHH-16224
		filters.produce(new LogCleanupFilterBuildItem(
			"org.hibernate.dialect.PostgreSQLPGObjectJdbcType", "HHH000514"));
	}

}
