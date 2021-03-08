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

package io.openk9.plugins.email.enrichprocessor;

import io.openk9.http.client.HttpClientFactory;
import io.openk9.http.web.HttpHandler;
import io.openk9.json.api.JsonFactory;
import io.openk9.search.client.api.Index;
import io.openk9.search.client.api.Search;
import io.openk9.search.enrich.api.BaseNerEnrichProcessor;
import io.openk9.search.enrich.api.EnrichProcessor;
import io.openk9.search.enrich.mapper.api.EntityMapperProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Component(immediate = true, service = EnrichProcessor.class)
public class EmailNerEnrichProcessor extends BaseNerEnrichProcessor {

	@interface Config {
		String url() default "http://ner-email:5002/";
		String path() default "/predict";
		int method() default HttpHandler.POST;
		String[] headers() default "Content-Type:application/json";
	}

	@Activate
	public void activate(Config config) {
		_config = config;
		setHttpClient(_httpClientFactory.getHttpClient(config.url()));
		setEntityMapperProvider(_entityMapperProvider);
		setIndex(_index);
		setSearch(_search);
		setJsonFactory(_jsonFactory);
	}

	@Modified
	public void modified(Config config) {
		activate(config);
	}

	@Override
	public String name() {
		return EmailNerEnrichProcessor.class.getName();
	}

	@Override
	protected Map<String, Object> getHeaders() {
		return Arrays
			.stream(_config.headers())
			.map(s -> s.split(":"))
			.collect(Collectors.toMap(e -> e[0], e -> e[1]));
	}

	@Override
	protected int getMethod() {
		return _config.method();
	}

	@Override
	protected String getPath() {
		return _config.path();
	}

	private Config _config;

	@Reference
	private HttpClientFactory _httpClientFactory;

	@Reference
	private JsonFactory _jsonFactory;

	@Reference
	private Search _search;

	@Reference
	private Index _index;

	@Reference
	private EntityMapperProvider _entityMapperProvider;

}
