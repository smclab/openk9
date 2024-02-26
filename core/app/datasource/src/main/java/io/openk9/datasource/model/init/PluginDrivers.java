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

import io.openk9.datasource.model.PluginDriver;
import io.openk9.datasource.model.dto.PluginDriverDTO;
import io.openk9.datasource.model.dto.util.K9EntityDTO;

import java.util.EnumMap;

public class PluginDrivers {

	public static final EnumMap<Preset, PluginDriverDTO> DRIVER_ENUM_MAP =
		new EnumMap<>(Preset.class);

	public static final EnumMap<Preset, K9EntityDTO> DESCRIPTION_ENUM_MAP =
		new EnumMap<>(Preset.class);

	static {
		DESCRIPTION_ENUM_MAP.put(Preset.LIFERAY, Liferay.K9_ENTITY);
		DESCRIPTION_ENUM_MAP.put(Preset.CRAWLER, Crawler.K9_ENTITY);
		DESCRIPTION_ENUM_MAP.put(Preset.EMAIL, Email.K9_ENTITY);
		DESCRIPTION_ENUM_MAP.put(Preset.GITLAB, Gitlab.K9_ENTITY);
		DESCRIPTION_ENUM_MAP.put(Preset.SITEMAP, Sitemap.K9_ENTITY);
		DESCRIPTION_ENUM_MAP.put(Preset.DATABASE, Database.K9_ENTITY);

		DRIVER_ENUM_MAP.put(Preset.LIFERAY, Liferay.PLUGIN_DRIVER_DTO);
		DRIVER_ENUM_MAP.put(Preset.CRAWLER, Crawler.PLUGIN_DRIVER_DTO);
		DRIVER_ENUM_MAP.put(Preset.EMAIL, Email.PLUGIN_DRIVER_DTO);
		DRIVER_ENUM_MAP.put(Preset.GITLAB, Gitlab.PLUGIN_DRIVER_DTO);
		DRIVER_ENUM_MAP.put(Preset.SITEMAP, Sitemap.PLUGIN_DRIVER_DTO);
		DRIVER_ENUM_MAP.put(Preset.DATABASE, Database.PLUGIN_DRIVER_DTO);
	}

	public enum Preset {
		LIFERAY,
		CRAWLER,
		EMAIL,
		GITLAB,
		SITEMAP,
		DATABASE
	}

	public static class Liferay {
		public static final String NAME = "Liferay";
		public static final String DESCRIPTION = "Plugin Driver for Liferay Portal";
		public static final PluginDriver.PluginDriverType TYPE = PluginDriver.PluginDriverType.HTTP;
		public static final PluginDriver.Provisioning PROVISIONING =
			PluginDriver.Provisioning.SYSTEM;
		public static final String JSON_CONFIG = """
			{
				"host": "openk9-liferay-parser",
				"port": 5000,
				"secure": false,
				"path": "/execute",
				"method": "POST"
			}
			""";

		static final K9EntityDTO K9_ENTITY = K9EntityDTO.builder()
			.name(NAME)
			.description(DESCRIPTION)
			.build();

		static final PluginDriverDTO PLUGIN_DRIVER_DTO = PluginDriverDTO.builder()
			.name(NAME)
			.description(DESCRIPTION)
			.type(TYPE)
			.provisioning(PluginDriver.Provisioning.SYSTEM)
			.jsonConfig(JSON_CONFIG)
			.build();

	}

	public static class Crawler {
		public static final String NAME = "Crawler";
		public static final String DESCRIPTION = "Plugin Driver for Generic Crawling";
		public static final PluginDriver.PluginDriverType TYPE = PluginDriver.PluginDriverType.HTTP;
		public static final PluginDriver.Provisioning PROVISIONING =
			PluginDriver.Provisioning.SYSTEM;
		public static final String JSON_CONFIG = """
			{
				"host": "openk9-web-parser",
				"port": 5000,
				"secure": false,
				"path": "/startUrlsCrawling",
				"method": "POST"
			}
			""";

		static final K9EntityDTO K9_ENTITY = K9EntityDTO.builder()
			.name(NAME)
			.description(DESCRIPTION)
			.build();

		static final PluginDriverDTO PLUGIN_DRIVER_DTO = PluginDriverDTO.builder()
			.name(NAME)
			.description(DESCRIPTION)
			.type(TYPE)
			.provisioning(PluginDriver.Provisioning.SYSTEM)
			.jsonConfig(JSON_CONFIG)
			.build();

	}

	public static class Email {
		public static final String NAME = "Email";
		public static final String DESCRIPTION = "Plugin Driver for Imap Server";
		public static final PluginDriver.PluginDriverType TYPE = PluginDriver.PluginDriverType.HTTP;
		public static final PluginDriver.Provisioning PROVISIONING =
			PluginDriver.Provisioning.SYSTEM;
		public static final String JSON_CONFIG = """
			{
				"host": "openk9-email-parser",
				"port": 5000,
				"secure": false,
				"path": "/execute",
				"method": "POST"
			}
			""";

		static final K9EntityDTO K9_ENTITY = K9EntityDTO.builder()
			.name(NAME)
			.description(DESCRIPTION)
			.build();

		static final PluginDriverDTO PLUGIN_DRIVER_DTO = PluginDriverDTO.builder()
			.name(NAME)
			.description(DESCRIPTION)
			.type(TYPE)
			.provisioning(PluginDriver.Provisioning.SYSTEM)
			.jsonConfig(JSON_CONFIG)
			.build();

	}

	public static class Gitlab {
		public static final String NAME = "Gitlab";
		public static final String DESCRIPTION = "Plugin Driver for Gitlab Server";
		public static final PluginDriver.PluginDriverType TYPE = PluginDriver.PluginDriverType.HTTP;
		public static final PluginDriver.Provisioning PROVISIONING =
			PluginDriver.Provisioning.SYSTEM;
		public static final String JSON_CONFIG = """
			{
				"host": "openk9-gitlab-parser",
				"port": 5000,
				"secure": false,
				"path": "/execute",
				"method": "POST"
			}
			""";

		static final K9EntityDTO K9_ENTITY = K9EntityDTO.builder()
			.name(NAME)
			.description(DESCRIPTION)
			.build();

		static final PluginDriverDTO PLUGIN_DRIVER_DTO = PluginDriverDTO.builder()
			.name(NAME)
			.description(DESCRIPTION)
			.type(TYPE)
			.provisioning(PluginDriver.Provisioning.SYSTEM)
			.jsonConfig(JSON_CONFIG)
			.build();

	}

	public static class Sitemap {
		public static final String NAME = "Sitemap";
		public static final String DESCRIPTION = "Plugin Driver for Sitemap Server";
		public static final PluginDriver.PluginDriverType TYPE = PluginDriver.PluginDriverType.HTTP;
		public static final PluginDriver.Provisioning PROVISIONING =
			PluginDriver.Provisioning.SYSTEM;
		public static final String JSON_CONFIG = """
			{
				"host": "openk9-web-parser",
				"port": 5000,
				"secure": false,
				"path": "/startSitemapCrawling",
				"method": "POST"
			}
			""";

		static final K9EntityDTO K9_ENTITY = K9EntityDTO.builder()
			.name(NAME)
			.description(DESCRIPTION)
			.build();

		static final PluginDriverDTO PLUGIN_DRIVER_DTO = PluginDriverDTO.builder()
			.name(NAME)
			.description(DESCRIPTION)
			.type(TYPE)
			.provisioning(PluginDriver.Provisioning.SYSTEM)
			.jsonConfig(JSON_CONFIG)
			.build();

	}

	public static class Database {
		public static final String NAME = "Gitlab";
		public static final String DESCRIPTION =
			"Plugin Driver for Database. Supporting PostgreSql, Mysql, MariaDB.";
		public static final PluginDriver.PluginDriverType TYPE = PluginDriver.PluginDriverType.HTTP;
		public static final PluginDriver.Provisioning PROVISIONING =
			PluginDriver.Provisioning.SYSTEM;
		public static final String JSON_CONFIG = """
			{
				"host": "openk9-database-parser",
				"port": 5000,
				"secure": false,
				"path": "/execute",
				"method": "POST"
			}
			""";

		static final K9EntityDTO K9_ENTITY = K9EntityDTO.builder()
			.name(NAME)
			.description(DESCRIPTION)
			.build();

		static final PluginDriverDTO PLUGIN_DRIVER_DTO = PluginDriverDTO.builder()
			.name(NAME)
			.description(DESCRIPTION)
			.type(TYPE)
			.provisioning(PluginDriver.Provisioning.SYSTEM)
			.jsonConfig(JSON_CONFIG)
			.build();

	}

}
