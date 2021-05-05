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

import io.openk9.http.web.Endpoint;
import io.openk9.http.web.HttpHandler;
import io.openk9.http.web.HttpRequest;
import io.openk9.http.web.HttpResponse;
import io.openk9.ingestion.logic.api.IngestionLogic;
import io.openk9.ingestion.queue.exception.AttributeException;
import io.openk9.json.api.JsonFactory;
import io.openk9.model.IngestionPayload;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component(
	immediate = true,
	service = Endpoint.class,
	property = {
		"base.path=/v1/ingestion"
	}
)
public class IngestionEndpoint implements HttpHandler {

	@Override
	public String getPath() {
		return "/";
	}

	@Override
	public int method() {
		return POST;
	}

	@Override
	public Publisher<Void> apply(
		HttpRequest httpRequest, HttpResponse httpResponse) {

		Mono<String> monoResponse =
			Mono.from(httpRequest.bodyAttributesFirst())
				.<Map<String, String>>handle((map, synchronousSink) -> {

					for (String attributeKey : _requiredAttributeKeys) {
						if (!map.containsKey(attributeKey)) {
							synchronousSink
								.error(
									new AttributeException(
										"request required attribute "
										+ attributeKey));
							return;
						}
					}

					synchronousSink.next(map);

				})
				.map(map -> {

					String datasourceIdAttribute = map.get("datasourceId");
					String contentId = map.get("contentId");
					String parsingDateAttribute = map.get("parsingDate");
					String rawContent = map.get("rawContent");
					String datasourcePayload = map.get("datasourcePayload");

					long datasourceId =
						Long.parseLong(datasourceIdAttribute);

					long parsingDate =
						Long.parseLong(parsingDateAttribute);

					Map<String, Object> datasourcePayloadMap = _jsonFactory
						.fromJsonToJsonNode(datasourcePayload)
						.toObjectNode()
						.toMap();

					return IngestionPayload.of(
							datasourceId,
							contentId,
							parsingDate,
							rawContent,
							datasourcePayloadMap,
							-1,
							datasourcePayloadMap
								.keySet()
								.toArray(new String[0])
						);
				})
				.doOnNext(_ingestionLogicSender::send)
				.map(ignore -> "{}")
				.onErrorResume(
					e -> Mono.just(
						httpResponse.status(500, _handleError(e))));

		return httpResponse.sendString(monoResponse);

	}

	private String _handleError(Throwable e) {
		return
			"{" +
			"\"errorMessage\":\"" + e.getMessage() + "\"," +
			"\"errorClassName\":\"" + e.getClass().getName() + "\"" +
			"}";
	}

	private final String[] _requiredAttributeKeys = {
		"datasourceId",
		"contentId",
		"parsingDate",
		"rawContent",
		"datasourcePayload"
	};

	@Reference
	private JsonFactory _jsonFactory;

	@Reference
	private IngestionLogic _ingestionLogicSender;

}
