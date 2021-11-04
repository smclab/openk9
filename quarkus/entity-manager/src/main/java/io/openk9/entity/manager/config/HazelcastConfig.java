package io.openk9.entity.manager.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import io.openk9.entity.manager.loader.EntityMapStore;
import io.quarkus.runtime.Startup;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

@Startup
@ApplicationScoped
public class HazelcastConfig {

	@PostConstruct
	public void afterCreate() {

		Config config = Config.load();

		MapConfig mapConfig = new MapConfig("entityMap");

		MapStoreConfig mapStoreConfig = new MapStoreConfig();

		mapStoreConfig.setEnabled(true);
		mapStoreConfig.setImplementation(new EntityMapStore());

		mapConfig.setMapStoreConfig(mapStoreConfig);

		config.addMapConfig(mapConfig);

		instance = Hazelcast.newHazelcastInstance(config);
	}

	@PreDestroy
	public void beforeDestroy() {
		if(instance != null) {
			instance.shutdown();
		}
	}

	@Produces
	@ApplicationScoped
	public HazelcastInstance getInstance() {
		return instance;
	}

	private HazelcastInstance instance;

}
