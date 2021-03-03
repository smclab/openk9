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

package com.openk9.search.client.internal.metrics;

import com.openk9.metrics.api.MeterRegistryProvider;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.elastic.ElasticMeterRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

import java.util.Map;

@Component(
	immediate = true,
	property = {
		"elastic.host=http://elasticsearch:9200",
		"elastic.autoCreateIndex=false"
	},
	service = MeterRegistryProvider.class
)
public class ElasticSearchMetricRegistry implements MeterRegistryProvider {

	@Activate
	public void activate(Map<String, Object> props) {
		_elasticMeterRegistry = new ElasticMeterRegistry(key -> {
			Object value = props.get(key);
			return value == null ? null : String.valueOf(value);
		}, Clock.SYSTEM);
	}

	@Modified
	public void modified(Map<String, Object> props) {
		_elasticMeterRegistry.close();
		activate(props);
	}

	@Override
	public MeterRegistry getMeterRegistry() {
		return _elasticMeterRegistry;
	}

	private ElasticMeterRegistry _elasticMeterRegistry;

}
