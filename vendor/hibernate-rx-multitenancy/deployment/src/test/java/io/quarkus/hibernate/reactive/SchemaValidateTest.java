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

package io.quarkus.hibernate.reactive;

import jakarta.inject.Inject;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import io.quarkus.test.QuarkusUnitTest;
import org.hibernate.reactive.mutiny.Mutiny;
import org.hibernate.tool.schema.spi.SchemaManagementException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class SchemaValidateTest {

    @RegisterExtension
    static QuarkusUnitTest runner = new QuarkusUnitTest()
		.withApplicationRoot((jar) -> jar
			.addClass(Hero.class))
		.withConfigurationResource("application.properties")
		.assertException(SchemaValidateTest::isSchemaValidationException)
		.overrideConfigKey("quarkus.hibernate-orm.database.generation", "validate");

    @Inject
    Mutiny.SessionFactory sessionFactory;

    @Test
    public void testSchemaValidationException() {
        Assertions.fail("We expect an exception because the db is empty");
    }

    private static void isSchemaValidationException(Throwable t) {
        Throwable cause = t;
		while (cause != null &&
			   !cause.getClass().getName().equals(SchemaManagementException.class.getName())) {
            cause = cause.getCause();
        }
        String causeName = cause != null ? cause.getClass().getName() : null;
        Assertions.assertEquals(SchemaManagementException.class.getName(), causeName);
		Assertions.assertTrue(cause
			.getMessage()
			.contains("Schema-validation: missing table [" + Hero.TABLE + "]"));
    }

    @Entity(name = "Hero")
    @Table(name = Hero.TABLE)
    public static class Hero {

        public static final String TABLE = "Hero_for_validation";

        @jakarta.persistence.Id
        @jakarta.persistence.GeneratedValue
        public java.lang.Long id;

        @Column(unique = true)
        public String name;

        public String otherName;

        public int level;

        public String picture;

        @Column(columnDefinition = "TEXT")
        public String powers;

    }
}
