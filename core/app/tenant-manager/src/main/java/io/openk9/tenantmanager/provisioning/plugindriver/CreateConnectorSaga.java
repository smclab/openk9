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

package io.openk9.tenantmanager.provisioning.plugindriver;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import io.openk9.app.manager.grpc.AppManager;
import io.openk9.app.manager.grpc.AppManifest;
import io.openk9.datasource.grpc.CreatePluginDriverRequest;
import io.openk9.datasource.grpc.Datasource;
import org.jboss.logging.Logger;

public class CreateConnectorSaga extends AbstractBehavior<CreateConnectorSaga.Command> {

	private static final Logger log = Logger.getLogger(CreateConnectorSaga.class);
	private final ActorRef<Operator.Response> operatorAdapter;
	private final ActorRef<Persistence.Response> persistenceAdapter;
	private final ActorRef<Operator.Command> operator;
	private final ActorRef<Persistence.Command> persistence;
	private ActorRef<Responses> replyTo;

	public CreateConnectorSaga(
		ActorContext<Command> context,
		AppManager operatorClient,
		Datasource persistenceClient,
		AppManifest operatorRequest,
		CreatePluginDriverRequest persistenceRequest) {

		this(
			context,
			context.spawnAnonymous(Operator.create(operatorClient, operatorRequest)),
			context.spawnAnonymous(Persistence.create(persistenceClient, persistenceRequest))
		);

	}

	public CreateConnectorSaga(
		ActorContext<Command> context,
		ActorRef<Operator.Command> operator,
		ActorRef<Persistence.Command> persistence) {

		super(context);

		this.operator = operator;
		this.persistence = persistence;

		this.operatorAdapter = context.messageAdapter(
			Operator.Response.class,
			response -> switch (response) {
				case SUCCESS -> States.INSTALLED;
				case ERROR -> States.NOT_INSTALLED;
				case COMPENSATED -> States.UNINSTALLED;
				case NOT_COMPENSATED -> States.NOT_UNISTALLED;
			}
		);

		this.persistenceAdapter = context.messageAdapter(
			Persistence.Response.class,
			response -> switch (response) {
				case SUCCESS -> States.PERSISTED;
				case ERROR -> States.NOT_PERSISTED;
			}
		);
	}

	public static Behavior<Command> create(
		ActorRef<Operator.Command> operator,
		ActorRef<Persistence.Command> persistence) {

		return Behaviors.setup(ctx -> new CreateConnectorSaga(ctx, operator, persistence));
	}

	public static Behavior<Command> create(
		AppManager opsClient,
		AppManifest opsRequest,
		Datasource persistenceClient,
		CreatePluginDriverRequest persistenceRequest) {

		return Behaviors.setup(ctx -> new CreateConnectorSaga(
			ctx,
			opsClient,
			persistenceClient,
			opsRequest,
			persistenceRequest
		));
	}

	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder().onMessage(Exec.class, this::onStart).build();
	}

	private Behavior<Command> onStart(Exec exec) {

		this.replyTo = exec.replyTo;

		operator.tell(new Operator.Install(operatorAdapter));

		return newReceiveBuilder()
			.onMessageEquals(States.INSTALLED, this::onInstalled)
			.onMessageEquals(States.NOT_INSTALLED, this::onNotInstalled)
			.build();
	}

	private Behavior<Command> onInstalled() {

		persistence.tell(new Persistence.Persist(persistenceAdapter));

		return newReceiveBuilder()
			.onMessageEquals(States.PERSISTED, this::onPersisted)
			.onMessageEquals(States.NOT_PERSISTED, this::onNotPersisted)
			.build();
	}

	private Behavior<Command> onPersisted() {
		replyTo.tell(Responses.SUCCESS);
		return Behaviors.stopped();
	}

	private Behavior<Command> onNotInstalled() {
		log.warn("CreateConnectorSaga failed: NOT_INSTALLED, no operation required.");
		replyTo.tell(Responses.ERROR);
		return Behaviors.stopped();
	}

	private Behavior<Command> onNotPersisted() {
		log.warn("CreateConnectorSaga failed: NOT_PERSISTED, compensating operation...");

		operator.tell(new Operator.Compensate(operatorAdapter));

		return newReceiveBuilder()
			.onMessageEquals(States.UNINSTALLED, this::onUninstalled)
			.onMessageEquals(States.NOT_UNISTALLED, this::onNotUninstalled)
			.build();
	}

	private Behavior<Command> onNotUninstalled() {
		log.warn("CreateConnectorSaga failed: NOT_UNINSTALLED, manual operation required.");
		replyTo.tell(Responses.COMPENSATION_ERROR);
		return Behaviors.stopped();
	}

	private Behavior<Command> onUninstalled() {
		log.info("CreateConnectorSaga compensated.");
		replyTo.tell(Responses.COMPENSATION);
		return Behaviors.stopped();
	}

	private enum States implements Command {
		INSTALLED,
		NOT_INSTALLED,
		PERSISTED,
		NOT_PERSISTED,
		UNINSTALLED,
		NOT_UNISTALLED
	}

	public enum Responses implements Response {
		SUCCESS,
		COMPENSATION,
		COMPENSATION_ERROR,
		ERROR
	}

	public sealed interface Command {}

	public sealed interface Response {}

	public record Exec(ActorRef<CreateConnectorSaga.Responses> replyTo) implements Command {}

}
