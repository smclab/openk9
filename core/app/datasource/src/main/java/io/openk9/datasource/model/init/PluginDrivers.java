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

package io.openk9.datasource.model.init;

import java.util.EnumMap;
import java.util.Map;

import io.openk9.datasource.grpc.Preset;
import io.openk9.datasource.grpc.PresetPluginDrivers;
import io.openk9.datasource.model.PluginDriver;
import io.openk9.datasource.model.ResourceUri;
import io.openk9.datasource.model.dto.base.PluginDriverDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class PluginDrivers {

	private static final Map<Preset, PresetConfiguration> CONFIGURATION_MAP = new EnumMap<>(Preset.class);
	private static final String PORT = "5000";

	static {
		CONFIGURATION_MAP.put(
			Preset.YOUTUBE,
			PresetConfiguration.builder()
				.name("Youtube")
				.description("Plugin Driver for Youtube Channel")
				.type(PluginDriver.PluginDriverType.HTTP)
				.provisioning(PluginDriver.Provisioning.SYSTEM)
				.resourceUri(ResourceUri.builder()
					.baseUri(PresetPluginDrivers.getPluginDriver(Preset.YOUTUBE))
					.path("/execute")
					.build()
				)
				.build()
		);
		CONFIGURATION_MAP.put(
			Preset.CRAWLER,
			PresetConfiguration.builder()
				.name("Crawler")
				.description("Plugin Driver for Generic Crawling")
				.type(PluginDriver.PluginDriverType.HTTP)
				.provisioning(PluginDriver.Provisioning.SYSTEM)
				.resourceUri(ResourceUri.builder()
					.baseUri(PresetPluginDrivers.getPluginDriver(Preset.CRAWLER))
					.path("/startUrlsCrawling")
					.build()
				)
				.build()
		);
		CONFIGURATION_MAP.put(
			Preset.EMAIL,
			PresetConfiguration.builder()
				.name("Email")
				.description("Plugin Driver for Imap Server")
				.type(PluginDriver.PluginDriverType.HTTP)
				.provisioning(PluginDriver.Provisioning.SYSTEM)
				.resourceUri(ResourceUri.builder()
					.baseUri(PresetPluginDrivers.getPluginDriver(Preset.EMAIL))
					.path("/execute")
					.build()
				)
				.build()
		);
		CONFIGURATION_MAP.put(
			Preset.GITLAB,
			PresetConfiguration.builder()
				.name("Gitlab")
				.description("Plugin Driver for Gitlab Server")
				.type(PluginDriver.PluginDriverType.HTTP)
				.provisioning(PluginDriver.Provisioning.SYSTEM)
				.resourceUri(ResourceUri.builder()
					.baseUri(PresetPluginDrivers.getPluginDriver(Preset.GITLAB))
					.path("/execute")
					.build()
				)
				.build()
		);
		CONFIGURATION_MAP.put(
			Preset.SITEMAP,
			PresetConfiguration.builder()
				.name("Sitemap")
				.description("Plugin Driver for Sitemap Server")
				.type(PluginDriver.PluginDriverType.HTTP)
				.provisioning(PluginDriver.Provisioning.SYSTEM)
				.resourceUri(ResourceUri.builder()
					.baseUri(PresetPluginDrivers.getPluginDriver(Preset.SITEMAP))
					.path("/startSitemapCrawling")
					.build()
				)
				.build()
		);
		CONFIGURATION_MAP.put(
			Preset.DATABASE,
			PresetConfiguration.builder()
				.name("Database")
				.description("Plugin Driver for Database. Supporting PostgreSql, Mysql, MariaDB.")
				.type(PluginDriver.PluginDriverType.HTTP)
				.provisioning(PluginDriver.Provisioning.SYSTEM)
				.resourceUri(ResourceUri.builder()
					.baseUri(PresetPluginDrivers.getPluginDriver(Preset.DATABASE))
					.path("/execute")
					.build()
				)
				.build()
		);
		CONFIGURATION_MAP.put(
			Preset.MINIO,
			PresetConfiguration.builder()
				.name("Minio")
				.description("Plugin Driver for Minio.")
				.type(PluginDriver.PluginDriverType.HTTP)
				.provisioning(PluginDriver.Provisioning.SYSTEM)
				.resourceUri(ResourceUri.builder()
					.baseUri(PresetPluginDrivers.getPluginDriver(Preset.MINIO))
					.path("/execute")
					.build()
				)
				.build()
		);
	}

	public static PluginDriverDTO getPluginDriverDTO(Preset preset) {
		return createPluginDriverDTO(null, preset);
	}

	public static PluginDriverDTO getPluginDriverDTO(String schemaName, Preset preset) {
		return createPluginDriverDTO(schemaName, preset);
	}

	private static ResourceUri buildResourceUri(PresetConfiguration presetConfiguration, String schemaName) {
		StringBuilder baseUri  = new StringBuilder(presetConfiguration.resourceUri.getBaseUri());

		if (schemaName != null) {
			baseUri.append("-");
			baseUri.append(schemaName);
		}

		baseUri.append(":");
		baseUri.append(PORT);

		return ResourceUri.builder()
			.baseUri(baseUri.toString())
			.path(presetConfiguration.resourceUri.getPath())
			.build();
	}

	private static PluginDriverDTO createPluginDriverDTO(String schemaName, Preset preset) {
		PresetConfiguration presetConfiguration = CONFIGURATION_MAP.get(preset);

		var resourceUri = buildResourceUri(presetConfiguration, schemaName);

		return PluginDriverDTO.builder()
			.name(presetConfiguration.getName())
			.description(presetConfiguration.getDescription())
			.type(presetConfiguration.getType())
			.provisioning(presetConfiguration.getProvisioning())
			.resourceUri(resourceUri)
			.build();
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	private static class PresetConfiguration {
		private String description;
		private String name;
		private ResourceUri resourceUri;
		private PluginDriver.Provisioning provisioning;
		private PluginDriver.PluginDriverType type;
	}

}
