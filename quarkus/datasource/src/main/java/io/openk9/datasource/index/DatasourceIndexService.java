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
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.ResizeRequest;
import org.elasticsearch.client.indices.ResizeResponse;
import org.elasticsearch.common.settings.Settings;
import org.jboss.logging.Logger;
import reactor.core.publisher.Mono;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class DatasourceIndexService {

	public Mono<Object> reindex(Datasource datasource) {

		return Mono.defer(() -> {

		Uni<PluginDriverDTO> pluginDriver =
			pluginDriverClient.getPluginDriver(
				datasource.getDriverServiceName());

		Mono<PluginDriverDTO> pluginDriverDTOMono =
			Mono.from(pluginDriver.convert().toPublisher());

		return pluginDriverDTOMono
			.map(response -> datasource.getTenantId() + "-" + response.getName() + "-data")
			.filterWhen(indexName -> _indexExists(indexName,  client.indices()))
			.flatMap(indexName -> _modifiedSettings(indexName, client.indices()).thenReturn(indexName))
			.flatMap(indexName -> _cloneIndex(indexName, client.indices()).thenReturn(indexName))
			.flatMap(targetIndex ->
				Mono.create(emitter -> client.indices().deleteAsync(
					new DeleteIndexRequest(targetIndex), RequestOptions.DEFAULT,
					new ActionListener<>() {
						@Override
						public void onResponse(AcknowledgedResponse deleteResponse) {
							emitter.success(deleteResponse);
						}

						@Override
						public void onFailure(Exception e) {
							emitter.error(e);
						}
					}))
			)
			.doOnNext(o -> logger.info("datasource " + datasource.getDatasourceId() + " " + o))
			.defaultIfEmpty(
				Mono.fromRunnable(() -> logger.info("default case for datasource " + datasource.getDatasourceId())));
		});

	}

	private Mono<?> _modifiedSettings(String indexName, IndicesClient indices) {

		UpdateSettingsRequest updateSettingsRequest =
			new UpdateSettingsRequest(indexName);

		updateSettingsRequest.settings(
			Settings.builder()
				.put("index.blocks.write", true)
				.build());

		return Mono.create(sink -> indices.putSettingsAsync(
			updateSettingsRequest, RequestOptions.DEFAULT,
			new ActionListener<AcknowledgedResponse>() {
				@Override
				public void onResponse(AcknowledgedResponse resizeResponse) {
					sink.success(resizeResponse);
				}

				@Override
				public void onFailure(Exception e) {
					sink.error(e);
				}
			}));

	}

	private Mono<ResizeResponse> _cloneIndex(String indexName, IndicesClient indices) {

		ResizeRequest resizeRequest = new ResizeRequest(
			indexName.replace("-data", "-clone"), indexName);

		return Mono.create(emitter ->
			indices.cloneAsync(
				resizeRequest, RequestOptions.DEFAULT,
				new ActionListener<>() {
					@Override
					public void onResponse(ResizeResponse resizeResponse) {
						emitter.success(resizeResponse);
					}

					@Override
					public void onFailure(Exception e) {
						emitter.error(e);
					}
				})
		);
	}

	private Mono<Boolean> _indexExists(String indexName, IndicesClient indices) {
		return Mono.create(emitter -> indices.existsAsync(
			new GetIndexRequest(indexName), RequestOptions.DEFAULT,
			new ActionListener<>() {
				@Override
				public void onResponse(Boolean exists) {
					emitter.success(exists);
				}

				@Override
				public void onFailure(Exception e) {
					emitter.error(e);
				}
			}));
	}

	@Inject
	RestHighLevelClient client;

	@Inject
	@RestClient
	PluginDriverClient pluginDriverClient;

	@Inject
	Logger logger;

}
