package io.openk9.ingestion.web;

import io.openk9.ingestion.dto.BinaryDTO;
import io.openk9.ingestion.dto.BinaryPayload;
import io.openk9.ingestion.dto.IngestionDTO;
import io.openk9.ingestion.dto.IngestionPayload;
import io.openk9.ingestion.dto.ResourcesDTO;
import io.openk9.ingestion.dto.ResourcesPayload;
import io.openk9.ingestion.grpc.Binary;
import io.openk9.ingestion.grpc.IngestionRequest;
import io.openk9.ingestion.grpc.Resources;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.OnOverflow;

import javax.enterprise.context.ApplicationScoped;
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

	private IngestionPayload _of(IngestionRequest dto) {

		Map<String, Object> datasourcePayload =
			new JsonObject(dto.getDatasourcePayload())
				.getMap();

		return IngestionPayload.of(
			UUID.randomUUID().toString(),
			dto.getDatasourceId(),
			dto.getContentId(),
			dto.getParsingDate(),
			dto.getRawContent(),
			datasourcePayload,
			-1,
			datasourcePayload
				.keySet()
				.toArray(new String[0]),
			_dtoToPayload(dto.getResources())
		);
	}

	private IngestionPayload _of(IngestionDTO dto) {
		return IngestionPayload.of(
			UUID.randomUUID().toString(),
			dto.getDatasourceId(),
			dto.getContentId(),
			dto.getParsingDate(),
			dto.getRawContent(),
			dto.getDatasourcePayload(),
			-1,
			dto.getDatasourcePayload()
				.keySet()
				.toArray(new String[0]),
			_dtoToPayload(dto.getResources())
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
						binaryDTO.getContentType(), binaryDTO.getData()))
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
						binaryDTO.getContentType(), binaryDTO.getData()))
					.collect(Collectors.toList());
		}

		return ResourcesPayload.of(binaryPayloadList);
	}


	@Channel("ingestion")
	@OnOverflow(value = OnOverflow.Strategy.BUFFER, bufferSize = 1000)
	Emitter<IngestionPayload> _emitter;

}
