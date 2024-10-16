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

import org.hibernate.boot.registry.StandardServiceInitiator;
import org.hibernate.reactive.context.Context;
import org.hibernate.service.spi.ServiceRegistryImplementor;

import java.util.Map;

/**
 * Custom Quarkus initiator for the {@link Context} service; this
 * one creates instances of {@link CheckingVertxContext}.
 */
public class CheckingVertxContextInitiator implements StandardServiceInitiator<Context> {

    public static final CheckingVertxContextInitiator INSTANCE =
        new CheckingVertxContextInitiator();

    @Override
    public Context initiateService(Map configurationValues, ServiceRegistryImplementor registry) {
        return new CheckingVertxContext();
    }

    @Override
    public Class<Context> getServiceInitiated() {
        return Context.class;
    }

}
