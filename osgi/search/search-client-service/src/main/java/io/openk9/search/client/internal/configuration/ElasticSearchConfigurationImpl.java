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

package io.openk9.search.client.internal.configuration;

import io.openk9.search.client.api.configuration.ElasticSearchConfiguration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

@Component(
	immediate = true,
	service = ElasticSearchConfiguration.class
)
public class ElasticSearchConfigurationImpl implements ElasticSearchConfiguration {

	@interface Config {
		String dataIndex() default "data";
		String entityIndex() default "entity";
		String[] hosts() default {"localhost:9200"};
		int bufferMaxSize() default 100;
		long bufferMaxTime() default 1_000;
	}

	@Activate
	public void activate(Config config) {
		_config = config;
	}

	@Modified
	public void modified(Config config) {
		_config = config;
	}

	public String getDataIndex() {
		return _config.dataIndex();
	}

	public String getEntityIndex() {
		return _config.entityIndex();
	}

	public String[] hosts() {
		return _config.hosts();
	}

	public int bufferMaxSize() {
		return _config.bufferMaxSize();
	}

	public long bufferMaxTime() {
		return _config.bufferMaxTime();
	}

	private Config _config;

}
