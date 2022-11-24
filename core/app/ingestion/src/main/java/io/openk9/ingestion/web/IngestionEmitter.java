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

import io.openk9.ingestion.dto.BinaryDTO;
import io.openk9.ingestion.dto.BinaryPayload;
import io.openk9.ingestion.dto.IngestionDTO;
import io.openk9.ingestion.dto.IngestionPayload;
import io.openk9.ingestion.dto.IngestionPayloadWrapper;
import io.openk9.ingestion.dto.ResourcesDTO;
import io.openk9.ingestion.dto.ResourcesPayload;
import io.openk9.ingestion.grpc.Binary;
import io.openk9.ingestion.grpc.IngestionRequest;
import io.openk9.ingestion.grpc.Resources;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.OnOverflow;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

@ApplicationScoped
public class IngestionEmitter {

	public CompletionStage<Void> emit(IngestionRequest ingestionRequest) {

		return _emitter.send(_of(ingestionRequest));

	}

	public CompletionStage<Void> emit(IngestionDTO ingestionDTO) {

		return _emitter.send(_of(ingestionDTO));
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
				datasourcePayload
					.keySet()
					.toArray(new String[0]),
				_dtoToPayload(dto.getResources()),
				mappingAcl,
				dto.getScheduleId()
			)
		);
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
				dto.getDatasourcePayload()
					.keySet()
					.toArray(new String[0]),
				_dtoToPayload(dto.getResources()),
				dto.getAcl(),
				dto.getScheduleId()
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
						binaryDTO.getContentType(), binaryDTO.getData(), binaryDTO.getResourceId()))
					.collect(Collectors.toList());
		}

		return ResourcesPayload.of(binaryPayloadList);
	}

	private ResourcesPayload _dtoToPayload(
		ResourcesDTO resources) {

		List<BinaryDTO> binaries = resources.getBinaries();

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
						binaryDTO.getContentType(), binaryDTO.getData(), binaryDTO.getResourceId()))
					.collect(Collectors.toList());
		}

		return ResourcesPayload.of(binaryPayloadList);
	}


	@Channel("ingestion")
	@OnOverflow(value = OnOverflow.Strategy.BUFFER, bufferSize = 1000)
	Emitter<IngestionPayloadWrapper> _emitter;


	@Inject
	Logger logger;

}
