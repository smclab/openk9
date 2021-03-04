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

package com.openk9.search.enrich.internal;

import com.openk9.cbor.api.CBORFactory;
import com.openk9.datasource.model.Datasource;
import com.openk9.datasource.model.EnrichItem;
import com.openk9.datasource.repository.DatasourceRepository;
import com.openk9.datasource.util.DatasourceContext;
import com.openk9.http.osgi.constants.Constants;
import com.openk9.ingestion.api.BundleReceiver;
import com.openk9.ingestion.api.Delivery;
import com.openk9.json.api.JsonFactory;
import com.openk9.json.api.JsonNode;
import com.openk9.json.api.ObjectNode;
import com.openk9.osgi.util.AutoCloseables;
import com.openk9.ingestion.driver.manager.api.DocumentType;
import com.openk9.ingestion.driver.manager.api.DocumentTypeProvider;
import com.openk9.ingestion.driver.manager.api.PluginDriver;
import com.openk9.ingestion.driver.manager.api.PluginDriverRegistry;
import com.openk9.search.enrich.api.StartEnrichProcessor;
import com.openk9.search.enrich.api.dto.EnrichProcessorContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.concurrent.Queues;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.util.List;
import java.util.Map;
import java.util.Optional;
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
			.map(Delivery::getBody)
			.map(_cborFactory::fromCBORToJsonNode)
			.map(JsonNode::toObjectNode)
			.concatMap(objectNode -> {

				JsonNode datasourceIdNode = objectNode.get("datasourceId");

				long datasourceId = datasourceIdNode.asLong();

				return _datasourceRepository
					.findContext(datasourceId)
					.zipWith(Mono.just(objectNode.deepCopy()))
					.map(t2 -> Tuples.of(t2.getT1(), t2.getT2()))
					.map(this::_addPluginDriverData)
					.map(this::_mapToEnrichProcessorContext)
					.flatMap(_startEnrichProcessor::exec);
			})
			.transform(this::_manageExceptions)
			.subscribe();
	}

	private EnrichProcessorContext _mapToEnrichProcessorContext(
		Tuple3<DatasourceContext, ObjectNode, PluginDriver> t3) {

		List<String> dependencies = t3
			.getT1()
			.getEnrichItems()
			.stream()
			.map(EnrichItem::getServiceName)
			.collect(Collectors.toList());

		return EnrichProcessorContext
			.builder()
			.dependencies(dependencies)
			.datasourceContext(t3.getT1())
			.objectNode(t3.getT2().toMap())
			.pluginDriverName(t3.getT3().getName())
			.build();
	}

	private Tuple3<
			DatasourceContext,
			ObjectNode, PluginDriver> _addPluginDriverData(
		Tuple2<
			DatasourceContext,
			ObjectNode> t3) {

		ObjectNode ingestionPayload = t3.getT2();

		Datasource datasource = t3.getT1().getDatasource();

		String driverServiceName = datasource.getDriverServiceName();

		Optional<PluginDriver> pluginDriverOptional =
			_pluginDriverProvider
				.getPluginDriver(driverServiceName);

		PluginDriver pluginDriver =
			pluginDriverOptional.orElseThrow(
				() -> new RuntimeException(
					"pluginDriver not found for driverServiceName: "
					+ driverServiceName));

		ObjectNode newIngestionPayload = ingestionPayload;

		if (newIngestionPayload.hasNonNull(
			com.openk9.core.api.constant.Constants.DATASOURCE_PAYLOAD)) {

			JsonNode datasourcePayload =
				newIngestionPayload.remove(
					com.openk9.core.api.constant.Constants.DATASOURCE_PAYLOAD);

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
					pluginDriver.getName());
		}

		if (
			ingestionPayload.hasNonNull(
				com.openk9.core.api.constant.Constants.TYPE) &&
			!ingestionPayload.get(com.openk9.core.api.constant.Constants.TYPE).toArrayNode().isEmpty()
		) {
			return Tuples.of(
				t3.getT1(),
				t3.getT2(),
				pluginDriver
			);
		}

		DocumentType documentType =
			_documentTypeProvider.getDefaultDocumentType(
				pluginDriver.getName());

		return Tuples.of(
			t3.getT1(),
			newIngestionPayload
				.set(
					com.openk9.core.api.constant.Constants.TYPE,
					_jsonFactory
						.createArrayNode()
						.add(documentType.getName())),
			pluginDriver
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
	private DatasourceRepository _datasourceRepository;

	@Reference
	private PluginDriverRegistry _pluginDriverProvider;

	@Reference
	private StartEnrichProcessor _startEnrichProcessor;

	@Reference
	private DocumentTypeProvider _documentTypeProvider;

	private static final Logger _log = LoggerFactory.getLogger(
		EnrichPipelineProcessor.class);

}
