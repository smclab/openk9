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

package io.openk9.ingestion.queue;

import io.openk9.http.web.HttpHandler;
import io.openk9.http.web.RouterHandler;
import io.openk9.ingestion.identifier.generator.api.IdentifierGenerator;
import io.openk9.ingestion.logic.api.IngestionLogic;
import io.openk9.json.api.JsonFactory;
import io.openk9.model.BinaryPayload;
import io.openk9.model.IngestionPayload;
import io.openk9.model.ResourcesPayload;
import io.openk9.reactor.netty.util.ReactorNettyUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerRoutes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component(
	immediate = true,
	service = RouterHandler.class
)
public class IngestionEndpoint
	implements HttpHandler, RouterHandler {

	@Override
	public HttpServerRoutes handle(HttpServerRoutes router) {
		return router.post("/v1/ingestion/", this);
	}

	@Override
	public Publisher<Void> apply(
		HttpServerRequest httpRequest, HttpServerResponse httpResponse) {


		Mono<String> monoResponse =
			ReactorNettyUtils
				.aggregateBodyAsByteArray(httpRequest)
				.map(body -> _jsonFactory.fromJson(body, IngestionDTO.class))
				.map(dto ->
					IngestionPayload.of(
						_identifierGenerator.create(),
						dto.getDatasourceId(),
						dto.getContentId(),
						dto.getParsingDate(),
						dto.getRawContent(),
						dto.getDatasourcePayload(),
						-1,
						dto.getDatasourcePayload()
							.keySet()
							.toArray(new String[0]),
						_dtoToPayload(dto.getResources())
					)
				)
				.doOnNext(_ingestionLogicSender::send)
				.map(ignore -> "{}");

		return httpResponse.sendString(monoResponse);

	}

	private ResourcesPayload _dtoToPayload(ResourcesDTO resources) {

		List<BinaryDTO> binaries = resources.getBinaries();

		List<BinaryPayload> binaryPayloadList;

		if (binaries == null) {
			binaryPayloadList = List.of();
		}
		else {
			binaryPayloadList =
				binaries
					.stream()
					.map(binaryDTO -> BinaryPayload.of(
						binaryDTO.id, binaryDTO.name, binaryDTO.contentType,
						binaryDTO.data))
					.collect(Collectors.toList());
		}

		return ResourcesPayload.of(binaryPayloadList);
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor(staticName = "of")
	public static class IngestionDTO {
		private long datasourceId;
		private String contentId;
		private long parsingDate;
		private String rawContent;
		private Map<String, Object> datasourcePayload;
		private ResourcesDTO resources;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor(staticName = "of")
	public static class ResourcesDTO {
		private List<BinaryDTO> binaries;
	}


	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor(staticName = "of")
	public static class BinaryDTO {
		private String id;
		private String name;
		private String contentType;
		private String data;
	}

	@Reference
	private JsonFactory _jsonFactory;

	@Reference
	private IngestionLogic _ingestionLogicSender;

	@Reference
	private IdentifierGenerator _identifierGenerator;

}
