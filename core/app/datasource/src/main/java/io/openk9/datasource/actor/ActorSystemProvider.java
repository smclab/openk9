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

import akka.actor.typed.ActorSystem;
import com.typesafe.config.ConfigFactory;
import io.quarkus.runtime.Startup;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

@ApplicationScoped
@Startup
public class ActorSystemProvider {

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

	}

	@PreDestroy
	void destroy() {
		actorSystem.terminate();
	}

	public ActorSystem<?> getActorSystem() {
		return actorSystem;
	}

	private ActorSystem<?> actorSystem;

	@Inject
	Instance<ActorSystemInitializer> actorSystemInitializerInstance;
	@Inject
	Instance<ActorSystemBehaviorInitializer> actorSystemBehaviorInitializerInstance;


	@ConfigProperty(name = "akka.cluster.file")
	String clusterFile;

}
