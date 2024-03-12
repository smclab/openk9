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

import io.openk9.common.util.IngestionUtils;
import io.openk9.datasource.processor.payload.DataPayload;
import io.openk9.datasource.processor.payload.IngestionPayload;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.Arrays;
import java.util.List;

@Mapper(componentModel = "cdi")
public interface IngestionPayloadMapper {

	static List<String> getDocumentTypes(IngestionPayload ingestionPayload) {
		return Arrays.asList(IngestionUtils.getDocumentTypes(
			ingestionPayload.getDatasourcePayload())
		);
	}

	@Mapping(
		target = "rest",
		source = "datasourcePayload"
	)
	DataPayload map(IngestionPayload ingestionPayload);

	@Mappings(
		{
			@Mapping(
				target = "rest",
				source = "ingestionPayload.datasourcePayload"
			),
			@Mapping(
				target = "documentTypes",
				source = "documentTypes"
			)
		}
	)
	DataPayload map(IngestionPayload ingestionPayload, List<String> documentTypes);

}
