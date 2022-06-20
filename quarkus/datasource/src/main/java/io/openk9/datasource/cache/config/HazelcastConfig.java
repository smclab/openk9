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

package io.openk9.datasource.cache.config;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import io.openk9.datasource.cache.EventMapStore;
import io.openk9.datasource.cache.annotation.MapName;
import io.quarkus.runtime.Startup;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import java.lang.annotation.Annotation;

@Startup
@ApplicationScoped
public class HazelcastConfig {

	@PostConstruct
	public void afterCreate() {
		Config config = Config.load();

		config.getMapConfig("eventMap")
			.getMapStoreConfig()
			.setEnabled(true)
			.setImplementation(eventMapStore);

		instance = Hazelcast.newHazelcastInstance(config);

	}

	@PreDestroy
	public void beforeDestroy() {
		if(instance != null) {
			instance.shutdown();
		}
	}

	@Produces
	public HazelcastInstance getInstance() {
		return instance;
	}

	@Produces
	@MapName("")
	public <K, V> IMap<K, V> getMap(InjectionPoint injectionPoint) {

		for (Annotation qualifier : injectionPoint.getQualifiers()) {
			if (qualifier instanceof MapName) {
				return instance.getMap(((MapName)qualifier).value());
			}
		}
		// This will never be returned.
		return null;
	}

	private HazelcastInstance instance;

	@Inject
	EventMapStore eventMapStore;

}
