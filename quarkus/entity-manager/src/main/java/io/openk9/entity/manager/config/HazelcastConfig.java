package io.openk9.entity.manager.config;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import io.quarkus.runtime.Startup;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

@Startup
@ApplicationScoped
public class HazelcastConfig {

	@PostConstruct
	public void afterCreate() {
		instance = Hazelcast.newHazelcastInstance(Config.load());
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
