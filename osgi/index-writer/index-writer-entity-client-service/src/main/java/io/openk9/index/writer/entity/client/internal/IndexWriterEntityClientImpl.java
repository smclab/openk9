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

package io.openk9.index.writer.entity.client.internal;

import io.openk9.http.client.HttpClient;
import io.openk9.http.client.HttpClientFactory;
import io.openk9.http.web.HttpHandler;
import io.openk9.index.writer.entity.client.api.IndexWriterEntityClient;
import io.openk9.index.writer.entity.model.DocumentEntityRequest;
import io.openk9.index.writer.entity.model.DocumentEntityResponse;
import io.openk9.json.api.JsonFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Component(
	immediate = true,
	service = IndexWriterEntityClient.class
)
public class IndexWriterEntityClientImpl implements IndexWriterEntityClient {

	@interface Config {
		String url() default "http://index-writer";
	}

	@Activate
	void activate(Config config) {

		String url = config.url();

		_indexWriterHttpClient = _httpClientFactory.getHttpClient(url);
	}

	@Modified
	void modified(Config config) {

		deactivate();

		activate(config);

	}

	@Deactivate
	void deactivate() {
		_indexWriterHttpClient = null;
	}

	@Override
	public Mono<Void> insertEntity(DocumentEntityRequest documentEntityRequest) {
		return insertEntities(List.of(documentEntityRequest));
	}

	@Override
	public Mono<Void> insertEntities(
		Collection<DocumentEntityRequest> documentEntityRequestList) {
		return Mono
			.from(
				_indexWriterHttpClient
					.request(
						HttpHandler.POST,
						"/v1/",
						_jsonFactory.toJson(documentEntityRequestList),
						Map.of()
					)
			)
			.then();
	}

	@Override
	public Mono<List<DocumentEntityResponse>> getEntities(long tenantId, Map<String, Object> request) {
		return Mono
			.from(
				_indexWriterHttpClient
					.request(
						HttpHandler.POST,
						"/v1/get-entities/" + tenantId,
						_jsonFactory.toJson(request),
						Map.of()
					)
			)
			.map(bytes -> _jsonFactory.fromJsonList(bytes, DocumentEntityResponse.class));
	}

	private HttpClient _indexWriterHttpClient;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private JsonFactory _jsonFactory;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private HttpClientFactory _httpClientFactory;
}
