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

package io.openk9.searcher.resource;

import java.util.List;
import java.util.Map;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jboss.resteasy.reactive.server.jaxrs.HttpHeadersImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SearchResourceTest {

	private static final JsonObject SOURCE_JSON = (JsonObject) Json.decodeValue("""
		{
			"ingestionId": "7f48650e-83d9-4f7f-b185-6f4b9e0d773e",
			"datasourceId": 182,
			"contentId": "9923041763992700",
			"parsingDate": 1713189821167,
			"rawContent": "Lorem ipsum... ",
			"tenantId": "sobble",
			"documentTypes": [
				"web",
				"file"
			],
			"resources": {
				"binaries": []
			},
			"acl": {
				"public": true
			},
			"indexName": "182-data-dd6c4252-224c-4d23-beab-46b3e0bfbba2",
			"last": false,
			"scheduleId": "dd6c4252-224c-4d23-beab-46b3e0bfbba2",
			"oldIndexName": null,
			"type": null,
			"file": {
				"area": {
					"base": "data-sheet",
					"i18n": {
						"it_IT": " scheda dati"
					}
				},
				"tags": {
					"base": [
						"tesla",
						"panasonic",
						"batteria al litio",
						"co2",
						"emissione"
					],
					"i18n": {
						"it_IT": [
							true,
							"panasonic",
							"batteria al litio",
							2,
							"emissione"
						]
					}
				}
			},
			"web": {
				"url": "https://www.acme.com/en/battery-investment",
				"title": {
					"base": "Investing for Sustainable Batteries ｜ Energy Solution",
					"i18n": {
						"it_IT": " Investire in batterie sostenibili ｜ Energy Solution"
					}
				},
				"content": {
					"base": "Lorem ipsum...",
					"i18n": {
						"it_IT": "Lorem ipsum..."
					}
				},
				"favicon": null
			}
		}
		""");


	@Test
	void mapI18nFields() {
		var sourceMap = getSourceMap();

		Assertions.assertDoesNotThrow(() -> SearchResource.mapI18nFields(sourceMap));

		var file = (Map<String, Object>) sourceMap.get("file");
		Assertions.assertInstanceOf(List.class, file.get("tags"));

		var valueList = (List) file.get("tags");
		var item = valueList.iterator().next();
		Assertions.assertInstanceOf(String.class, item);

	}

	@Test
	void should_get_rawToken() {
		var s = "Bearer is-a-bearer-token";

		var headersMap = Map.of(
			"Authorization", s,
			"anotherHeader", "anotherValue"
		);

		var headers = new HttpHeadersImpl(headersMap.entrySet());

		var rawToken = SearchResource.getRawToken(headers);

		Assertions.assertEquals("is-a-bearer-token", rawToken);

	}

	private Map<String, Object> getSourceMap() {
		var sourceMap = SOURCE_JSON.copy().getMap();

		fromJsonObjectToJavaCollections(sourceMap);

		return sourceMap;
	}

	private void fromJsonObjectToJavaCollections(Map<String, Object> sourceMap) {
		for (Map.Entry<String, Object> entry : sourceMap.entrySet()) {
			if (entry.getValue() instanceof JsonObject) {
				var asMap = ((JsonObject) entry.getValue()).getMap();
				fromJsonObjectToJavaCollections(asMap);
				entry.setValue(asMap);
			}
			if (entry.getValue() instanceof JsonArray) {
				var asList = ((JsonArray) entry.getValue()).getList();
				entry.setValue(asList);
			}
		}
	}

}