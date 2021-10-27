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

package io.openk9.search.enrich.internal;

import io.openk9.cbor.api.CBORFactory;
import io.openk9.http.osgi.constants.Constants;
import io.openk9.ingestion.api.BundleReceiver;
import io.openk9.json.api.JsonFactory;
import io.openk9.json.api.JsonNode;
import io.openk9.json.api.ObjectNode;
import io.openk9.model.DatasourceContext;
import io.openk9.model.EnrichItem;
import io.openk9.osgi.util.AutoCloseables;
import io.openk9.plugin.driver.manager.model.DocumentTypeDTO;
import io.openk9.plugin.driver.manager.model.IngestionDatasourcePluginDriverPayload;
import io.openk9.plugin.driver.manager.model.PluginDriverDTO;
import io.openk9.search.enrich.api.StartEnrichProcessor;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.util.concurrent.Queues;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component(immediate = true, service = EnrichPipelineProcessor.class)
public class EnrichPipelineProcessor {

	@Activate
	public void activate() {

		Disposable disposable = _erichProcessorSubscriber();

		_autoCloseableSafe = AutoCloseables
			.mergeAutoCloseableToSafe(disposable::dispose);

	}

	@Deactivate
	public void deactivate() {
		_autoCloseableSafe.close();
	}

	private Disposable _erichProcessorSubscriber() {
		return _bundleReceiver
			.consumeAutoAck(Queues.XS_BUFFER_SIZE)
			.map(delivery -> _jsonFactory.fromJson(
				delivery.getBody(), IngestionDatasourcePluginDriverPayload.class))
			.map(idp ->
				_mapToEnrichProcessorContext(
					_adaptIngestionPayload(
						idp.getDatasourceContext(),
						_cborFactory
							.treeNode(idp.getIngestionPayload())
							.toObjectNode(),
						idp.getPluginDriverDTO()
					)
				)
			)
			.flatMap(_startEnrichProcessor::exec)
			.transform(this::_manageExceptions)
			.subscribe();
	}

	private ObjectNode _mapToEnrichProcessorContext(
		Tuple3<DatasourceContext, ObjectNode, PluginDriverDTO> t3) {

		List<String> dependencies = t3
			.getT1()
			.getEnrichItems()
			.stream()
			.map(EnrichItem::getServiceName)
			.collect(Collectors.toList());

		ObjectNode objectNode = _jsonFactory.createObjectNode();

		objectNode.put("pluginDriver", _jsonFactory.fromObjectToJsonNode(t3.getT3()));
		objectNode.put("dependencies", _jsonFactory.fromObjectToJsonNode(dependencies));
		objectNode.put("datasourceContext", _jsonFactory.fromObjectToJsonNode(t3.getT1()));
		objectNode.put("payload", t3.getT2());

		return objectNode;
	}

	private Tuple3<
		DatasourceContext,
		ObjectNode, PluginDriverDTO> _adaptIngestionPayload(
			DatasourceContext datasourceContext, ObjectNode ingestionPayload,
			PluginDriverDTO pluginDriverDTO) {

		ObjectNode newIngestionPayload = ingestionPayload;

		if (newIngestionPayload.hasNonNull(
			io.openk9.core.api.constant.Constants.DATASOURCE_PAYLOAD)) {

			JsonNode datasourcePayload =
				newIngestionPayload.remove(
					io.openk9.core.api.constant.Constants.DATASOURCE_PAYLOAD);

			if (datasourcePayload.isObject()) {
				ObjectNode jsonNodes = datasourcePayload.toObjectNode();

				for (Map.Entry<String, JsonNode> field : jsonNodes.fields()) {
					newIngestionPayload.set(field.getKey(), field.getValue());
				}

			}

		}

		if (!ingestionPayload.hasNonNull(
			Constants.DATASOURCE_NAME)) {

			newIngestionPayload = newIngestionPayload
				.deepCopy()
				.put(
					Constants.DATASOURCE_NAME,
					pluginDriverDTO.getName());
		}

		if (
			ingestionPayload.hasNonNull(
				io.openk9.core.api.constant.Constants.TYPE) &&
			!ingestionPayload.get(io.openk9.core.api.constant.Constants.TYPE).toArrayNode().isEmpty()
		) {
			return Tuples.of(
				datasourceContext,
				ingestionPayload,
				pluginDriverDTO
			);
		}

		DocumentTypeDTO documentType =
			pluginDriverDTO.getDefaultDocumentType();

		return Tuples.of(
			datasourceContext,
			newIngestionPayload
				.set(
					io.openk9.core.api.constant.Constants.TYPE,
					_jsonFactory
						.createArrayNode()
						.add(documentType.getName())),
			pluginDriverDTO
		);


	}

	private <V> Publisher<V> _manageExceptions(Publisher<V> objectNodeFlux) {
		return Flux.from(objectNodeFlux).onErrorContinue((throwable, o) -> {

			if (_log.isErrorEnabled()) {

				if (o != null) {
					_log.error("error on object: " + o, throwable);
				}
				else {
					_log.error(throwable.getMessage(), throwable);
				}
			}

		});
	}

	private AutoCloseables.AutoCloseableSafe _autoCloseableSafe;

	@Reference
	private JsonFactory _jsonFactory;

	@Reference
	private CBORFactory _cborFactory;

	@Reference(
		target = "(queue=data-ingestion)"
	)
	private BundleReceiver _bundleReceiver;

	@Reference
	private StartEnrichProcessor _startEnrichProcessor;

	private static final Logger _log = LoggerFactory.getLogger(
		EnrichPipelineProcessor.class);

}

