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

import io.openk9.common.util.StringUtils;
import io.openk9.datasource.grpc.Preset;
import io.openk9.datasource.grpc.PresetPluginDrivers;
import io.openk9.datasource.model.PluginDriver;
import io.openk9.datasource.model.dto.base.K9EntityDTO;
import io.openk9.datasource.model.dto.base.PluginDriverDTO;
import io.openk9.datasource.plugindriver.HttpPluginDriverInfo;

import io.vertx.core.json.Json;

public class PluginDrivers {

	private static final EnumMap<Preset, PluginDriverPreset> PLUGIN_DRIVER_PRESET_MAP =
		new EnumMap<>(Preset.class);
	public static final String PORT = ":5000";

	private static final EnumMap<Preset, K9EntityDTO> PRESET_K9_ENTITY_DTO_MAP =
		new EnumMap<>(Preset.class);

	static {
		PRESET_K9_ENTITY_DTO_MAP.put(Preset.LIFERAY, Liferay.K9_ENTITY);
		PRESET_K9_ENTITY_DTO_MAP.put(Preset.CRAWLER, Crawler.K9_ENTITY);
		PRESET_K9_ENTITY_DTO_MAP.put(Preset.EMAIL, Email.K9_ENTITY);
		PRESET_K9_ENTITY_DTO_MAP.put(Preset.GITLAB, Gitlab.K9_ENTITY);
		PRESET_K9_ENTITY_DTO_MAP.put(Preset.SITEMAP, Sitemap.K9_ENTITY);
		PRESET_K9_ENTITY_DTO_MAP.put(Preset.DATABASE, Database.K9_ENTITY);

		PLUGIN_DRIVER_PRESET_MAP.put(Preset.LIFERAY, Liferay.INSTANCE);
		PLUGIN_DRIVER_PRESET_MAP.put(Preset.CRAWLER, Crawler.INSTANCE);
		PLUGIN_DRIVER_PRESET_MAP.put(Preset.EMAIL, Email.INSTANCE);
		PLUGIN_DRIVER_PRESET_MAP.put(Preset.GITLAB, Gitlab.INSTANCE);
		PLUGIN_DRIVER_PRESET_MAP.put(Preset.SITEMAP, Sitemap.INSTANCE);
		PLUGIN_DRIVER_PRESET_MAP.put(Preset.DATABASE, Database.INSTANCE);
	}

	public static PluginDriverDTO getPluginDriverDTO(String schemaName, Preset preset) {
		return PLUGIN_DRIVER_PRESET_MAP.get(preset).getPresetByTenant(schemaName);
	}

	public static PluginDriverDTO getPluginDriverDTO(Preset preset) {
		return PLUGIN_DRIVER_PRESET_MAP.get(preset).getPreset();
	}

	public static EnumMap<Preset, K9EntityDTO> getPresets() {
		return PRESET_K9_ENTITY_DTO_MAP;
	}

	private sealed interface PluginDriverPreset {
		PluginDriverDTO getPreset();

		PluginDriverDTO getPresetByTenant(String tenant);

	}

	private static final class Liferay implements PluginDriverPreset {
		static final Liferay INSTANCE = new Liferay();

		private static final HttpPluginDriverInfo JSON_CONFIG = HttpPluginDriverInfo.builder()
			.baseUri(PresetPluginDrivers.getPluginDriver(Preset.LIFERAY))
			.secure(false)
			.path("/execute")
			.method(HttpPluginDriverInfo.Method.POST)
			.build();
		private static final String NAME = "Liferay";
		private static final String DESCRIPTION = "Plugin Driver for Liferay Portal";
		static final K9EntityDTO K9_ENTITY = K9EntityDTO.builder()
			.name(NAME)
			.description(DESCRIPTION)
			.build();
		private static final PluginDriver.PluginDriverType TYPE =
			PluginDriver.PluginDriverType.HTTP;
		private static final PluginDriverDTO PLUGIN_DRIVER_DTO = PluginDriverDTO.builder()
			.name(NAME)
			.description(DESCRIPTION)
			.type(TYPE)
			.provisioning(PluginDriver.Provisioning.SYSTEM)
			.jsonConfig(Json.encode(JSON_CONFIG))
			.build();

		private Liferay() {}

		@Override
		public PluginDriverDTO getPreset() {
			return getPresetInternal(null);
		}

		@Override
		public PluginDriverDTO getPresetByTenant(String tenant) {
			return getPresetInternal(tenant);
		}

		/**
		 * Builds a {@link PluginDriverDTO} with a modified JSON configuration URI.
		 *
		 * <p>This method constructs the base URI by appending the given tenant and a port to the default base URI.
		 * It then updates the JSON configuration with this constructed URI and returns a new instance of
		 * {@code PluginDriverDTO} containing the encoded configuration.
		 *
		 * @param tenant the tenant identifier to append to the base URI, or {@code null} if no tenant is used
		 * @return a {@code PluginDriverDTO} with the updated JSON configuration
		 *
		 * @throws NullPointerException if {@code JSON_CONFIG} or {@code PLUGIN_DRIVER_DTO} is {@code null}
		 */
		private PluginDriverDTO getPresetInternal(String tenant) {
			StringBuilder baseUri =
				new StringBuilder(JSON_CONFIG.getBaseUri());

			if (tenant != null) {
				baseUri.append(tenant);
			}

			baseUri.append(PORT);

			var jsonConfig = JSON_CONFIG.withBaseUri(baseUri.toString());

			return PLUGIN_DRIVER_DTO.toBuilder().jsonConfig(Json.encode(jsonConfig)).build();
		}
	}

	private static final class Crawler implements PluginDriverPreset {
		static final Crawler INSTANCE = new Crawler();
		private static final HttpPluginDriverInfo JSON_CONFIG = HttpPluginDriverInfo.builder()
			.baseUri(PresetPluginDrivers.getPluginDriver(Preset.CRAWLER))
			.secure(false)
			.path("/startUrlsCrawling")
			.method(HttpPluginDriverInfo.Method.POST)
			.build();
		private static final String NAME = "Crawler";
		private static final String DESCRIPTION = "Plugin Driver for Generic Crawling";
		static final K9EntityDTO K9_ENTITY = K9EntityDTO.builder()
			.name(NAME)
			.description(DESCRIPTION)
			.build();
		private static final PluginDriver.PluginDriverType TYPE =
			PluginDriver.PluginDriverType.HTTP;
		private static final PluginDriverDTO PLUGIN_DRIVER_DTO = PluginDriverDTO.builder()
			.name(NAME)
			.description(DESCRIPTION)
			.type(TYPE)
			.provisioning(PluginDriver.Provisioning.SYSTEM)
			.jsonConfig(Json.encode(JSON_CONFIG))
			.build();

		private Crawler() {}

		@Override
		public PluginDriverDTO getPreset() {
			return getPresetInternal(null);
		}

		@Override
		public PluginDriverDTO getPresetByTenant(String tenant) {
			return getPresetInternal(tenant);
		}

		/**
		 * Builds a {@link PluginDriverDTO} with a modified JSON configuration URI.
		 *
		 * <p>This method constructs the base URI by appending the given tenant and a port to the default base URI.
		 * It then updates the JSON configuration with this constructed URI and returns a new instance of
		 * {@code PluginDriverDTO} containing the encoded configuration.
		 *
		 * @param tenant the tenant identifier to append to the base URI, or {@code null} if no tenant is used
		 * @return a {@code PluginDriverDTO} with the updated JSON configuration
		 *
		 * @throws NullPointerException if {@code JSON_CONFIG} or {@code PLUGIN_DRIVER_DTO} is {@code null}
		 */
		private PluginDriverDTO getPresetInternal(String tenant) {
			StringBuilder baseUri =
				new StringBuilder(JSON_CONFIG.getBaseUri());

			if (tenant != null) {
				baseUri.append(tenant);
			}

			baseUri.append(PORT);

			var jsonConfig = JSON_CONFIG.withBaseUri(baseUri.toString());

			return PLUGIN_DRIVER_DTO.toBuilder().jsonConfig(Json.encode(jsonConfig)).build();
		}

	}

	private static final class Email implements PluginDriverPreset {
		static final Email INSTANCE = new Email();
		private static final String NAME = "Email";
		private static final String DESCRIPTION = "Plugin Driver for Imap Server";
		static final K9EntityDTO K9_ENTITY = K9EntityDTO.builder()
			.name(NAME)
			.description(DESCRIPTION)
			.build();
		private static final PluginDriver.PluginDriverType TYPE =
			PluginDriver.PluginDriverType.HTTP;
		private static final HttpPluginDriverInfo JSON_CONFIG = HttpPluginDriverInfo.builder()
			.baseUri(PresetPluginDrivers.getPluginDriver(Preset.EMAIL))
			.secure(false)
			.path("/execute")
			.method(HttpPluginDriverInfo.Method.POST)
			.build();
		static final PluginDriverDTO PLUGIN_DRIVER_DTO = PluginDriverDTO.builder()
			.name(NAME)
			.description(DESCRIPTION)
			.type(TYPE)
			.provisioning(PluginDriver.Provisioning.SYSTEM)
			.jsonConfig(Json.encode(JSON_CONFIG))
			.build();

		private Email() {}

		@Override
		public PluginDriverDTO getPreset() {
			return getPresetInternal(null);
		}

		@Override
		public PluginDriverDTO getPresetByTenant(String tenant) {
			return getPresetInternal(tenant);
		}

		/**
		 * Builds a {@link PluginDriverDTO} with a modified JSON configuration URI.
		 *
		 * <p>This method constructs the base URI by appending the given tenant and a port to the default base URI.
		 * It then updates the JSON configuration with this constructed URI and returns a new instance of
		 * {@code PluginDriverDTO} containing the encoded configuration.
		 *
		 * @param tenant the tenant identifier to append to the base URI, or {@code null} if no tenant is used
		 * @return a {@code PluginDriverDTO} with the updated JSON configuration
		 *
		 * @throws NullPointerException if {@code JSON_CONFIG} or {@code PLUGIN_DRIVER_DTO} is {@code null}
		 */
		private PluginDriverDTO getPresetInternal(String tenant) {
			StringBuilder baseUri =
				new StringBuilder(JSON_CONFIG.getBaseUri());

			if (tenant != null) {
				baseUri.append(tenant);
			}

			baseUri.append(PORT);

			var jsonConfig = JSON_CONFIG.withBaseUri(baseUri.toString());

			return PLUGIN_DRIVER_DTO.toBuilder().jsonConfig(Json.encode(jsonConfig)).build();
		}

	}

	private static final class Gitlab implements PluginDriverPreset {
		static final Gitlab INSTANCE = new Gitlab();
		private static final String NAME = "Gitlab";
		private static final String DESCRIPTION = "Plugin Driver for Gitlab Server";
		static final K9EntityDTO K9_ENTITY = K9EntityDTO.builder()
			.name(NAME)
			.description(DESCRIPTION)
			.build();
		private static final PluginDriver.PluginDriverType TYPE =
			PluginDriver.PluginDriverType.HTTP;
		private static final HttpPluginDriverInfo JSON_CONFIG = HttpPluginDriverInfo.builder()
			.baseUri(PresetPluginDrivers.getPluginDriver(Preset.GITLAB))
			.secure(false)
			.path("/execute")
			.method(HttpPluginDriverInfo.Method.POST)
			.build();
		static final PluginDriverDTO PLUGIN_DRIVER_DTO = PluginDriverDTO.builder()
			.name(NAME)
			.description(DESCRIPTION)
			.type(TYPE)
			.provisioning(PluginDriver.Provisioning.SYSTEM)
			.jsonConfig(Json.encode(JSON_CONFIG))
			.build();

		private Gitlab() {}

		@Override
		public PluginDriverDTO getPreset() {
			return getPresetInternal(null);
		}

		@Override
		public PluginDriverDTO getPresetByTenant(String tenant) {
			return getPresetInternal(tenant);
		}

		/**
		 * Builds a {@link PluginDriverDTO} with a modified JSON configuration URI.
		 *
		 * <p>This method constructs the base URI by appending the given tenant and a port to the default base URI.
		 * It then updates the JSON configuration with this constructed URI and returns a new instance of
		 * {@code PluginDriverDTO} containing the encoded configuration.
		 *
		 * @param tenant the tenant identifier to append to the base URI, or {@code null} if no tenant is used
		 * @return a {@code PluginDriverDTO} with the updated JSON configuration
		 *
		 * @throws NullPointerException if {@code JSON_CONFIG} or {@code PLUGIN_DRIVER_DTO} is {@code null}
		 */
		private PluginDriverDTO getPresetInternal(String tenant) {
			StringBuilder baseUri =
				new StringBuilder(JSON_CONFIG.getBaseUri());

			if (tenant != null) {
				baseUri.append(tenant);
			}

			baseUri.append(PORT);

			var jsonConfig = JSON_CONFIG.withBaseUri(baseUri.toString());

			return PLUGIN_DRIVER_DTO.toBuilder().jsonConfig(Json.encode(jsonConfig)).build();
		}

	}

	private static final class Sitemap implements PluginDriverPreset {
		static final Sitemap INSTANCE = new Sitemap();
		private static final String NAME = "Sitemap";
		private static final String DESCRIPTION = "Plugin Driver for Sitemap Server";
		static final K9EntityDTO K9_ENTITY = K9EntityDTO.builder()
			.name(NAME)
			.description(DESCRIPTION)
			.build();
		private static final PluginDriver.PluginDriverType TYPE =
			PluginDriver.PluginDriverType.HTTP;
		private static final HttpPluginDriverInfo JSON_CONFIG = HttpPluginDriverInfo.builder()
			.baseUri(PresetPluginDrivers.getPluginDriver(Preset.SITEMAP))
			.secure(false)
			.path("/startSitemapCrawling")
			.method(HttpPluginDriverInfo.Method.POST)
			.build();
		static final PluginDriverDTO PLUGIN_DRIVER_DTO = PluginDriverDTO.builder()
			.name(NAME)
			.description(DESCRIPTION)
			.type(TYPE)
			.provisioning(PluginDriver.Provisioning.SYSTEM)
			.jsonConfig(Json.encode(JSON_CONFIG))
			.build();

		private Sitemap() {}

		@Override
		public PluginDriverDTO getPreset() {
			return getPresetInternal(null);
		}

		@Override
		public PluginDriverDTO getPresetByTenant(String tenant) {
			return getPresetInternal(tenant);
		}

		/**
		 * Builds a {@link PluginDriverDTO} with a modified JSON configuration URI.
		 *
		 * <p>This method constructs the base URI by appending the given tenant and a port to the default base URI.
		 * It then updates the JSON configuration with this constructed URI and returns a new instance of
		 * {@code PluginDriverDTO} containing the encoded configuration.
		 *
		 * @param tenant the tenant identifier to append to the base URI, or {@code null} if no tenant is used
		 * @return a {@code PluginDriverDTO} with the updated JSON configuration
		 *
		 * @throws NullPointerException if {@code JSON_CONFIG} or {@code PLUGIN_DRIVER_DTO} is {@code null}
		 */
		private PluginDriverDTO getPresetInternal(String tenant) {
			StringBuilder baseUri =
				new StringBuilder(JSON_CONFIG.getBaseUri());

			if (tenant != null) {
				baseUri.append(tenant);
			}

			baseUri.append(PORT);

			var jsonConfig = JSON_CONFIG.withBaseUri(baseUri.toString());

			return PLUGIN_DRIVER_DTO.toBuilder().jsonConfig(Json.encode(jsonConfig)).build();
		}

	}

	private static final class Database implements PluginDriverPreset {
		static final Database INSTANCE = new Database();
		private static final String NAME = "Database";
		private static final String DESCRIPTION =
			"Plugin Driver for Database. Supporting PostgreSql, Mysql, MariaDB.";
		static final K9EntityDTO K9_ENTITY = K9EntityDTO.builder()
			.name(NAME)
			.description(DESCRIPTION)
			.build();
		private static final PluginDriver.PluginDriverType TYPE =
			PluginDriver.PluginDriverType.HTTP;
		private static final HttpPluginDriverInfo JSON_CONFIG = HttpPluginDriverInfo.builder()
			.baseUri(PresetPluginDrivers.getPluginDriver(Preset.DATABASE))
			.secure(false)
			.path("/execute")
			.method(HttpPluginDriverInfo.Method.POST)
			.build();
		static final PluginDriverDTO PLUGIN_DRIVER_DTO = PluginDriverDTO.builder()
			.name(NAME)
			.description(DESCRIPTION)
			.type(TYPE)
			.provisioning(PluginDriver.Provisioning.SYSTEM)
			.jsonConfig(Json.encode(JSON_CONFIG))
			.build();

		private Database() {}

		@Override
		public PluginDriverDTO getPreset() {
			return getPresetInternal(null);
		}

		@Override
		public PluginDriverDTO getPresetByTenant(String tenant) {
			return getPresetInternal(tenant);
		}

		/**
		 * Builds a {@link PluginDriverDTO} with a modified JSON configuration URI.
		 *
		 * <p>This method constructs the base URI by appending the given tenant and a port to the default base URI.
		 * It then updates the JSON configuration with this constructed URI and returns a new instance of
		 * {@code PluginDriverDTO} containing the encoded configuration.
		 *
		 * @param tenant the tenant identifier to append to the base URI, or {@code null} if no tenant is used
		 * @return a {@code PluginDriverDTO} with the updated JSON configuration
		 *
		 * @throws NullPointerException if {@code JSON_CONFIG} or {@code PLUGIN_DRIVER_DTO} is {@code null}
		 */
		private PluginDriverDTO getPresetInternal(String tenant) {
			StringBuilder baseUri =
				new StringBuilder(JSON_CONFIG.getBaseUri());

			if (tenant != null) {
				baseUri.append(tenant);
			}

			baseUri.append(PORT);

			var jsonConfig = JSON_CONFIG.withBaseUri(baseUri.toString());

			return PLUGIN_DRIVER_DTO.toBuilder().jsonConfig(Json.encode(jsonConfig)).build();
		}

	}

}
