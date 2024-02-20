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
import io.openk9.datasource.grpc.CreatePluginDriverRequest;
import io.vertx.core.json.JsonObject;

public class Constants {
	static final String CREATE_CONNECTOR_PATH = "/connector";
	private static final String TENANT_NAME = "mew";
	private static final String CHART_NAME = "openk9-foo-parser";
	private static final String CONNECTOR_NAME = String.format(
		"%s-%s-%s",
		TENANT_NAME,
		CHART_NAME,
		"999_999_999"
	);
	private static final String CHART_VERSION = "999.999.999";
	static final AppManifest APP_MANIFEST = AppManifest.newBuilder()
		.setSchemaName(TENANT_NAME)
		.setChart(CHART_NAME)
		.setVersion(CHART_VERSION)
		.build();
	private static final String TENANT_NAME_KEY = "tenantName";
	private static final String CHART_NAME_KEY = "chartName";
	private static final String CHART_VERSION_KEY = "chartVersion";
	private static final String CONNECTOR_PORT = "8080";
	private static final String CONNECTOR_METHOD = "POST";
	private static final String CONNECTOR_PATH = "/crawl";
	private static final String CONNECTOR_SECURE = "true";
	static final CreatePluginDriverRequest CREATE_PLUGIN_DRIVER_REQUEST =
		CreatePluginDriverRequest.newBuilder()
			.setSchemaName(TENANT_NAME)
			.setName(CONNECTOR_NAME)
			.setHost(CHART_NAME)
			.setPort(CONNECTOR_PORT)
			.setMethod(CONNECTOR_METHOD)
			.setPath(CONNECTOR_PATH)
			.setSecure(CONNECTOR_SECURE)
			.build();
	private static final String CONNECTOR_HOST_KEY = "host";
	private static final String CONNECTOR_PORT_KEY = "port";
	private static final String CONNECTOR_PATH_KEY = "path";
	private static final String CONNECTOR_METHOD_KEY = "method";
	private static final String CONNECTOR_SECURE_KEY = "secure";
	static final String VALID_JSON_BODY = JsonObject.of(
		TENANT_NAME_KEY, TENANT_NAME,
		CHART_NAME_KEY, CHART_NAME,
		CHART_VERSION_KEY, CHART_VERSION,
		CONNECTOR_HOST_KEY, CHART_NAME,
		CONNECTOR_PORT_KEY, CONNECTOR_PORT,
		CONNECTOR_PATH_KEY, CONNECTOR_PATH,
		CONNECTOR_METHOD_KEY, CONNECTOR_METHOD,
		CONNECTOR_SECURE_KEY, CONNECTOR_SECURE
	).toString();

	static final String INVALID_JSON_BODY = JsonObject.of(
		CHART_NAME_KEY, CHART_NAME,
		CONNECTOR_HOST_KEY, CHART_NAME,
		CONNECTOR_PORT_KEY, CONNECTOR_PORT,
		CONNECTOR_METHOD_KEY, CONNECTOR_METHOD,
		CONNECTOR_SECURE_KEY, CONNECTOR_SECURE
	).toString();

}
