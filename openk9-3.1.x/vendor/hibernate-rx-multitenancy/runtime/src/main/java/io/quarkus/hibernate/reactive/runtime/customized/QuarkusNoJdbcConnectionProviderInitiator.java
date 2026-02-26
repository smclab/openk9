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

package io.quarkus.hibernate.reactive.runtime.customized;

import java.util.Map;

import org.hibernate.boot.registry.StandardServiceInitiator;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.reactive.provider.service.NoJdbcConnectionProvider;
import org.hibernate.service.spi.ServiceRegistryImplementor;

public class QuarkusNoJdbcConnectionProviderInitiator
        implements StandardServiceInitiator<ConnectionProvider> {
    public static final QuarkusNoJdbcConnectionProviderInitiator INSTANCE = new QuarkusNoJdbcConnectionProviderInitiator();

    private QuarkusNoJdbcConnectionProviderInitiator() {
    }

    @Override
    public ConnectionProvider initiateService(
            Map configurationValues,
            ServiceRegistryImplementor registry) {
        return NoJdbcConnectionProvider.INSTANCE;
    }

    @Override
    public Class<ConnectionProvider> getServiceInitiated() {
        return ConnectionProvider.class;
    }
}
