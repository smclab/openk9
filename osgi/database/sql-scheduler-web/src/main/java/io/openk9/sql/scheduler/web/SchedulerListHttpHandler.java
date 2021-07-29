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

package io.openk9.sql.scheduler.web;

import io.openk9.datasource.repository.DatasourceRepository;
import io.openk9.http.util.HttpResponseWriter;
import io.openk9.http.web.HttpHandler;
import io.openk9.http.web.RouterHandler;
import io.openk9.json.api.JsonFactory;
import io.openk9.json.api.ObjectNode;
import io.openk9.model.Datasource;
import io.openk9.sql.scheduler.web.exception.SchedulerException;
import org.apache.karaf.scheduler.ScheduleOptions;
import org.apache.karaf.scheduler.Scheduler;
import org.apache.karaf.scheduler.SchedulerError;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerRoutes;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

@Component(
	immediate = true,
	service = RouterHandler.class
)
public class SchedulerListHttpHandler
	implements RouterHandler, HttpHandler {

	@Override
	public HttpServerRoutes handle(HttpServerRoutes router) {
		return router.get("/v1/scheduler", this);
	}

	@Override
	public Publisher<Void> apply(
		HttpServerRequest httpRequest, HttpServerResponse httpResponse) {

		return _httpResponseWriter.write(
			httpResponse,
			Mono.defer(() -> {

				Map<String, ScheduleOptions> jobs;

				try {
					jobs = _scheduler.getJobs();
				}
				catch (SchedulerError schedulerError) {
					throw new SchedulerException(schedulerError);
				}

				Set<String> jobNames = jobs.keySet();

				Function<Datasource, Optional<String>> findJobName =
					datasource -> jobNames
						.stream()
						.filter(e -> e.endsWith("-" + datasource.getName()))
						.findFirst();

				return _datasourceRepository
					.findAll(true)
					.concatMap(datasource -> {

						Optional<String> jobNameOptional =
							findJobName.apply(datasource);

						if (jobNameOptional.isPresent()) {

							ObjectNode objectNode =
								_jsonFactory.createObjectNode();

							objectNode.put(
								"datasourceId", datasource.getDatasourceId());

							objectNode.put(
								"scheduling", datasource.getScheduling());

							objectNode.put(
								"datasourceName", datasource.getName());

							objectNode.put("jobName", jobNameOptional.get());

							return Mono.just(objectNode.toMap());

						}
						else {
							return Mono.empty();
						}

					})
					.collectList();
			})
		);
	}

	@Reference(
		target = "(type=json)"
	)
	private HttpResponseWriter _httpResponseWriter;

	@Reference
	private JsonFactory _jsonFactory;

	@Reference
	private Scheduler _scheduler;

	@Reference
	private DatasourceRepository _datasourceRepository;

}
