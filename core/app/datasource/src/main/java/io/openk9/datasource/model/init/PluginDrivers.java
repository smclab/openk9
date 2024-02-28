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

import io.openk9.datasource.grpc.Preset;
import io.openk9.datasource.grpc.PresetPluginDrivers;
import io.openk9.datasource.model.PluginDriver;
import io.openk9.datasource.model.dto.PluginDriverDTO;
import io.openk9.datasource.model.dto.util.K9EntityDTO;
import io.openk9.datasource.plugindriver.HttpPluginDriverInfo;
import io.vertx.core.json.Json;

import java.util.EnumMap;

public class PluginDrivers {

	private static final EnumMap<Preset, PluginDriverDTO> PRESET_PLUGIN_DRIVER_DTO_ENUM_MAP =
		new EnumMap<>(Preset.class);

	private static final EnumMap<Preset, K9EntityDTO> PRESET_K9_ENTITY_DTO_MAP =
		new EnumMap<>(Preset.class);

	static {
		PRESET_K9_ENTITY_DTO_MAP.put(Preset.LIFERAY, Liferay.K9_ENTITY);
		PRESET_K9_ENTITY_DTO_MAP.put(Preset.CRAWLER, Crawler.K9_ENTITY);
		PRESET_K9_ENTITY_DTO_MAP.put(Preset.EMAIL, Email.K9_ENTITY);
		PRESET_K9_ENTITY_DTO_MAP.put(Preset.GITLAB, Gitlab.K9_ENTITY);
		PRESET_K9_ENTITY_DTO_MAP.put(Preset.SITEMAP, Sitemap.K9_ENTITY);
		PRESET_K9_ENTITY_DTO_MAP.put(Preset.DATABASE, Database.K9_ENTITY);

		PRESET_PLUGIN_DRIVER_DTO_ENUM_MAP.put(Preset.LIFERAY, Liferay.PLUGIN_DRIVER_DTO);
		PRESET_PLUGIN_DRIVER_DTO_ENUM_MAP.put(Preset.CRAWLER, Crawler.PLUGIN_DRIVER_DTO);
		PRESET_PLUGIN_DRIVER_DTO_ENUM_MAP.put(Preset.EMAIL, Email.PLUGIN_DRIVER_DTO);
		PRESET_PLUGIN_DRIVER_DTO_ENUM_MAP.put(Preset.GITLAB, Gitlab.PLUGIN_DRIVER_DTO);
		PRESET_PLUGIN_DRIVER_DTO_ENUM_MAP.put(Preset.SITEMAP, Sitemap.PLUGIN_DRIVER_DTO);
		PRESET_PLUGIN_DRIVER_DTO_ENUM_MAP.put(Preset.DATABASE, Database.PLUGIN_DRIVER_DTO);
	}

	public static PluginDriverDTO getPresetPluginDriver(Preset preset) {
		return PRESET_PLUGIN_DRIVER_DTO_ENUM_MAP.get(preset);
	}

	public static EnumMap<Preset, K9EntityDTO> getPresets() {
		return PRESET_K9_ENTITY_DTO_MAP;
	}

	public static class Liferay {
		public static final String NAME = "Liferay";
		public static final String DESCRIPTION = "Plugin Driver for Liferay Portal";
		public static final PluginDriver.PluginDriverType TYPE = PluginDriver.PluginDriverType.HTTP;
		public static final PluginDriver.Provisioning PROVISIONING =
			PluginDriver.Provisioning.SYSTEM;
		public static final HttpPluginDriverInfo JSON_CONFIG = HttpPluginDriverInfo.builder()
			.host(PresetPluginDrivers.CONNECTOR_MAP.get(Preset.LIFERAY))
			.port(5000)
			.secure(false)
			.path("/execute")
			.method(HttpPluginDriverInfo.Method.POST)
			.build();

		static final K9EntityDTO K9_ENTITY = K9EntityDTO.builder()
			.name(NAME)
			.description(DESCRIPTION)
			.build();

		static final PluginDriverDTO PLUGIN_DRIVER_DTO = PluginDriverDTO.builder()
			.name(NAME)
			.description(DESCRIPTION)
			.type(TYPE)
			.provisioning(PluginDriver.Provisioning.SYSTEM)
			.jsonConfig(Json.encode(JSON_CONFIG))
			.build();

	}

	public static class Crawler {
		public static final String NAME = "Crawler";
		public static final String DESCRIPTION = "Plugin Driver for Generic Crawling";
		public static final PluginDriver.PluginDriverType TYPE = PluginDriver.PluginDriverType.HTTP;
		public static final PluginDriver.Provisioning PROVISIONING =
			PluginDriver.Provisioning.SYSTEM;

		public static final HttpPluginDriverInfo JSON_CONFIG = HttpPluginDriverInfo.builder()
			.host(PresetPluginDrivers.CONNECTOR_MAP.get(Preset.CRAWLER))
			.port(5000)
			.secure(false)
			.path("/startUrlsCrawling")
			.method(HttpPluginDriverInfo.Method.POST)
			.build();

		static final K9EntityDTO K9_ENTITY = K9EntityDTO.builder()
			.name(NAME)
			.description(DESCRIPTION)
			.build();

		static final PluginDriverDTO PLUGIN_DRIVER_DTO = PluginDriverDTO.builder()
			.name(NAME)
			.description(DESCRIPTION)
			.type(TYPE)
			.provisioning(PluginDriver.Provisioning.SYSTEM)
			.jsonConfig(Json.encode(JSON_CONFIG))
			.build();

	}

	public static class Email {
		public static final String NAME = "Email";
		public static final String DESCRIPTION = "Plugin Driver for Imap Server";
		public static final PluginDriver.PluginDriverType TYPE = PluginDriver.PluginDriverType.HTTP;
		public static final PluginDriver.Provisioning PROVISIONING =
			PluginDriver.Provisioning.SYSTEM;
		public static final HttpPluginDriverInfo JSON_CONFIG = HttpPluginDriverInfo.builder()
			.host(PresetPluginDrivers.CONNECTOR_MAP.get(Preset.EMAIL))
			.port(5000)
			.secure(false)
			.path("/execute")
			.method(HttpPluginDriverInfo.Method.POST)
			.build();

		static final K9EntityDTO K9_ENTITY = K9EntityDTO.builder()
			.name(NAME)
			.description(DESCRIPTION)
			.build();

		static final PluginDriverDTO PLUGIN_DRIVER_DTO = PluginDriverDTO.builder()
			.name(NAME)
			.description(DESCRIPTION)
			.type(TYPE)
			.provisioning(PluginDriver.Provisioning.SYSTEM)
			.jsonConfig(Json.encode(JSON_CONFIG))
			.build();

	}

	public static class Gitlab {
		public static final String NAME = "Gitlab";
		public static final String DESCRIPTION = "Plugin Driver for Gitlab Server";
		public static final PluginDriver.PluginDriverType TYPE = PluginDriver.PluginDriverType.HTTP;
		public static final PluginDriver.Provisioning PROVISIONING =
			PluginDriver.Provisioning.SYSTEM;
		public static final HttpPluginDriverInfo JSON_CONFIG = HttpPluginDriverInfo.builder()
			.host(PresetPluginDrivers.CONNECTOR_MAP.get(Preset.GITLAB))
			.port(5000)
			.secure(false)
			.path("/execute")
			.method(HttpPluginDriverInfo.Method.POST)
			.build();

		static final K9EntityDTO K9_ENTITY = K9EntityDTO.builder()
			.name(NAME)
			.description(DESCRIPTION)
			.build();

		static final PluginDriverDTO PLUGIN_DRIVER_DTO = PluginDriverDTO.builder()
			.name(NAME)
			.description(DESCRIPTION)
			.type(TYPE)
			.provisioning(PluginDriver.Provisioning.SYSTEM)
			.jsonConfig(Json.encode(JSON_CONFIG))
			.build();

	}

	public static class Sitemap {
		public static final String NAME = "Sitemap";
		public static final String DESCRIPTION = "Plugin Driver for Sitemap Server";
		public static final PluginDriver.PluginDriverType TYPE = PluginDriver.PluginDriverType.HTTP;
		public static final PluginDriver.Provisioning PROVISIONING =
			PluginDriver.Provisioning.SYSTEM;
		public static final HttpPluginDriverInfo JSON_CONFIG = HttpPluginDriverInfo.builder()
			.host(PresetPluginDrivers.CONNECTOR_MAP.get(Preset.SITEMAP))
			.port(5000)
			.secure(false)
			.path("/startSitemapCrawling")
			.method(HttpPluginDriverInfo.Method.POST)
			.build();

		static final K9EntityDTO K9_ENTITY = K9EntityDTO.builder()
			.name(NAME)
			.description(DESCRIPTION)
			.build();

		static final PluginDriverDTO PLUGIN_DRIVER_DTO = PluginDriverDTO.builder()
			.name(NAME)
			.description(DESCRIPTION)
			.type(TYPE)
			.provisioning(PluginDriver.Provisioning.SYSTEM)
			.jsonConfig(Json.encode(JSON_CONFIG))
			.build();

	}

	public static class Database {
		public static final String NAME = "Database";
		public static final String DESCRIPTION =
			"Plugin Driver for Database. Supporting PostgreSql, Mysql, MariaDB.";
		public static final PluginDriver.PluginDriverType TYPE = PluginDriver.PluginDriverType.HTTP;
		public static final PluginDriver.Provisioning PROVISIONING =
			PluginDriver.Provisioning.SYSTEM;

		public static final HttpPluginDriverInfo JSON_CONFIG = HttpPluginDriverInfo.builder()
			.host(PresetPluginDrivers.CONNECTOR_MAP.get(Preset.DATABASE))
			.port(5000)
			.secure(false)
			.path("/execute")
			.method(HttpPluginDriverInfo.Method.POST)
			.build();

		static final K9EntityDTO K9_ENTITY = K9EntityDTO.builder()
			.name(NAME)
			.description(DESCRIPTION)
			.build();

		static final PluginDriverDTO PLUGIN_DRIVER_DTO = PluginDriverDTO.builder()
			.name(NAME)
			.description(DESCRIPTION)
			.type(TYPE)
			.provisioning(PluginDriver.Provisioning.SYSTEM)
			.jsonConfig(Json.encode(JSON_CONFIG))
			.build();

	}

}
