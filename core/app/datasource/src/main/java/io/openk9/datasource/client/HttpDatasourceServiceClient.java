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
import io.openk9.datasource.web.dto.HealthDTO;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import jakarta.validation.ValidationException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;

public abstract class HttpDatasourceServiceClient {

    @GET
    @Produces("application/json")
    public abstract Uni<FormTemplate> getForm(HttpEnricherInfo enricherInfo);

    @GET
    @Produces("application/json")
    public abstract Uni<HealthDTO> getHealth(HttpEnricherInfo enricherInfo);

    protected Uni<HttpResponse<Buffer>> validateResponse(HttpResponse<Buffer> response) {
        if (response.statusCode() >= 200 && response.statusCode() <= 299) {
            return Uni.createFrom().item(response);
        }
        else {
            return Uni.createFrom().failure(new ValidationException(
                    String.format(
                            "Unexpected Response Status: %d, Message: %s",
                            response.statusCode(),
                            response.statusMessage()
                    ))
            );
        }
    }

}
