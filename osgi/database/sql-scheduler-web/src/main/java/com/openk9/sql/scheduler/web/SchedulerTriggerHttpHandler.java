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

package com.openk9.sql.scheduler.web;

import com.openk9.http.util.HttpResponseWriter;
import com.openk9.http.web.Endpoint;
import com.openk9.http.web.HttpHandler;
import com.openk9.http.web.HttpRequest;
import com.openk9.http.web.HttpResponse;
import com.openk9.json.api.ArrayNode;
import com.openk9.json.api.JsonFactory;
import com.openk9.json.api.JsonNode;
import com.openk9.json.api.ObjectNode;
import org.apache.karaf.scheduler.Scheduler;
import org.apache.karaf.scheduler.SchedulerError;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Component(
	immediate = true,
	service = Endpoint.class
)
public class SchedulerTriggerHttpHandler implements HttpHandler {

	@Override
	public String getPath() {
		return "/v1/scheduler/trigger";
	}

	@Override
	public int method() {
		return HttpHandler.POST;
	}

	@Override
	public Publisher<Void> apply(
		HttpRequest httpRequest, HttpResponse httpResponse) {

		return _httpResponseWriter.write(
			httpResponse,
			Mono
				.from(httpRequest.aggregateBodyToString())
				.map(_jsonFactory::fromJsonToJsonNode)
				.handle((jsonNode, sink) -> {

					List<Throwable> throwableList = new ArrayList<>();

					ObjectNode objectNode = _jsonFactory.createObjectNode();

					if (jsonNode.isArray()) {

						for (JsonNode node : jsonNode) {

							String jobName = node.asText();

							try {
								objectNode.put(
									jobName, _scheduler.trigger(jobName));
							}
							catch (SchedulerError schedulerError) {
								throwableList.add(schedulerError);
							}

						}

					}

					if (!objectNode.isEmpty()) {

						ArrayNode arrayNode = _jsonFactory.createArrayNode();

						for (Throwable throwable : throwableList) {
							arrayNode.add(throwable.getMessage());
						}

						objectNode.set("errors", arrayNode);

						sink.next(objectNode.toMap());

					}
					else if (!throwableList.isEmpty()){
						sink.error(Exceptions.multiple(throwableList));
					}
					else {
						sink.complete();
					}

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

}
