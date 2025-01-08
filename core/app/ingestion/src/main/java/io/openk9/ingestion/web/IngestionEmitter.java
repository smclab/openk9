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

package io.openk9.ingestion.web;

import io.openk9.common.util.ShardingKey;
import io.openk9.common.util.ingestion.IngestionUtils;
import io.openk9.common.util.ingestion.PayloadType;
import io.openk9.ingestion.dto.BinaryDTO;
import io.openk9.ingestion.dto.BinaryPayload;
import io.openk9.ingestion.dto.IngestionDTO;
import io.openk9.ingestion.dto.IngestionPayload;
import io.openk9.ingestion.dto.IngestionPayloadWrapper;
import io.openk9.ingestion.dto.ResourcesDTO;
import io.openk9.ingestion.dto.ResourcesPayload;
import io.openk9.ingestion.exception.NoSuchQueueException;
import io.openk9.ingestion.grpc.Binary;
import io.openk9.ingestion.grpc.IngestionRequest;
import io.openk9.ingestion.grpc.Resources;
import io.quarkiverse.rabbitmqclient.RabbitMQClient;
import io.smallrye.reactive.messaging.rabbitmq.OutgoingRabbitMQMetadata;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;
import org.eclipse.microprofile.reactive.messaging.OnOverflow;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

@ApplicationScoped
public class IngestionEmitter {

	@Channel("ingestion")
	@OnOverflow(value = OnOverflow.Strategy.BUFFER, bufferSize = 1000)
	Emitter<IngestionPayloadWrapper> _emitter;
	@Inject
	Logger logger;

	@Inject
	RabbitMQClient rabbitMQClient;

	public CompletionStage<Void> emit(IngestionRequest ingestionRequest) {

		_emitter.send(createMessage(_of(ingestionRequest)));

		return CompletableFuture.completedStage(null);

	}

	public CompletionStage<Void> emit(IngestionDTO ingestionDTO) throws NoSuchQueueException{

		var queueName = ShardingKey.asString(
			ingestionDTO.getTenantId(),
			ingestionDTO.getScheduleId());

		if (checkQueueExistence(queueName)) {
			_emitter.send(createMessage(_of(ingestionDTO)));

			return CompletableFuture.completedStage(null);
		}
		else {
			throw new NoSuchQueueException(
				String.format("No such queue with name: \"%s\".", queueName));
		}
	}

	private IngestionPayloadWrapper _of(IngestionRequest dto) {

		Map<String, Object> datasourcePayload =
			new JsonObject(dto.getDatasourcePayload())
				.getMap();

		Map<String, List<String>> mappingAcl =
			dto
				.getAclMap()
				.entrySet()
				.stream()
				.collect(
					Collectors.toMap(
						Map.Entry::getKey,
						e -> new ArrayList<>(e.getValue().getValueList())
					)
				);

		return IngestionPayloadWrapper.of(
			IngestionPayload.of(
				UUID.randomUUID().toString(),
				dto.getDatasourceId(),
				dto.getContentId(),
				dto.getParsingDate(),
				dto.getRawContent(),
				datasourcePayload,
				dto.getTenantId(),
				IngestionUtils.getDocumentTypes(datasourcePayload),
				_dtoToPayload(dto.getResources()),
				mappingAcl,
				dto.getScheduleId(),
				dto.getLast(),
				_mapType(dto.getType())
			)
		);
	}

	private PayloadType _mapType(io.openk9.ingestion.grpc.PayloadType type) {
		return switch (type) {
			case DOCUMENT -> PayloadType.DOCUMENT;
			case LAST -> PayloadType.LAST;
			case HALT -> PayloadType.HALT;
			case UNRECOGNIZED -> null;
		};
	}

	private IngestionPayloadWrapper _of(IngestionDTO dto) {
		return IngestionPayloadWrapper.of(
			IngestionPayload.of(
				UUID.randomUUID().toString(),
				dto.getDatasourceId(),
				dto.getContentId(),
				dto.getParsingDate(),
				dto.getRawContent(),
				dto.getDatasourcePayload(),
				dto.getTenantId(),
				IngestionUtils.getDocumentTypes(dto.getDatasourcePayload()),
				_dtoToPayload(dto.getResources()),
				dto.getAcl(),
				dto.getScheduleId(),
				dto.isLast(),
				dto.getType()
			)
		);
	}

	private ResourcesPayload _dtoToPayload(
		Resources resources) {

		List<Binary> binaries = resources.getBinaryList();

		List<BinaryPayload> binaryPayloadList;

		if (binaries == null) {
			binaryPayloadList = List.of();
		}
		else {
			binaryPayloadList =
				binaries
					.stream()
					.map(binaryDTO -> BinaryPayload.of(
						binaryDTO.getId(), binaryDTO.getName(),
						binaryDTO.getContentType(), binaryDTO.getData(), binaryDTO.getResourceId()
					))
					.collect(Collectors.toList());
		}

		return ResourcesPayload.of(binaryPayloadList);
	}

	private ResourcesPayload _dtoToPayload(
		ResourcesDTO resources) {

		List<BinaryDTO> binaries = null;

		if (resources != null) {
			binaries = resources.getBinaries();
		}

		List<BinaryPayload> binaryPayloadList;

		if (binaries == null) {
			binaryPayloadList = List.of();
		}
		else {
			binaryPayloadList =
				binaries
					.stream()
					.map(binaryDTO -> BinaryPayload.of(
						binaryDTO.getId(), binaryDTO.getName(),
						binaryDTO.getContentType(), binaryDTO.getData(), binaryDTO.getResourceId()
					))
					.collect(Collectors.toList());
		}

		return ResourcesPayload.of(binaryPayloadList);
	}

	private boolean checkQueueExistence(String queueName) {
		try {
			var channel = rabbitMQClient.connect().createChannel();
			channel.queueDeclarePassive(queueName);

			return true;
		}
		catch (IOException e) {
			logger.warnf(String.format("No such queue with name: \"%s\".", queueName));

			return false;
		}
	}

	private Message<IngestionPayloadWrapper> createMessage(IngestionPayloadWrapper ingestionPayloadWrapper) {
		return Message.of(
			ingestionPayloadWrapper,
			Metadata.of(
				OutgoingRabbitMQMetadata
					.builder()
					.withRoutingKey(_toRoutingKey(ingestionPayloadWrapper))
					.withDeliveryMode(2)
					.build()
			)
		);
	}

	private String _toRoutingKey(IngestionPayloadWrapper ingestionPayloadWrapper) {
		IngestionPayload ingestionPayload = ingestionPayloadWrapper.getIngestionPayload();

		return ShardingKey.asString(
			ingestionPayload.getTenantId(),
			ingestionPayload.getScheduleId()
		);
	}

}