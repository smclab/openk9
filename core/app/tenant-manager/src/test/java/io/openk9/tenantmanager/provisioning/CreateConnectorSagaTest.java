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

package io.openk9.tenantmanager.provisioning;


import akka.actor.testkit.typed.javadsl.ActorTestKit;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.ReceiveBuilder;
import akka.japi.Pair;
import io.openk9.tenantmanager.provisioning.plugindriver.CreateConnectorSaga;
import io.openk9.tenantmanager.provisioning.plugindriver.Operator;
import io.openk9.tenantmanager.provisioning.plugindriver.Persistence;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

public class CreateConnectorSagaTest {

	private static final ActorTestKit testKit = ActorTestKit.create();

	@AfterAll
	public static void cleanup() {
		testKit.shutdownTestKit();
	}

	@Test
	void should_install_ok_and_persist_ok_success() {
		var mockOperator = mockOperator();
		var mockPersistence = mockPersistence();

		var saga = testKit.spawn(CreateConnectorSaga.create(
			mockOperator.first(),
			mockPersistence.first()
		));

		var sagaProbe = testKit.createTestProbe(CreateConnectorSaga.Response.class);

		saga.tell(new CreateConnectorSaga.Exec(sagaProbe.ref()));

		var operatorProbe = mockOperator.second();
		var persistenceProbe = mockPersistence.second();

		operatorProbe.expectMessageClass(Operator.Install.class);
		persistenceProbe.expectMessageClass(Persistence.Persist.class);
		sagaProbe.expectMessage(CreateConnectorSaga.Responses.SUCCESS);

		operatorProbe.expectNoMessage();
		persistenceProbe.expectNoMessage();
		sagaProbe.expectNoMessage();
	}

	@Test
	void should_install_ok_and_persist_ko_rollback_and_compesate() {
		var mockOperator = mockOperator();
		var mockPersistence = mockPersistenceFailure();

		var saga = testKit.spawn(CreateConnectorSaga.create(
			mockOperator.first(),
			mockPersistence.first()
		));

		var sagaProbe = testKit.createTestProbe(CreateConnectorSaga.Response.class);

		saga.tell(new CreateConnectorSaga.Exec(sagaProbe.ref()));

		var operatorProbe = mockOperator.second();
		var persistenceProbe = mockPersistence.second();

		operatorProbe.expectMessageClass(Operator.Install.class);
		persistenceProbe.expectMessageClass(Persistence.Persist.class);
		operatorProbe.expectMessageClass(Operator.Compensate.class);
		sagaProbe.expectMessage(CreateConnectorSaga.Responses.COMPENSATION);

		operatorProbe.expectNoMessage();
		persistenceProbe.expectNoMessage();
		sagaProbe.expectNoMessage();
	}

	@Test
	void should_install_ok_and_persist_ko_rollback_and_not_compesate() {
		var mockOperator = mockOperatorCompensateFailure();
		var mockPersistence = mockPersistenceFailure();

		var saga = testKit.spawn(CreateConnectorSaga.create(
			mockOperator.first(),
			mockPersistence.first()
		));

		var sagaProbe = testKit.createTestProbe(CreateConnectorSaga.Response.class);

		saga.tell(new CreateConnectorSaga.Exec(sagaProbe.ref()));

		var operatorProbe = mockOperator.second();
		var persistenceProbe = mockPersistence.second();

		operatorProbe.expectMessageClass(Operator.Install.class);
		persistenceProbe.expectMessageClass(Persistence.Persist.class);
		operatorProbe.expectMessageClass(Operator.Compensate.class);
		sagaProbe.expectMessage(CreateConnectorSaga.Responses.COMPENSATION_ERROR);

		operatorProbe.expectNoMessage();
		persistenceProbe.expectNoMessage();
		sagaProbe.expectNoMessage();
	}

	@Test
	void should_install_ko_and_cancel_saga() {
		var mockOperator = mockOperatorFailure();
		var mockPersistence = mockPersistence();

		var saga = testKit.spawn(CreateConnectorSaga.create(
			mockOperator.first(),
			mockPersistence.first()
		));

		var sagaProbe = testKit.createTestProbe(CreateConnectorSaga.Response.class);

		saga.tell(new CreateConnectorSaga.Exec(sagaProbe.ref()));

		var operatorProbe = mockOperator.second();
		var persistenceProbe = mockPersistence.second();

		operatorProbe.expectMessageClass(Operator.Install.class);
		sagaProbe.expectMessage(CreateConnectorSaga.Responses.ERROR);

		operatorProbe.expectNoMessage();
		persistenceProbe.expectNoMessage();
		sagaProbe.expectNoMessage();
	}

	private static Pair<ActorRef<Persistence.Command>, TestProbe<Persistence.Command>> mockPersistenceFailure() {
		var probe = testKit.createTestProbe(Persistence.Command.class);
		var mock = testKit.spawn(Behaviors.monitor(
			Persistence.Command.class,
			probe.ref(),
			ReceiveBuilder.<Persistence.Command>create()
				.onMessage(Persistence.Persist.class, (persist) -> {
					persist.replyTo().tell(Persistence.Response.ERROR);
					return Behaviors.same();
				})
				.build()
		));

		return Pair.create(mock, probe);
	}

	private static Pair<ActorRef<Persistence.Command>, TestProbe<Persistence.Command>> mockPersistence() {
		var probe = testKit.createTestProbe(Persistence.Command.class);
		var mock = testKit.spawn(Behaviors.monitor(
			Persistence.Command.class,
			probe.ref(),
			ReceiveBuilder.<Persistence.Command>create()
				.onMessage(Persistence.Persist.class, (persist) -> {
					persist.replyTo().tell(Persistence.Response.SUCCESS);
					return Behaviors.same();
				})
				.build()
		));

		return Pair.create(mock, probe);
	}

	private static Pair<ActorRef<Operator.Command>, TestProbe<Operator.Command>> mockOperatorCompensateFailure() {
		var probe = testKit.createTestProbe(Operator.Command.class);
		var mock = testKit.spawn(Behaviors.monitor(
			Operator.Command.class,
			probe.ref(),
			ReceiveBuilder.<Operator.Command>create()
				.onMessage(Operator.Install.class, (install) -> {
					install.replyTo().tell(Operator.Response.SUCCESS);
					return Behaviors.same();
				})
				.onMessage(Operator.Compensate.class, (compensate) -> {
					compensate.replyTo().tell(Operator.Response.NOT_COMPENSATED);
					return Behaviors.same();
				})
				.build()
		));

		return Pair.create(mock, probe);
	}

	private static Pair<ActorRef<Operator.Command>, TestProbe<Operator.Command>> mockOperatorFailure() {
		var probe = testKit.createTestProbe(Operator.Command.class);
		var mock = testKit.spawn(Behaviors.monitor(
			Operator.Command.class,
			probe.ref(),
			ReceiveBuilder.<Operator.Command>create()
				.onMessage(Operator.Install.class, (install) -> {
					install.replyTo().tell(Operator.Response.ERROR);
					return Behaviors.same();
				})
				.build()
		));

		return Pair.create(mock, probe);
	}

	private static Pair<ActorRef<Operator.Command>, TestProbe<Operator.Command>> mockOperator() {
		var probe = testKit.createTestProbe(Operator.Command.class);
		var mock = testKit.spawn(Behaviors.monitor(
			Operator.Command.class,
			probe.ref(),
			ReceiveBuilder.<Operator.Command>create()
				.onMessage(Operator.Install.class, (install) -> {
					install.replyTo().tell(Operator.Response.SUCCESS);
					return Behaviors.same();
				})
				.onMessage(Operator.Compensate.class, (compensate) -> {
					compensate.replyTo().tell(Operator.Response.COMPENSATED);
					return Behaviors.same();
				})
				.build()
		));

		return Pair.create(mock, probe);
	}

}
