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

import java.util.List;
import java.util.Map;

import io.quarkus.hibernate.orm.runtime.HibernateOrmRuntimeConfig;
import io.quarkus.hibernate.orm.runtime.SingletonPersistenceProviderResolver;
import io.quarkus.hibernate.orm.runtime.integration.HibernateOrmIntegrationRuntimeDescriptor;

public final class ReactivePersistenceProviderSetup {

    private ReactivePersistenceProviderSetup() {
        // not to be constructed
    }

    public static void registerStaticInitPersistenceProvider() {
        jakarta.persistence.spi.PersistenceProviderResolverHolder
                .setPersistenceProviderResolver(new StaticInitHibernateReactivePersistenceProviderResolver());
    }

    public static void registerRuntimePersistenceProvider(
            HibernateOrmRuntimeConfig hibernateOrmRuntimeConfig,
            Map<String, List<HibernateOrmIntegrationRuntimeDescriptor>> integrationRuntimeDescriptors) {
        jakarta.persistence.spi.PersistenceProviderResolverHolder
                .setPersistenceProviderResolver(
                        new SingletonPersistenceProviderResolver(
                                new FastBootHibernateReactivePersistenceProvider(
                                        hibernateOrmRuntimeConfig,
                                        integrationRuntimeDescriptors)));
    }

}
