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

package io.quarkus.hibernate.reactive.runtime.boot;

import jakarta.persistence.EntityManagerFactory;

import io.quarkus.hibernate.orm.runtime.PersistenceUnitUtil;
import io.quarkus.hibernate.orm.runtime.RuntimeSettings;
import io.quarkus.hibernate.orm.runtime.boot.FastBootEntityManagerFactoryBuilder;
import io.quarkus.hibernate.orm.runtime.migration.MultiTenancyStrategy;
import io.quarkus.hibernate.orm.runtime.recording.PrevalidatedQuarkusMetadata;
import org.hibernate.boot.internal.SessionFactoryOptionsBuilder;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.reactive.session.impl.ReactiveSessionFactoryImpl;

public final class FastBootReactiveEntityManagerFactoryBuilder
	extends FastBootEntityManagerFactoryBuilder {

    public FastBootReactiveEntityManagerFactoryBuilder(
		PrevalidatedQuarkusMetadata metadata,
		String persistenceUnitName,
		StandardServiceRegistry standardServiceRegistry,
		RuntimeSettings runtimeSettings,
		Object validatorFactory,
            Object cdiBeanManager) {
        super(metadata,
			persistenceUnitName,
			standardServiceRegistry,
			runtimeSettings,
			validatorFactory,
			cdiBeanManager,
			MultiTenancyStrategy.SCHEMA
		);
    }

    @Override
    public EntityManagerFactory build() {
        try {
			final SessionFactoryOptionsBuilder optionsBuilder =
				metadata.buildSessionFactoryOptionsBuilder();
            optionsBuilder.enableCollectionInDefaultFetchGroup(true);
            populate(
				PersistenceUnitUtil.DEFAULT_PERSISTENCE_UNIT_NAME,
				optionsBuilder,
				standardServiceRegistry
			);
            SessionFactoryOptions options = optionsBuilder.buildOptions();
            return new ReactiveSessionFactoryImpl(
				metadata,
				options,
				metadata.getBootstrapContext()
			);
		}
		catch (Exception e) {
            throw persistenceException("Unable to build Hibernate Reactive SessionFactory", e);
        }
    }
}
