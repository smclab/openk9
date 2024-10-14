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

import io.quarkus.test.vertx.UniAsserter;
import jakarta.inject.Inject;
import org.assertj.core.api.SoftAssertions;
import org.hibernate.SessionFactory;
import org.hibernate.reactive.mutiny.Mutiny;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class AbstractTimezoneDefaultStorageTest {

    private static final LocalDateTime LOCAL_DATE_TIME_TO_TEST = LocalDateTime.of(
		2017,
		Month.NOVEMBER,
		6,
		19,
		19,
		0
	);
    public static final ZonedDateTime PERSISTED_ZONED_DATE_TIME = LOCAL_DATE_TIME_TO_TEST.atZone(
		ZoneId.of("Africa/Cairo"));
	public static final OffsetDateTime PERSISTED_OFFSET_DATE_TIME =
		LOCAL_DATE_TIME_TO_TEST.atOffset(ZoneOffset.ofHours(3));
    public static final OffsetTime PERSISTED_OFFSET_TIME = LOCAL_DATE_TIME_TO_TEST.toLocalTime()
		.atOffset(ZoneOffset.ofHours(3));

    @Inject
    SessionFactory ormSessionFactory;
    // This is an ORM SessionFactory, but it's backing Hibernate Reactive.

    @Inject
    Mutiny.SessionFactory sessionFactory;

    protected void assertPersistedThenLoadedValues(
		UniAsserter asserter, ZonedDateTime expectedZonedDateTime,
		OffsetDateTime expectedOffsetDateTime, OffsetTime expectedOffsetTime) {
        asserter.assertThat(
			() -> sessionFactory.withTransaction(session -> {
                    var entity = new EntityWithTimezones(
						PERSISTED_ZONED_DATE_TIME,
						PERSISTED_OFFSET_DATE_TIME,
						PERSISTED_OFFSET_TIME
					);
                    return session.persist(entity).replaceWith(() -> entity.id);
                })
				.chain(id -> sessionFactory.withTransaction(session -> session.find(
					EntityWithTimezones.class,
					id
				))),
			entity -> {
				SoftAssertions.assertSoftly(assertions -> {
					assertions.assertThat(entity).extracting("zonedDateTime").isEqualTo(
						expectedZonedDateTime);
					assertions.assertThat(entity).extracting("offsetDateTime").isEqualTo(
						expectedOffsetDateTime);
					assertions.assertThat(entity).extracting("offsetTime").isEqualTo(
						expectedOffsetTime);
				});
			}
		);
    }

}
