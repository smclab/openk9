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

package io.openk9.datasource.mapper;

import java.util.Arrays;
import java.util.List;

import io.openk9.common.util.ingestion.IngestionUtils;
import io.openk9.common.util.ingestion.PayloadType;
import io.openk9.datasource.processor.payload.DataPayload;
import io.openk9.datasource.processor.payload.IngestionIndexWriterPayload;
import io.openk9.datasource.processor.payload.IngestionPayload;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

@Mapper(
	componentModel = MappingConstants.ComponentModel.CDI,
	unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface IngestionPayloadMapper {

	static List<String> getDocumentTypes(IngestionPayload ingestionPayload) {
		return Arrays.asList(IngestionUtils.getDocumentTypes(
			ingestionPayload.getDatasourcePayload())
		);
	}


	default DataPayload map(IngestionIndexWriterPayload payload) {
		return map(payload.getIngestionPayload());
	}

	@Mappings({
		@Mapping(
			target = "rest",
			source = "datasourcePayload"
		),
		@Mapping(
			target = "type",
			source = "."
		)
	})
	DataPayload map(IngestionPayload ingestionPayload);

	@InheritInverseConfiguration
	IngestionPayload map(DataPayload dataPayload);

	@Mappings(
		{
			@Mapping(
				target = "rest",
				source = "ingestionPayload.datasourcePayload"
			),
			@Mapping(
				target = "documentTypes",
				source = "documentTypes"
			),
			@Mapping(
				target = "type",
				source = "ingestionPayload"
			)
		}
	)
	DataPayload map(IngestionPayload ingestionPayload, List<String> documentTypes);

	default PayloadType mapType(IngestionPayload ingestionPayload) {
		var last = ingestionPayload.isLast();
		var type = ingestionPayload.getType();

		return type != null
			? type
			: last ? PayloadType.LAST : PayloadType.DOCUMENT;


	}

}
