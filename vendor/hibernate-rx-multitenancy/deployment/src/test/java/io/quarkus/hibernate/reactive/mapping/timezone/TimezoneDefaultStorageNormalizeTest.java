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

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.ZoneId;

import io.quarkus.hibernate.reactive.SchemaUtil;
import io.quarkus.test.QuarkusUnitTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class TimezoneDefaultStorageNormalizeTest extends AbstractTimezoneDefaultStorageTest {

    private static final ZoneId JDBC_TIMEZONE = ZoneId.of("America/Los_Angeles");

    @RegisterExtension
    static QuarkusUnitTest TEST = new QuarkusUnitTest()
		.withApplicationRoot((jar) -> jar
			.addClasses(EntityWithTimezones.class)
			.addClasses(SchemaUtil.class))
		.withConfigurationResource("application.properties")
		.overrideConfigKey("quarkus.hibernate-orm.mapping.timezone.default-storage", "normalize")
		.overrideConfigKey("quarkus.hibernate-orm.jdbc.timezone", JDBC_TIMEZONE.getId());

    @Test
    public void schema() {
        assertThat(SchemaUtil.getColumnNames(ormSessionFactory, EntityWithTimezones.class))
			.doesNotContain("zonedDateTime_tz", "offsetDateTime_tz", "offsetTime_tz");
		assertThat(SchemaUtil.getColumnTypeName(
			ormSessionFactory,
			EntityWithTimezones.class,
			"zonedDateTime"
		))
			.isEqualTo("TIMESTAMP");
		assertThat(SchemaUtil.getColumnTypeName(
			ormSessionFactory,
			EntityWithTimezones.class,
			"offsetDateTime"
		))
			.isEqualTo("TIMESTAMP");
    }

    @Test
    @RunOnVertxContext
    public void persistAndLoad(UniAsserter asserter) {
		assertPersistedThenLoadedValues(
			asserter,
			PERSISTED_ZONED_DATE_TIME.withZoneSameInstant(ZoneId.systemDefault()),
			PERSISTED_OFFSET_DATE_TIME.withOffsetSameInstant(
				ZoneId
					.systemDefault()
					.getRules()
					.getOffset(PERSISTED_OFFSET_DATE_TIME.toInstant())),
			PERSISTED_OFFSET_TIME.withOffsetSameInstant(
				ZoneId.systemDefault().getRules().getOffset(Instant.now()))
		);
    }
}
