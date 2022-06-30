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

package io.openk9.datasource.index;

import io.openk9.datasource.client.plugindriver.PluginDriverClient;
import io.openk9.datasource.client.plugindriver.dto.PluginDriverDTO;
import io.openk9.datasource.model.Datasource;
import io.smallrye.mutiny.Uni;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.ResizeRequest;
import org.elasticsearch.client.indices.ResizeResponse;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class DatasourceIndexService {

	public Uni<Void> reindex(Datasource datasource) {

		Uni<PluginDriverDTO> pluginDriver =
			pluginDriverClient.getPluginDriver(
				datasource.getDriverServiceName());

		return pluginDriver
			.map(response -> datasource.getTenantId() + "-" + response.getName() + "-data")
			.call(indexName -> {

				IndicesClient indices = client.indices();

				ResizeRequest resizeRequest = new ResizeRequest(
					indexName, indexName.replace("-data", "-clone"));

				return Uni.createFrom().emitter(emitter ->
					indices.cloneAsync(
						resizeRequest, RequestOptions.DEFAULT,
						new ActionListener<>() {
							@Override
							public void onResponse(ResizeResponse resizeResponse) {
								emitter.complete(resizeResponse);
							}

							@Override
							public void onFailure(Exception e) {
								emitter.fail(e);
							}
						})
				);
			})
			.flatMap(targetIndex ->

				Uni.createFrom().emitter(emitter -> client.deleteAsync(
					new DeleteRequest(targetIndex), RequestOptions.DEFAULT,
					new ActionListener<>() {
						@Override
						public void onResponse(DeleteResponse deleteResponse) {
							emitter.complete(deleteResponse);
						}

						@Override
						public void onFailure(Exception e) {
							emitter.fail(e);
						}
					}))
			)
			.onItemOrFailure().invoke((o, t) -> {
				if (o != null) {
					logger.info("datasource " + datasource.getDatasourceId() + " " + o);
				}
				if (t != null) {
					logger.error("error reindexing datasource " + datasource.getDatasourceId(), t);
				}
			})
			.replaceWithVoid();


	}

	@Inject
	RestHighLevelClient client;

	@Inject
	PluginDriverClient pluginDriverClient;

	@Inject
	Logger logger;

}
