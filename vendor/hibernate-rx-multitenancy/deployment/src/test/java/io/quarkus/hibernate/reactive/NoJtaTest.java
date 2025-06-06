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

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import io.quarkus.test.QuarkusUnitTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.transaction.jta.platform.spi.JtaPlatform;
import org.hibernate.reactive.mutiny.Mutiny;
import org.hibernate.resource.transaction.spi.TransactionCoordinatorBuilder;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class NoJtaTest {

    @RegisterExtension
    static QuarkusUnitTest runner = new QuarkusUnitTest()
		.withApplicationRoot((jar) -> jar
			.addClass(MyEntity.class)
			.addAsResource("application.properties"));

    @Inject
	SessionFactory sessionFactory;
	// This is an ORM SessionFactory, but it's backing Hibernate Reactive.

    @Inject
    Mutiny.SessionFactory factory;

    @Test
    @RunOnVertxContext
    public void test(UniAsserter asserter) {
		ServiceRegistryImplementor serviceRegistry =
			sessionFactory.unwrap(SessionFactoryImplementor.class)
                .getServiceRegistry();

        // Two assertions are necessary, because these values are influenced by separate configuration
		assertThat(serviceRegistry
			.getService(JtaPlatform.class)
			.retrieveTransactionManager()).isNull();
		assertThat(serviceRegistry
			.getService(TransactionCoordinatorBuilder.class)
			.isJta()).isFalse();

        // Quick test to make sure HRX works
        MyEntity entity = new MyEntity("default");

		asserter.assertThat(
			() -> factory.withTransaction((session, tx) -> session.persist(entity))
				.chain(() -> factory.withTransaction((session, tx) -> session
					.clear()
					.find(MyEntity.class, entity.getId()))),
			retrievedEntity -> assertThat(retrievedEntity).isNotSameAs(entity).returns(
				entity.getName(),
				MyEntity::getName
			)
		);
    }

    @Entity
    public static class MyEntity {

        private long id;

        private String name;

        public MyEntity() {
        }

        public MyEntity(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + ":" + name;
        }

        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "defaultSeq")
        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
