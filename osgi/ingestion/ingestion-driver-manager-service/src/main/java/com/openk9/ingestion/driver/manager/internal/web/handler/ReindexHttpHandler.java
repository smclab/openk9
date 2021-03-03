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

package com.openk9.ingestion.driver.manager.internal.web.handler;

import com.openk9.ingestion.driver.manager.internal.web.request.ReindexRequest;
import com.openk9.ingestion.driver.manager.api.PluginDriverRegistry;
import com.openk9.ingestion.driver.manager.internal.web.response.ReindexResponse;
import com.openk9.datasource.repository.DatasourceRepository;
import com.openk9.datasource.repository.TenantRepository;
import com.openk9.http.util.HttpResponseWriter;
import com.openk9.http.util.HttpUtil;
import com.openk9.http.web.Endpoint;
import com.openk9.http.web.HttpHandler;
import com.openk9.http.web.HttpRequest;
import com.openk9.http.web.HttpResponse;
import com.openk9.json.api.JsonFactory;
import com.openk9.search.client.api.ReactorActionListener;
import com.openk9.search.client.api.RestHighLevelClientProvider;
import com.openk9.sql.api.client.Criteria;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component(
	immediate = true,
	service = Endpoint.class
)
public class ReindexHttpHandler implements HttpHandler {

	@Override
	public String getPath() {
		return "/v1/index/reindex";
	}

	@Override
	public int method() {
		return POST;
	}

	@Override
	public Publisher<Void> apply(
		HttpRequest httpRequest, HttpResponse httpResponse) {

		String hostName = HttpUtil.getHostName(httpRequest);

		return _httpResponseWriter.write(
			httpResponse,
			_tenantRepository
				.findByVirtualHost(hostName)
				.switchIfEmpty(
					Mono.error(
						() -> new RuntimeException(
							"tenant not found for virtualhost: " + hostName)))
				.flatMapMany(t -> HttpUtil
					.mapBodyRequest(
						httpRequest, body -> _jsonFactory.fromJson(
							body, ReindexRequest.class))
					.map(ReindexRequest::getDatasourceIds)
					.flatMapMany(ids -> _datasourceRepository.findBy(
						Criteria
							.where("tenantId").is(t.getTenantId())
							.and("datasourceId").in(ids))
					))
				.flatMap(datasource -> {

					String indexName =
						datasource.getTenantId() +
						"-" +
						_pluginDriverRegistry
							.getPluginDriver(
								datasource.getDriverServiceName())
							.get()
							.getName() +
						"-data";

					DeleteIndexRequest deleteIndexRequest =
						new DeleteIndexRequest(indexName);

					return Mono
						.<AcknowledgedResponse>create(
							sink -> _restHighLevelClientProvider
								.get()
								.indices()
								.deleteAsync(
									deleteIndexRequest, RequestOptions.DEFAULT,
									new ReactorActionListener<>(sink)))
						.doOnError(throwable -> {
							if (_log.isWarnEnabled()) {
								_log.warn(throwable.getMessage());
							}
						})
						.onErrorReturn(_NOTHING)
						.then(
							Mono
								.just(datasource)
								.flatMap(ds -> {

									ds.setLastIngestionDate(Instant.EPOCH);

									return _datasourceRepository.update(ds);
								})
						)
						.thenReturn(
							ReindexResponse
								.of(
									datasource.getDatasourceId(),
									true)
						)
						.doOnError(throwable -> {
							if (_log.isErrorEnabled()) {
								_log.error(throwable.getMessage(), throwable);
							}
						})
						.onErrorReturn(
							ReindexResponse
								.of(
									datasource.getDatasourceId(),
									false)
						);

				})
		);

	}

	private void _manageException(Throwable throwable, Object object) {

		if (_log.isErrorEnabled()) {
			if (object == null) {
				_log.error(throwable.getMessage(), throwable);
			}
			else {
				_log.error(
					"error on object: { " + object.toString() + " }",
					throwable);
			}
		}

	}

	@Reference
	private DatasourceRepository _datasourceRepository;

	@Reference
	private TenantRepository _tenantRepository;

	@Reference
	private RestHighLevelClientProvider _restHighLevelClientProvider;

	@Reference
	private JsonFactory _jsonFactory;

	@Reference(
		target = "(type=json)"
	)
	private HttpResponseWriter _httpResponseWriter;

	@Reference
	private PluginDriverRegistry _pluginDriverRegistry;

	private static final Logger _log = LoggerFactory.getLogger(
		ReindexHttpHandler.class.getName());

	private static final AcknowledgedResponse _NOTHING =
		new AcknowledgedResponse(false);

}
