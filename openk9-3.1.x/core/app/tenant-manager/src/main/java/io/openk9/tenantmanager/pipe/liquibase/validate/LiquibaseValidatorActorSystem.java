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

package io.openk9.tenantmanager.pipe.liquibase.validate;

import io.openk9.tenantmanager.pipe.liquibase.validate.util.Params;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.ActorSystem;
import org.apache.pekko.actor.typed.javadsl.AskPattern;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.util.Deque;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class LiquibaseValidatorActorSystem {

	@PostConstruct
	void init() {
		_actorSystem = ActorSystem.apply(
			Supervisor.create(), "liquibase-validator-supervisor");
	}

	public Uni<Void> validateSchemas(Deque<Params> paramsList) {
		CompletionStage<Supervisor.Response> completionStage =
			AskPattern.ask(
				_actorSystem,
				(ActorRef<Supervisor.Response> replyTo) ->
					new Supervisor.Start(paramsList, replyTo),
				Duration.ofMinutes(10),
				_actorSystem.scheduler());

		return Uni.createFrom()
			.completionStage(completionStage)
			.invoke(response -> logger.info("response: " + response))
			.replaceWithVoid();

	}

	@Inject
	Logger logger;

	private ActorSystem<Supervisor.Command> _actorSystem;

}
