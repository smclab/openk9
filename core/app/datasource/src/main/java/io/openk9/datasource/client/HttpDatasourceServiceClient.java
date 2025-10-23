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

package io.openk9.datasource.client;

import io.openk9.datasource.enricher.HttpEnricherInfo;
import io.openk9.datasource.model.form.FormTemplate;
import io.openk9.datasource.web.dto.PluginDriverHealthDTO;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;

public abstract class HttpDatasourceServiceClient {

    @GET
    @Produces("application/json")
    public abstract Uni<FormTemplate> getForm(HttpEnricherInfo enricherInfo);

    @GET
    @Produces("application/json")
    public abstract Uni<PluginDriverHealthDTO> getHealth(HttpEnricherInfo enricherInfo);

}
