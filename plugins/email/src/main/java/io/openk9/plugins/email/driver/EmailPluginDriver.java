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

package io.openk9.plugins.email.driver;

import io.openk9.http.client.HttpClient;
import io.openk9.http.client.HttpClientFactory;
import io.openk9.http.web.HttpHandler;
import io.openk9.json.api.JsonFactory;
import io.openk9.plugin.driver.manager.api.BasePluginDriver;
import io.openk9.plugin.driver.manager.api.PluginDriver;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
	immediate = true,
	service = PluginDriver.class
)
public class EmailPluginDriver extends BasePluginDriver {

	@interface Config {
		String url() default "http://email-parser:5005/";
		String path() default "/execute";
		int method() default HttpHandler.POST;
		String[] headers() default "Content-Type:application/json";
		boolean schedulerEnabled() default true;
		String[] jsonKeys() default {
			"mailServer", "port", "username", "password", "datasourceId",
			"folder"
		};
	}

	@Activate
	public void activate(Config config) {
		_httpClient = _httpClientFactory.getHttpClient(config.url());
		_config = config;
	}

	@Override
	public String getName() {
		return "email";
	}

	@Override
	public boolean schedulerEnabled() {
		return _config.schedulerEnabled();
	}

	@Override
	protected String[] headers() {
		return _config.headers();
	}

	@Override
	protected String path() {
		return _config.path();
	}

	@Override
	protected int method() {
		return _config.method();
	}

	@Override
	protected String[] jsonKeys() {
		return _config.jsonKeys();
	}

	@Override
	protected JsonFactory getJsonFactory() {
		return _jsonFactory;
	}

	@Override
	protected HttpClient getHttpClient() {
		return _httpClient;
	}

	private Config _config;

	private HttpClient _httpClient;

	@Reference
	private HttpClientFactory _httpClientFactory;

	@Reference
	private JsonFactory _jsonFactory;

}
