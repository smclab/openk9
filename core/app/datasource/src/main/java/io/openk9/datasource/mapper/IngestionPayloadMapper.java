package io.openk9.datasource.mapper;

import io.openk9.datasource.processor.payload.DataPayload;
import io.openk9.datasource.processor.payload.IngestionPayload;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi")
public interface IngestionPayloadMapper {

	@Mapping(
		target = "rest",
		source = "datasourcePayload"
	)
	DataPayload map(IngestionPayload ingestionPayload);

}
