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

package io.openk9.datasource.grpc;

import java.util.EnumMap;
import java.util.Map;

public class PresetPluginDrivers {

	private static final Map<Preset, String> CONNECTOR_MAP = new EnumMap<>(Preset.class);

	static {
		CONNECTOR_MAP.put(Preset.YOUTUBE, "openk9-youtube-connector");
		CONNECTOR_MAP.put(Preset.CRAWLER, "openk9-web-connector");
		CONNECTOR_MAP.put(Preset.EMAIL, "openk9-email-connector");
		CONNECTOR_MAP.put(Preset.GITLAB, "openk9-gitlab-connector");
		CONNECTOR_MAP.put(Preset.SITEMAP, "openk9-web-connector");
		CONNECTOR_MAP.put(Preset.DATABASE, "openk9-database-connector");
	}

	public static String getPluginDriver(Preset preset) {
		return CONNECTOR_MAP.get(preset);
	}
	private PresetPluginDrivers() {}

}
