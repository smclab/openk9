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

package io.openk9.ingestion.driver.manager.internal.web.handler;

import io.openk9.datasource.repository.DatasourceRepository;
import io.openk9.datasource.repository.TenantRepository;
import io.openk9.http.util.HttpResponseWriter;
import io.openk9.http.util.HttpUtil;
import io.openk9.http.web.Endpoint;
import io.openk9.http.web.HttpHandler;
import io.openk9.http.web.HttpRequest;
import io.openk9.http.web.HttpResponse;
import io.openk9.ingestion.driver.manager.internal.web.request.ReindexRequest;
import io.openk9.ingestion.driver.manager.internal.web.response.ReindexResponse;
import io.openk9.json.api.JsonFactory;
import io.openk9.sql.api.client.Criteria;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

		return _httpResponseWriter.write(
			httpResponse,
				HttpUtil
					.mapBodyRequest(
						httpRequest, body -> _jsonFactory.fromJson(
							body, ReindexRequest.class))
					.map(ReindexRequest::getDatasourceIds)
					.flatMapMany(ids -> _datasourceRepository.findBy(
						Criteria.where("datasourceId").in(ids)
					))
				.flatMap(ds -> {
						ds.setLastIngestionDate(Instant.EPOCH);
						return _datasourceRepository.update(ds);
				})
				.map(ds -> ReindexResponse
					.of(ds.getDatasourceId(), true)
				)
				.doOnError(throwable -> {
					if (_log.isErrorEnabled()) {
						_log.error(throwable.getMessage(), throwable);
					}
				})
		);

	}

	@Reference
	private DatasourceRepository _datasourceRepository;

	@Reference
	private TenantRepository _tenantRepository;

	@Reference
	private JsonFactory _jsonFactory;

	@Reference(
		target = "(type=json)"
	)
	private HttpResponseWriter _httpResponseWriter;

	private static final Logger _log = LoggerFactory.getLogger(
		ReindexHttpHandler.class.getName());

}
