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

package io.quarkus.hibernate.reactive.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.quarkus.runtime.configuration.ConfigurationException;
import io.quarkus.test.QuarkusUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class ConfigEnabledFalseAndActiveTrueTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
		.withApplicationRoot(jar -> jar.addClass(MyEntity.class))
		.withConfigurationResource("application.properties")
		.overrideConfigKey("quarkus.hibernate-orm.enabled", "false")
		.overrideConfigKey("quarkus.hibernate-orm.active", "true")
		.assertException(throwable -> assertThat(throwable)
			.isInstanceOf(ConfigurationException.class)
			.hasMessageContaining(
				"Hibernate ORM activated explicitly for persistence unit '<default>', but the Hibernate ORM extension was disabled at build time",
				"If you want Hibernate ORM to be active for this persistence unit, you must set 'quarkus.hibernate-orm.enabled' to 'true' at build time",
				"If you don't want Hibernate ORM to be active for this persistence unit, you must leave 'quarkus.hibernate-orm.active' unset or set it to 'false'"
			));

    @Test
    public void test() {
        // Startup will fail
    }
}
