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

package io.openk9.tenantmanager.resource;

import io.openk9.app.manager.grpc.AppManifest;
import io.openk9.datasource.grpc.CreatePresetPluginDriverRequest;
import io.openk9.datasource.grpc.Preset;
import io.openk9.datasource.grpc.PresetPluginDrivers;
import io.vertx.core.json.JsonObject;

public class Constants {
	static final String CREATE_CONNECTOR_PATH = "/connector";
	private static final String TENANT_NAME = "mew";
	public static final String INVALID_PRESET = "INVALID_VALUE";
	static final CreatePresetPluginDriverRequest CREATE_PRESET_REQUEST =
		CreatePresetPluginDriverRequest.newBuilder()
			.setSchemaName(TENANT_NAME)
			.setPreset(Preset.CRAWLER)
			.build();
	private static final String PRESET_VALUE = "CRAWLER";
	private static final String CONNECTOR_NAME = String.format(
		"%s-%s-%s",
		TENANT_NAME,
		PresetPluginDrivers.CONNECTOR_MAP.get(Preset.CRAWLER),
		"1_x_x"
	);
	private static final String CHART_VERSION = "1.x.x";
	private static final String TENANT_NAME_KEY = "tenantName";
	static final AppManifest APP_MANIFEST = AppManifest.newBuilder()
		.setSchemaName(TENANT_NAME)
		.setChart(PresetPluginDrivers.CONNECTOR_MAP.get(Preset.CRAWLER))
		.setVersion(CHART_VERSION)
		.build();
	private static final String PRESET_KEY = "preset";
	static final String VALID_JSON_BODY = JsonObject.of(
		TENANT_NAME_KEY, TENANT_NAME,
		PRESET_KEY, PRESET_VALUE
	).toString();

	static final String INVALID_JSON_BODY = JsonObject.of(
		PRESET_KEY, INVALID_PRESET
	).toString();

}
