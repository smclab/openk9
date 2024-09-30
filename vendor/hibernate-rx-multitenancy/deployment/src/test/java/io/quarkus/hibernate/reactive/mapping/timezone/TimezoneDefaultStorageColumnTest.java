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

package io.quarkus.hibernate.reactive.mapping.timezone;

import io.quarkus.hibernate.reactive.SchemaUtil;
import io.quarkus.test.QuarkusUnitTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.assertj.core.api.Assertions.assertThat;

public class TimezoneDefaultStorageColumnTest extends AbstractTimezoneDefaultStorageTest {

	@RegisterExtension
	static QuarkusUnitTest TEST = new QuarkusUnitTest()
		.withApplicationRoot((jar) -> jar
			.addClasses(EntityWithTimezones.class)
			.addClasses(SchemaUtil.class))
		.withConfigurationResource("application.properties")
		.overrideConfigKey("quarkus.hibernate-orm.mapping.timezone.default-storage", "column");

	@Test
	public void schema() {
		assertThat(SchemaUtil.getColumnNames(ormSessionFactory, EntityWithTimezones.class))
			.contains("zonedDateTime_tz", "offsetDateTime_tz", "offsetTime_tz");
		assertThat(SchemaUtil.getColumnTypeName(
			ormSessionFactory,
			EntityWithTimezones.class,
			"zonedDateTime"
		))
			.isEqualTo("TIMESTAMP_UTC");
		assertThat(SchemaUtil.getColumnTypeName(
			ormSessionFactory,
			EntityWithTimezones.class,
			"offsetDateTime"
		))
			.isEqualTo("TIMESTAMP_UTC");
	}

	@Test
	@RunOnVertxContext
	public void persistAndLoad(UniAsserter asserter) {
		assertPersistedThenLoadedValues(
			asserter,
			// Column storage preserves the offset, but not the zone ID: https://hibernate.atlassian.net/browse/HHH-16289
			PERSISTED_ZONED_DATE_TIME.withZoneSameInstant(PERSISTED_ZONED_DATE_TIME.getOffset()),
			PERSISTED_OFFSET_DATE_TIME,
			PERSISTED_OFFSET_TIME
		);
	}

}
