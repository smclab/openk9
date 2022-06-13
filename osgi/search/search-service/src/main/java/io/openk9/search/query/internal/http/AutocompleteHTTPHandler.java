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

package io.openk9.search.query.internal.http;

import io.openk9.datasource.client.api.DatasourceClient;
import io.openk9.http.util.HttpResponseWriter;
import io.openk9.http.util.HttpUtil;
import io.openk9.http.web.RouterHandler;
import io.openk9.json.api.JsonFactory;
import io.openk9.model.Datasource;
import io.openk9.model.Tenant;
import io.openk9.plugin.driver.manager.client.api.PluginDriverManagerClient;
import io.openk9.plugin.driver.manager.model.DocumentTypeDTO;
import io.openk9.plugin.driver.manager.model.PluginDriverDTO;
import io.openk9.plugin.driver.manager.model.PluginDriverDTOList;
import io.openk9.plugin.driver.manager.model.SearchKeywordDTO;
import io.openk9.search.api.query.QueryParser;
import io.openk9.search.api.query.SearchRequest;
import io.openk9.search.api.query.SearchToken;
import io.openk9.search.api.query.SearchTokenizer;
import io.openk9.search.client.api.Search;
import io.openk9.search.query.internal.config.SearchConfig;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerRoutes;
import reactor.util.function.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component(
	immediate = true,
	service = RouterHandler.class,
	configurationPid = SearchConfig.PID
)
public class AutocompleteHTTPHandler extends BaseSearchHTTPHandler {

	@Activate
	@Modified
	public void activate(SearchConfig config) {
		_searchConfig = config;
	}

	@Override
	public HttpServerRoutes handle(
		HttpServerRoutes router) {
		return router
			.post("/v1/autocomplete", this);
	}

	@Override
	protected Mono<Tuple2<Tenant, List<Datasource>>> _getTenantAndDatasourceList(
		HttpServerRequest httpRequest, HttpServerResponse httpResponse) {
		String hostName = HttpUtil.getHostName(httpRequest);

		return _datasourceClient
			.findTenantByVirtualHost(hostName)
			.next()
			.switchIfEmpty(
				Mono.error(
					() -> new RuntimeException(
						"tenant not found for virtualhost: " + hostName)))
			.zipWhen(tenant -> _datasourceClient
				.findDatasourceByTenantIdAndIsActive(tenant.getTenantId())
				.collectList());
	}

	@Override
	protected void customizeSearchSourceBuilder(
		Tenant tenant, List<Datasource> datasources,
		SearchRequest searchRequest, List<PluginDriverDTO> documentTypeList,
		SearchSourceBuilder searchSourceBuilder,
		org.elasticsearch.action.search.SearchRequest elasticSearchQuery) {

		int[] range = searchRequest.getRange();

		if (range != null) {
			searchSourceBuilder.from(range[0]);
			searchSourceBuilder.size(range[1]);
		}

		searchSourceBuilder.trackTotalHits(false);

		List<String> autocompleteKeywords =
			searchRequest
				.getSearchQuery()
				.stream()
				.filter(searchToken -> searchToken.getTokenType().equals("AUTOCOMPLETE"))
				.map(SearchToken::getKeywordKey)
				.collect(Collectors.toList());

		Predicate<String> predicate;

		if (autocompleteKeywords.stream().anyMatch(Objects::isNull)) {
			predicate = s -> true;
		}
		else {
			predicate = autocompleteKeywords.isEmpty()
				? (s -> true)
				: autocompleteKeywords::contains;
		}

		String[] includes =
			documentTypeList
				.stream()
				.map(PluginDriverDTO::getDocumentTypes)
				.flatMap(Collection::stream)
				.map(DocumentTypeDTO::getSearchKeywords)
				.flatMap(Collection::stream)
				.filter(SearchKeywordDTO::isAutocomplete)
				.map(SearchKeywordDTO::getReferenceKeyword)
				.filter(predicate)
				.toArray(String[]::new);

		if (includes.length > 0) {
			searchSourceBuilder.fetchSource(includes, null);
		}
		else {
			searchSourceBuilder.from(0);
			searchSourceBuilder.size(0);
		}

	}

	protected Mono<Object> searchHitToResponseMono(
		Tenant tenant, List<Datasource> datasourceList,
		PluginDriverDTOList pluginDriverDTOList,
		HttpServerRequest httpServerRequest, SearchRequest searchRequest,
		SearchResponse searchResponse) {

		return Mono.fromSupplier(() -> {

			List<String> values =
				searchRequest
					.getSearchQuery()
					.stream()
					.filter(searchToken -> searchToken.getTokenType().equals(
						"AUTOCOMPLETE"))
					.map(SearchToken::getValues)
					.flatMap(Arrays::stream)
					.map(String::toLowerCase)
					.collect(Collectors.toList());

			SearchHits hits = searchResponse.getHits();

			Map<String, Collection<String>> innerResult = new HashMap<>();

			for (SearchHit hit : hits.getHits()) {

				Map<String, Object> sourceAsMap = hit.getSourceAsMap();

				if (sourceAsMap != null) {
					_findAndReplaceLeaf(null, sourceAsMap, values, innerResult);
				}

			}

			return innerResult;

		});
	}

	private void _findAndReplaceLeaf(
		String initialKey,
		Map<String, Object> originalMap,
		List<String> values,
		Map<String, Collection<String>> acc) {

		for (Map.Entry<String, Object> e : originalMap.entrySet()) {
			String key = e.getKey();
			Object value = e.getValue();

			String currentKey = (initialKey == null || initialKey.isBlank())
				? key
				: initialKey + "." + key;

			if (value instanceof Map) {
				_findAndReplaceLeaf(
					currentKey, ((Map<String, Object>) value), values, acc);
			}
			else if (value instanceof Collection) {

				List<String> newValues =
					((Collection<String>) value)
						.stream()
						.filter(s -> values.stream().anyMatch(
							s1 -> s.toLowerCase().contains(s1)))
						.collect(Collectors.toList());

				if (!newValues.isEmpty()) {

					acc.compute(currentKey, (a, b) -> {

						if (b == null) {
							b = new ArrayList<>();
						}

						for (String newValue : newValues) {
							if (!b.contains(newValue)) {
								b.add(newValue);
							}
						}

						return b;

					});

				}

			}
			else if (value instanceof String) {
				if (values.stream().anyMatch(
					s1 -> ((String) value).toLowerCase().contains(s1))) {
					acc.compute(currentKey, (a, b) -> {

						if (b == null) {
							b = new ArrayList<>();
						}

						if (!b.contains(value)) {
							b.add((String) value);
						}

						return b;

					});
				}
			}
		}
	}

	@Reference(
		service = QueryParser.class,
		bind = "addQueryParser",
		unbind = "removeQueryParser",
		target = "(component.name=io.openk9.search.query.internal.parser.SearchAsYouTypeQueryParser)",
		cardinality = ReferenceCardinality.MANDATORY,
		policyOption = ReferencePolicyOption.GREEDY,
		policy = ReferencePolicy.DYNAMIC
	)
	@Override
	protected void addQueryParser(QueryParser queryParser) {
		super.addQueryParser(queryParser);
	}

	@Override
	protected void removeQueryParser(QueryParser queryParser) {
		super.removeQueryParser(queryParser);
	}

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	@Override
	protected void setDatasourceClient(
		DatasourceClient datasourceClient) {
		super.setDatasourceClient(datasourceClient);
	}

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	@Override
	protected void setSearch(Search search) {
		super.setSearch(search);
	}

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	@Override
	protected void setSearchTokenizer(
		SearchTokenizer searchTokenizer) {
		super.setSearchTokenizer(searchTokenizer);
	}

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	@Override
	protected void setPluginDriverManagerClient(
		PluginDriverManagerClient pluginDriverManagerClient) {
		super.setPluginDriverManagerClient(pluginDriverManagerClient);
	}

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	@Override
	protected void setJsonFactory(JsonFactory jsonFactory) {
		super.setJsonFactory(jsonFactory);
	}

	@Override
	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	protected void setHttpResponseWriter(
		HttpResponseWriter httpResponseWriter) {
		super.setHttpResponseWriter(httpResponseWriter);
	}

	@Override
	protected SearchConfig getSearchConfig() {
		return _searchConfig;
	}

	private SearchConfig _searchConfig;

	private float minScore;

}
