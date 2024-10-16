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

package io.quarkus.hibernate.reactive.config.datasource;

import io.quarkus.hibernate.reactive.config.MyEntity;
import io.quarkus.runtime.configuration.ConfigurationException;
import io.quarkus.test.QuarkusUnitTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.assertj.core.api.Assertions.assertThat;

public class EntitiesInDefaultPUWithImplicitDatasourceConfigActiveFalseTest {

	@RegisterExtension
	static QuarkusUnitTest runner = new QuarkusUnitTest()
		.withApplicationRoot((jar) -> jar
			.addClass(MyEntity.class))
		.withConfigurationResource("application.properties")
		.overrideConfigKey("quarkus.datasource.active", "false")
		.assertException(t -> assertThat(t)
			.isInstanceOf(ConfigurationException.class)
			.hasMessageContainingAll(
				"Unable to find datasource '<default>' for persistence unit 'default-reactive'",
				"Datasource '<default>' was deactivated through configuration properties.",
				"To solve this, avoid accessing this datasource at runtime, for instance by deactivating consumers (persistence units, ...).",
				"Alternatively, activate the datasource by setting configuration property 'quarkus.datasource.active'"
				+ " to 'true' and configure datasource '<default>'",
				"Refer to https://quarkus.io/guides/datasource for guidance."
			));

	@Test
	public void testInvalidConfiguration() {
		// deployment exception should happen first
		Assertions.fail();
	}

}
