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

package io.openk9.datasource.grpc.mapper;

import io.openk9.datasource.grpc.CreatePluginDriverRequest;
import io.openk9.datasource.grpc.PluginDriverType;
import io.openk9.datasource.model.PluginDriver;
import io.openk9.datasource.model.dto.base.PluginDriverDTO;
import io.openk9.datasource.plugindriver.HttpPluginDriverInfo;
import io.vertx.core.json.JsonObject;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ValueMapping;
import org.mapstruct.ValueMappings;

import java.util.Optional;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface PluginDriverMapper {

	default PluginDriverDTO map(CreatePluginDriverRequest source) {
		var infoBuilder = HttpPluginDriverInfo.builder()
			.secure(Boolean.parseBoolean(source.getSecure()))
			.baseUri(source.getBaseUri())
			.path(source.getPath());

		Optional<HttpPluginDriverInfo.Method> method =
			HttpPluginDriverInfo.Method.fromString(source.getMethod());

		method.ifPresent(infoBuilder::method);

		HttpPluginDriverInfo pluginDriverInfo = infoBuilder.build();

		var jsonConfig = JsonObject.mapFrom(pluginDriverInfo);

		return PluginDriverDTO.builder()
			.name(source.getName())
			.description(source.getDescription())
			.type(map(source.getType()))
			.jsonConfig(jsonConfig.encode())
			.build();
	}

	@ValueMappings(
		@ValueMapping(source = "HTTP", target = "HTTP")
	)
	PluginDriver.PluginDriverType map(PluginDriverType source);

}
