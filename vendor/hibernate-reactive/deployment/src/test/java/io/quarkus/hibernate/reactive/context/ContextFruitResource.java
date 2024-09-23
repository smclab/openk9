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

package io.quarkus.hibernate.reactive.context;

import io.quarkus.vertx.core.runtime.context.VertxContextSafetyToggle;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.resteasy.reactive.RestPath;

@Path("contextTest")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class ContextFruitResource {

    @Inject
    Mutiny.SessionFactory sf;

    @GET
    @Path("valid")
    public Uni<Fruit> get() {
        return sf.withTransaction((s, t) -> s.find(Fruit.class, 1));
    }

    @GET
    @Path("invalid")
    public Uni<Fruit> getSingle(@RestPath Integer id) {
        VertxContextSafetyToggle.setCurrentContextSafe(false);
        return get();
    }

}
