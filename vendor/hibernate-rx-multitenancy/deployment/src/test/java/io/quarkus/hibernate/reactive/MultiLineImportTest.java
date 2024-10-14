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

import io.quarkus.test.QuarkusUnitTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import jakarta.inject.Inject;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.assertj.core.api.Assertions.assertThat;

public class MultiLineImportTest {

    @RegisterExtension
    static QuarkusUnitTest runner = new QuarkusUnitTest()
            .withApplicationRoot((jar) -> jar
                    .addClass(Hero.class)
                    .addAsResource("complexMultilineImports.sql", "import.sql"))
            .withConfigurationResource("application.properties");

    @Inject
    Mutiny.SessionFactory sessionFactory;

    @Test
    @RunOnVertxContext
    public void integerIdentifierWithStageAPI(UniAsserter asserter) {
        asserter.assertThat(
			() -> sessionFactory.withSession(s -> s
				.createQuery(
					"from Hero h where h.name = :name")
				.setParameter("name", "Galadriel")
				.getResultList()),
                list -> assertThat(list).hasSize(1));
    }

    @Entity(name = "Hero")
    @Table(name = "hero")
    public static class Hero {

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
