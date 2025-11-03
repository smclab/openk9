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

import io.openk9.datasource.web.dto.ResourceUriDTO;
import io.openk9.datasource.model.form.FormTemplate;
import io.openk9.datasource.web.dto.HealthDTO;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpMethod;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.WebClient;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;

public abstract class HttpDatasourceServiceClient {

    @Inject
    WebClient webClient;
    @Inject
    Validator validator;
    
    public Uni<FormTemplate> getForm(ResourceUriDTO resourceUriDTO) {
        return webClient
                .requestAbs(
                        HttpMethod.GET,
                        resourceUriDTO.getBaseUri() + resourceUriDTO.getPath()
                )
                .send()
                .flatMap(this::validateResponse)
                .map(res -> res.bodyAsJson(FormTemplate.class))
                .flatMap(this::validateDto);
    }

    public Uni<HealthDTO> getHealth(ResourceUriDTO resourceUriDTO) {
        return webClient
                .requestAbs(
                        HttpMethod.GET,
                        resourceUriDTO.getBaseUri() + resourceUriDTO.getPath()
                )
                .send()
                .flatMap(this::validateResponse)
                .map(res -> res.bodyAsJson(HealthDTO.class))
                .flatMap(this::validateDto)
                .onFailure(ConstraintViolationException.class)
                .recoverWithItem(HealthDTO
                        .builder()
                        .status(HealthDTO.Status.UNKOWN)
                        .build()
                );
    }

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

    protected  <T> Uni<T> validateDto(T dto) {
        var violations = validator.validate(dto);
        if (violations.isEmpty()) {
            return Uni.createFrom().item(dto);
        }
        else {
            return Uni.createFrom().failure(new ConstraintViolationException(violations));
        }
    }

}
