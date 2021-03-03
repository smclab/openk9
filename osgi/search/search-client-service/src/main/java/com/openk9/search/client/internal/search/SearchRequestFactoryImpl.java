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

package com.openk9.search.client.internal.search;

import com.openk9.search.client.api.SearchRequestFactory;
import com.openk9.search.client.internal.configuration.ElasticSearchConfiguration;
import com.openk9.search.client.internal.util.IndexUtil;
import org.elasticsearch.action.search.SearchRequest;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.Objects;

@Component(
	immediate = true,
	service = SearchRequestFactory.class
)
public class SearchRequestFactoryImpl implements SearchRequestFactory {

	@Override
	public SearchRequest createSearchRequest(long tenantId, String indexName) {
		return new SearchRequest(IndexUtil.getIndexName(tenantId, indexName));
	}

	@Override
	public SearchRequest createSearchRequestEntity(long tenantId) {
		return new SearchRequest(
			IndexUtil.getIndexName(
				tenantId, _elasticSearchConfiguration.getEntityIndex())
		);
	}

	@Override
	public SearchRequest createSearchRequestData(
		long tenantId, String driverName) {

		return new SearchRequest(
			IndexUtil.getIndexName(
				tenantId, driverName,
				_elasticSearchConfiguration.getDataIndex())
		);
	}

	@Override
	public SearchRequest createSearchRequestData(
		long tenantId, String...driverNames) {

		Objects.requireNonNull(driverNames, "driverNames is null");

		String[] indexNames = Arrays
			.stream(driverNames)
			.map(driverName -> IndexUtil.getIndexName(
				tenantId, driverName,
				_elasticSearchConfiguration.getDataIndex()))
			.toArray(String[]::new);

		return new SearchRequest(indexNames);

	}

	@Reference
	private ElasticSearchConfiguration _elasticSearchConfiguration;

}
