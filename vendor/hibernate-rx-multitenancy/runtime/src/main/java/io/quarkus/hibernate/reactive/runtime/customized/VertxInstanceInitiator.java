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

import io.vertx.core.Vertx;
import org.hibernate.boot.registry.StandardServiceInitiator;
import org.hibernate.reactive.vertx.VertxInstance;
import org.hibernate.reactive.vertx.impl.ProvidedVertxInstance;
import org.hibernate.service.spi.ServiceRegistryImplementor;

import java.util.Map;

public class VertxInstanceInitiator implements StandardServiceInitiator<VertxInstance> {

    private final Vertx vertx;

    public VertxInstanceInitiator(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public VertxInstance initiateService(
        Map map,
        ServiceRegistryImplementor serviceRegistryImplementor) {
        return new ProvidedVertxInstance(vertx);
    }

    @Override
    public Class<VertxInstance> getServiceInitiated() {
        return VertxInstance.class;
    }

}
