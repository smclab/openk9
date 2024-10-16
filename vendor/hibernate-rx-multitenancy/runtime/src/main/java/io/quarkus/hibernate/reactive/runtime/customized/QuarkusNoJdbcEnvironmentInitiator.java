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

import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.env.internal.JdbcEnvironmentImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.reactive.provider.service.NoJdbcEnvironmentInitiator;
import org.hibernate.service.spi.ServiceRegistryImplementor;

import java.util.Map;

public class QuarkusNoJdbcEnvironmentInitiator extends NoJdbcEnvironmentInitiator {

    private final Dialect dialect;

    public QuarkusNoJdbcEnvironmentInitiator(Dialect dialect) {
        this.dialect = dialect;
    }

    @Override
    public Class<JdbcEnvironment> getServiceInitiated() {
        return JdbcEnvironment.class;
    }

    @Override
    public JdbcEnvironment initiateService(
        Map configurationValues,
        ServiceRegistryImplementor registry) {
        return new JdbcEnvironmentImpl(registry, dialect);
    }

}
