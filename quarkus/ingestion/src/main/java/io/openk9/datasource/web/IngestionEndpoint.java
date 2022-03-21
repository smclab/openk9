package io.openk9.datasource.web;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.OnOverflow;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("/v1/ingestion/")
public class IngestionEndpoint {

	@POST
	public Response ingestion(IngestionDTO dto) {

		ResourcesPayload resourcesPayload = _dtoToPayload(dto.getResources());

		_emitter.send(
			IngestionPayload.of(
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
				resourcesPayload
			)
		);

		return Response.ok("{}").build();

	}

	private ResourcesPayload _dtoToPayload(ResourcesDTO resources) {

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
						binaryDTO.id, binaryDTO.name, binaryDTO.contentType,
						binaryDTO.data))
					.collect(Collectors.toList());
		}

		return ResourcesPayload.of(binaryPayloadList);
	}

	@Channel("ingestion")
	@OnOverflow(value = OnOverflow.Strategy.BUFFER, bufferSize = 1000)
	Emitter<IngestionPayload> _emitter;

	@Data
	@Builder
	@AllArgsConstructor(staticName = "of")
	@NoArgsConstructor
	@RegisterForReflection
	public static class IngestionPayload {
		private String ingestionId;
		private long datasourceId;
		private String contentId;
		private long parsingDate;
		private String rawContent;
		private Map<String, Object> datasourcePayload;
		private long tenantId;
		private String[] documentTypes;
		private ResourcesPayload resources;
	}

	@Data
	@Builder
	@AllArgsConstructor(staticName = "of")
	@NoArgsConstructor
	@RegisterForReflection
	public static class ResourcesPayload {
		private List<BinaryPayload> binaries;
	}

	@Data
	@Builder
	@AllArgsConstructor(staticName = "of")
	@NoArgsConstructor
	@RegisterForReflection
	public static class BinaryPayload {
		private String id;
		private String name;
		private String contentType;
		private String data;
	}


	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor(staticName = "of")
	@RegisterForReflection
	public static class IngestionDTO {
		private long datasourceId;
		private String contentId;
		private long parsingDate;
		private String rawContent;
		private Map<String, Object> datasourcePayload;
		private ResourcesDTO resources;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor(staticName = "of")
	@RegisterForReflection
	public static class ResourcesDTO {
		private List<BinaryDTO> binaries;
	}


	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor(staticName = "of")
	@RegisterForReflection
	public static class BinaryDTO {
		private String id;
		private String name;
		private String contentType;
		private String data;
	}

}