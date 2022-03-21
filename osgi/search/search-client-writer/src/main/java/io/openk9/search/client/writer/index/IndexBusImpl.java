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
import io.openk9.search.client.api.IndexBus;

import io.openk9.search.client.writer.ElasticSearchIndexer;
import org.elasticsearch.action.DocWriteRequest;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.function.Function;

@Component(service = IndexBus.class)
public class IndexBusImpl implements IndexBus {

	@Override
	public void sendRequest(DocWriteRequest<?> request) {
		_elasticSearchIndexer.sendDocWriteRequest(request);
	}

	@Override
	public void sendRequest(
		Function<DocWriteRequestFactory, DocWriteRequest<?>> requestFunction) {

		sendRequest(requestFunction.apply(_docWriteRequestFactory));
	}

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private ElasticSearchIndexer _elasticSearchIndexer;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private DocWriteRequestFactory _docWriteRequestFactory;

}
