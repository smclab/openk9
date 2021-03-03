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

package com.openk9.ingestion.driver.manager.api;

import com.openk9.datasource.model.Datasource;
import com.openk9.http.client.HttpClient;
import com.openk9.json.api.JsonFactory;
import com.openk9.json.api.JsonNode;
import com.openk9.json.api.ObjectNode;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class BasePluginDriver implements PluginDriver {

	@Override
	public Mono<Void> invokeDataParser(
		Datasource datasource, Date fromDate, Date toDate) {
		String jsonConfig = datasource.getJsonConfig();

		JsonNode jsonNode = getJsonFactory().fromJsonToJsonNode(jsonConfig);

		ObjectNode objectNode = jsonNode.toObjectNode();

		ObjectNode requestJson = getJsonFactory().createObjectNode();

		objectNode
			.stream()
			.filter(e -> _containsKey(e.getKey()))
			.forEach(e -> requestJson.set(e.getKey(), e.getValue()));

		requestJson.put("timestamp", fromDate.getTime());

		Map<String, Object> headers = Arrays
			.stream(headers())
			.map(s -> s.split(":"))
			.collect(Collectors.toMap(e -> e[0], e -> e[1]));

		Publisher<byte[]> request = getHttpClient().request(
			method(), path(), requestJson.toString(), headers);

		return Mono.from(request).then();
	}

	private boolean _containsKey(String key) {
		for (String jsonKey : jsonKeys()) {
			if (jsonKey.equals(key)) {
				return true;
			}
		}
		return false;
	}

	protected abstract String[] headers();

	protected abstract String path();

	protected abstract int method();

	protected abstract String[] jsonKeys();

	protected abstract JsonFactory getJsonFactory();

	protected abstract HttpClient getHttpClient();

}