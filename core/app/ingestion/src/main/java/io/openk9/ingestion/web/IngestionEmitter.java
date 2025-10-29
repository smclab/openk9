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

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.openk9.common.util.ingestion.IngestionUtils;
import io.openk9.common.util.ingestion.ShardingKey;
import io.openk9.ingestion.dto.BinaryDTO;
import io.openk9.ingestion.dto.BinaryPayload;
import io.openk9.ingestion.dto.IngestionDTO;
import io.openk9.ingestion.dto.IngestionPayload;
import io.openk9.ingestion.dto.IngestionPayloadWrapper;
import io.openk9.ingestion.dto.ResourcesDTO;
import io.openk9.ingestion.dto.ResourcesPayload;
import io.openk9.ingestion.exception.NoSuchQueueException;

import com.rabbitmq.client.Connection;
import io.quarkiverse.rabbitmqclient.RabbitMQClient;
import io.smallrye.reactive.messaging.rabbitmq.OutgoingRabbitMQMetadata;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;
import org.eclipse.microprofile.reactive.messaging.OnOverflow;
import org.jboss.logging.Logger;

@ApplicationScoped
public class IngestionEmitter {

	@Channel("ingestion")
	@OnOverflow(value = OnOverflow.Strategy.BUFFER, bufferSize = 1000)
	Emitter<IngestionPayloadWrapper> _emitter;
	@Inject
	Logger logger;

	@Inject
	RabbitMQClient rabbitMQClient;

	private Connection connect;

	@PreDestroy
	public void destroy() throws IOException {
		connect.close();
	}

	public CompletionStage<Void> emit(IngestionDTO ingestionDTO) {

		var queueName = ShardingKey.asString(
			ingestionDTO.getTenantId(),
			ingestionDTO.getScheduleId());

		emit(createMessage(_of(ingestionDTO)), queueName);

		return CompletableFuture.completedStage(null);
	}

	@PostConstruct
	public void init() {
		this.connect = rabbitMQClient.connect();
	}

	private void emit(Message<IngestionPayloadWrapper> message, String queueName)
		throws NoSuchQueueException {

		if (checkQueueExistence(queueName)) {
			_emitter.send(message);
		}
		else {
			throw new NoSuchQueueException(
				String.format("No such queue with name: \"%s\".", queueName));
		}
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

	private String _toRoutingKey(IngestionPayloadWrapper ingestionPayloadWrapper) {
		IngestionPayload ingestionPayload = ingestionPayloadWrapper.getIngestionPayload();

		return ShardingKey.asString(
			ingestionPayload.getTenantId(),
			ingestionPayload.getScheduleId()
		);
	}

	private boolean checkQueueExistence(String queueName) {
		boolean exist = false;
		try (var channel = connect.createChannel()) {
			channel.queueDeclarePassive(queueName);
			exist = true;

		}
		catch (IOException e) {
			logger.warnf(String.format("No such queue with name: \"%s\".", queueName));
			exist = false;

		}
		catch (TimeoutException e) {
			logger.errorf(e, String.format("Error closing channel."));
		}

		return exist;
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

}