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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.assertj.core.api.Assertions.assertThat;

public class EntitiesInDefaultPUWithImplicitUnconfiguredDatasourceTest {

	@RegisterExtension
	static QuarkusUnitTest runner = new QuarkusUnitTest()
		.withApplicationRoot((jar) -> jar
			.addClass(MyEntity.class))
		// The datasource won't be truly "unconfigured" if dev services are enabled
		.overrideConfigKey("quarkus.devservices.enabled", "false")
		.assertException(t -> assertThat(t)
			.isInstanceOf(ConfigurationException.class)
			.hasMessageContaining(
				"The default datasource must be configured for Hibernate Reactive. Refer to https://quarkus.io/guides/datasource for guidance."));

	@Test
	public void testInvalidConfiguration() {
		// bootstrap will succeed and ignore the fact that a datasource is unconfigured...
	}

}
