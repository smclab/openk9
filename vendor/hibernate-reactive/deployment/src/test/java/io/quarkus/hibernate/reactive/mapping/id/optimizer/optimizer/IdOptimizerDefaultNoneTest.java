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

package io.quarkus.hibernate.reactive.mapping.id.optimizer.optimizer;

import io.quarkus.hibernate.reactive.SchemaUtil;
import io.quarkus.test.QuarkusUnitTest;
import io.quarkus.test.vertx.UniAsserter;
import org.hibernate.id.enhanced.NoopOptimizer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.RegisterExtension;

public class IdOptimizerDefaultNoneTest extends AbstractIdOptimizerDefaultTest {

	@RegisterExtension
	static QuarkusUnitTest TEST = new QuarkusUnitTest()
		.withApplicationRoot((jar) -> jar
			.addClasses(EntityWithDefaultGenerator.class, EntityWithGenericGenerator.class,
				EntityWithSequenceGenerator.class, EntityWithTableGenerator.class,
				EntityWithGenericGeneratorAndPooledOptimizer.class,
				EntityWithGenericGeneratorAndPooledLoOptimizer.class
			)
			.addClasses(SchemaUtil.class))
		.withConfigurationResource("application.properties")
		.overrideConfigKey("quarkus.hibernate-orm.mapping.id.optimizer.default", "none");

	@Override
	@Disabled(
		"The 'none' optimizer will produce a different stream of IDs (1 then 51 then 101 then ...)"
	)
	public void ids(UniAsserter asserter) {
		super.ids(asserter);
	}

	@Override
	Class<?> defaultOptimizerType() {
		return NoopOptimizer.class;
	}

}
