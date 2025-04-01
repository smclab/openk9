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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import jakarta.inject.Inject;

import io.quarkus.hibernate.reactive.SchemaUtil;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.hibernate.SessionFactory;
import org.hibernate.id.OptimizableGenerator;
import org.hibernate.id.enhanced.Optimizer;
import org.hibernate.id.enhanced.PooledLoOptimizer;
import org.hibernate.id.enhanced.PooledOptimizer;
import org.hibernate.reactive.id.impl.ReactiveGeneratorWrapper;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.Test;

public abstract class AbstractIdOptimizerDefaultTest {

    @Inject
	SessionFactory ormSessionFactory;
	// This is an ORM SessionFactory, but it's backing Hibernate Reactive.

    @Inject
    Mutiny.SessionFactory sessionFactory;

    @Test
    @RunOnVertxContext
    public void ids(UniAsserter asserter) {
        for (long i = 1; i <= 51; i++) {
            long expectedId = i;
            // Apparently, we can rely on assertions being executed in order.
			asserter.assertThat(
				() -> sessionFactory.withTransaction(s -> {
					var entity = new EntityWithSequenceGenerator();
					return s.persist(entity).replaceWith(() -> entity.id);
				}),
				id -> assertThat(id).isEqualTo(expectedId)
			);
        }
    }

    @Test
    public void defaults() {
        assertThat(List.of(
			EntityWithDefaultGenerator.class,
			EntityWithGenericGenerator.class,
			EntityWithSequenceGenerator.class,
			EntityWithTableGenerator.class
		))
			.allSatisfy(c -> assertOptimizer(c).isInstanceOf(defaultOptimizerType()));
    }

    @Test
    public void explicitOverrides() {
        assertOptimizer(EntityWithGenericGeneratorAndPooledOptimizer.class)
			.isInstanceOf(PooledOptimizer.class);
        assertOptimizer(EntityWithGenericGeneratorAndPooledLoOptimizer.class)
			.isInstanceOf(PooledLoOptimizer.class);
    }

	abstract Class<?> defaultOptimizerType();

    AbstractObjectAssert<?, Optimizer> assertOptimizer(Class<?> entityType) {
        return assertThat(SchemaUtil.getGenerator(ormSessionFactory, entityType))
			.as("Reactive ID generator wrapper for entity type " + entityType.getSimpleName())
			.asInstanceOf(InstanceOfAssertFactories.type(ReactiveGeneratorWrapper.class))
			.extracting("generator") // Needs reflection, unfortunately the blocking generator is not exposed...
			.as("Blocking ID generator for entity type " + entityType.getSimpleName())
			.asInstanceOf(InstanceOfAssertFactories.type(OptimizableGenerator.class))
			.extracting(OptimizableGenerator::getOptimizer)
			.as("ID optimizer for entity type " + entityType.getSimpleName());
    }
}
