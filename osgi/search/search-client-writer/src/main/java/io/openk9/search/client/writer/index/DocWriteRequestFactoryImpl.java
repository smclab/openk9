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

package io.openk9.search.client.writer.index;

import io.openk9.search.client.api.DocWriteRequestFactory;
import io.openk9.search.client.api.configuration.ElasticSearchConfiguration;
import io.openk9.search.client.api.util.IndexUtil;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service = DocWriteRequestFactory.class)
public class DocWriteRequestFactoryImpl implements DocWriteRequestFactory {

	@Override
	public IndexRequest createDataIndexRequest(long tenantId, String driverName) {
		return new IndexRequest(
			IndexUtil.getIndexName(
				tenantId, driverName,
				_elasticSearchConfiguration.getDataIndex()));
	}

	@Override
	public IndexRequest createEntityIndexRequest(long tenantId) {
		return new IndexRequest(
			IndexUtil.getIndexName(
				tenantId, _elasticSearchConfiguration.getEntityIndex()));
	}

	@Override
	public DeleteRequest createDataDeleteRequest(
		long tenantId, String driverName) {

		return new DeleteRequest(
			IndexUtil.getIndexName(
				tenantId, driverName,
				_elasticSearchConfiguration.getDataIndex()));
	}

	@Override
	public DeleteRequest createEntityDeleteRequest(long tenantId) {
		return new DeleteRequest(
			IndexUtil.getIndexName(
				tenantId, _elasticSearchConfiguration.getEntityIndex()));
	}

	@Override
	public DeleteRequest createDataDeleteRequest(
		long tenantId, String driverName, String id) {
		return new DeleteRequest(
			IndexUtil.getIndexName(
				tenantId, driverName,
				_elasticSearchConfiguration.getDataIndex()), id);
	}

	@Override
	public DeleteRequest createEntityDeleteRequest(long tenantId, String id) {
		return new DeleteRequest(
			IndexUtil.getIndexName(
				tenantId, _elasticSearchConfiguration.getEntityIndex(), id));
	}

	@Override
	public UpdateRequest createDataUpdateRequest(
		long tenantId, String driverName, String id) {

		return new UpdateRequest(
			IndexUtil.getIndexName(
				tenantId, driverName,
				_elasticSearchConfiguration.getDataIndex()), id);
	}

	@Override
	public UpdateRequest createEntityUpdateRequest(long tenantId, String id) {
		return new UpdateRequest(
			IndexUtil.getIndexName(
				tenantId, _elasticSearchConfiguration.getEntityIndex()), id);
	}

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private ElasticSearchConfiguration _elasticSearchConfiguration;

}
