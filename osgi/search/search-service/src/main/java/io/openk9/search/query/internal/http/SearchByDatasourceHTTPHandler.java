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
import io.openk9.http.web.RouterHandler;
import io.openk9.json.api.JsonFactory;
import io.openk9.model.Datasource;
import io.openk9.model.Tenant;
import io.openk9.plugin.driver.manager.client.api.PluginDriverManagerClient;
import io.openk9.search.api.query.QueryParser;
import io.openk9.search.api.query.SearchTokenizer;
import io.openk9.search.client.api.Search;
import io.openk9.search.query.internal.config.SearchConfig;
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
import reactor.util.function.Tuples;

import java.util.List;
import java.util.Objects;

@Component(
	immediate = true,
	service = RouterHandler.class,
	configurationPid = SearchConfig.PID
)
public class SearchByDatasourceHTTPHandler extends BaseSearchHTTPHandler {

	@Activate
	@Modified
	public void activate(SearchConfig config) {
		_searchConfig = config;
	}

	@Override
	public HttpServerRoutes handle(HttpServerRoutes router) {
		return router.post("/v1/search/{datasourceId}", this);
	}

	@Override
	protected Mono<Tuple2<Tenant, List<Datasource>>> _getTenantAndDatasourceList(
		HttpServerRequest httpRequest, HttpServerResponse httpResponse) {

		long datasourceId =
			Long.parseLong(
				Objects.requireNonNull(httpRequest.param("datasourceId")));

		return _datasourceClient
			.findTenantAndDatasourceByDatasourceId(datasourceId)
			.map(tenantDatasource -> Tuples.of(
				tenantDatasource.getTenant(),
				List.of(tenantDatasource.getDatasource()))
			);
	}

	@Reference(
		service = QueryParser.class,
		bind = "addQueryParser",
		unbind = "removeQueryParser",
		cardinality = ReferenceCardinality.MULTIPLE,
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

}
