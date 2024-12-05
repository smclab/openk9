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

package io.quarkus.hibernate.reactive.runtime;

import io.quarkus.arc.DefaultBean;
import io.quarkus.arc.Unremovable;
import io.quarkus.hibernate.orm.runtime.JPAConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.Typed;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import org.hibernate.reactive.common.spi.Implementor;
import org.hibernate.reactive.mutiny.Mutiny;
import org.hibernate.reactive.mutiny.impl.MutinySessionFactoryImpl;

public class ReactiveSessionFactoryProducer {

    @Inject
    @PersistenceUnit
    EntityManagerFactory emf;

    @Inject
    JPAConfig jpaConfig;

    @Produces
    @ApplicationScoped
    @DefaultBean
    @Unremovable
	@Typed({Mutiny.SessionFactory.class, Implementor.class})
    public MutinySessionFactoryImpl mutinySessionFactory() {
        if (jpaConfig.getDeactivatedPersistenceUnitNames()
                .contains(HibernateReactive.DEFAULT_REACTIVE_PERSISTENCE_UNIT_NAME)) {
            throw new IllegalStateException(
                    "Cannot retrieve the Mutiny.SessionFactory for persistence unit "
                            + HibernateReactive.DEFAULT_REACTIVE_PERSISTENCE_UNIT_NAME
                            + ": Hibernate Reactive was deactivated through configuration properties");
        }
        return (MutinySessionFactoryImpl) emf.unwrap(Mutiny.SessionFactory.class);
    }

}
