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

package io.openk9.datasource.actor;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import io.openk9.datasource.pipeline.service.EmbeddingStubRegistry;

import com.typesafe.config.ConfigFactory;
import io.quarkus.runtime.Startup;
import io.vertx.mutiny.core.eventbus.EventBus;
import lombok.Getter;
import org.apache.pekko.actor.typed.ActorSystem;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Startup
public class ActorSystemProvider {

	public static final String INITIALIZED =
		"io.openk9.datasource.actor.ActorSystemProvider#INITIALIZED";
	@Inject
	Instance<ActorSystemInitializer> actorSystemInitializerInstance;
	@Inject
	Instance<ActorSystemBehaviorInitializer> actorSystemBehaviorInitializerInstance;
	@Inject
	EventBus eventBus;
	@ConfigProperty(name = "pekko.cluster.file")
	String clusterFile;
	@Getter
	private ActorSystem<?> actorSystem;

	@PostConstruct
	void init() {
		var clusterConfig = ConfigFactory.load(clusterFile);
		var application = ConfigFactory.load();

		var combined = ConfigFactory
			.parseApplicationReplacement()
			.map(external -> external.withFallback(clusterConfig))
			.orElse(clusterConfig)
			.withFallback(application);

		var complete = ConfigFactory.load(combined);

		actorSystem = ActorSystem.create(
			Initializer.create(actorSystemBehaviorInitializerInstance),
			"datasource",
			complete
		);

		for (ActorSystemInitializer actorSystemInitializer : actorSystemInitializerInstance) {
			actorSystemInitializer.init(actorSystem);
		}

		EventBusInstanceHolder.setEventBus(eventBus);

		eventBus.send(INITIALIZED, INITIALIZED);
	}

	@PreDestroy
	void destroy() {
		EmbeddingStubRegistry.clear();
		actorSystem.terminate();
	}

}
