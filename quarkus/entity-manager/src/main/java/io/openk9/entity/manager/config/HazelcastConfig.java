package io.openk9.entity.manager.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import io.openk9.entity.manager.loader.EntityMapStore;
import io.openk9.entity.manager.service.EntityService;
import io.quarkus.arc.Unremovable;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@ApplicationScoped
public class HazelcastConfig {

	@Produces
	@Unremovable
	HazelcastInstance createInstance() {

		Config config = Config.load();

		MapConfig mapConfig = new MapConfig("entityMap");

		MapStoreConfig mapStoreConfig = new MapStoreConfig();

		mapStoreConfig.setEnabled(true);
		mapStoreConfig.setImplementation(new EntityMapStore(_entityService));

		mapConfig.setMapStoreConfig(mapStoreConfig);

		config.addMapConfig(mapConfig);

		return Hazelcast.newHazelcastInstance(config);
	}

	@Inject
	EntityService _entityService;

}
