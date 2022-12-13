package io.openk9.tenantmanager.pipe.tenant.create;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import io.openk9.tenantmanager.model.BackgroundProcess;
import io.openk9.tenantmanager.pipe.tenant.create.message.SchemaMessage;
import io.openk9.tenantmanager.pipe.tenant.create.message.TenantMessage;
import io.openk9.tenantmanager.service.BackgroundProcessService;
import io.openk9.tenantmanager.service.DatasourceLiquibaseService;
import io.openk9.tenantmanager.util.VertxUtil;
import liquibase.exception.LiquibaseException;

import java.util.UUID;

public class SchemaBehavior extends AbstractBehavior<SchemaMessage> {

	public SchemaBehavior(
		ActorContext<SchemaMessage> context,
		ActorRef<TenantMessage> tenantActor,
		DatasourceLiquibaseService liquibaseService,
		BackgroundProcessService backgroundProcessService,
		UUID requestId) {
		super(context);
		this.tenantActor = tenantActor;
		this.liquibaseService = liquibaseService;
		this.backgroundProcessService = backgroundProcessService;
		this.requestId = requestId;
	}

	public static Behavior<SchemaMessage> create(
		DatasourceLiquibaseService liquibaseService,
		ActorRef<TenantMessage> tenantActor,
		BackgroundProcessService backgroundProcessService,
		UUID requestId) {

		return Behaviors.setup(
			context -> new SchemaBehavior(
				context, tenantActor, liquibaseService,
				backgroundProcessService, requestId));
	}

	@Override
	public Receive<SchemaMessage> createReceive() {
		return newReceiveBuilder()
			.onMessage(SchemaMessage.Start.class, this::onStart)
			.onMessage(SchemaMessage.ProcessCreatedId.class, this::onProcessCreatedId)
			.onMessage(SchemaMessage.Rollback.class, this::onRollback)
			.onMessage(SchemaMessage.Stop.class, this::onStop)
			.build();
	}

	private Behavior<SchemaMessage> onProcessCreatedId(
		SchemaMessage.ProcessCreatedId pcid) {

		this.processId = pcid.processId();
		String schemaName = pcid.schemaName();
		String virtualHost = pcid.virtualHost();

		try {
			liquibaseService.runInitialization(schemaName, virtualHost);
			this.schemaName = schemaName;
			getContext().getLog().info("Schema created");
			this.tenantActor.tell(new TenantMessage.SchemaCreated(schemaName));
		}
		catch (LiquibaseException e) {
			tenantActor.tell(new TenantMessage.Error(e));
		}

		return this;

	}

	private Behavior<SchemaMessage> onStop(SchemaMessage.Stop stop) {
		getContext().getLog().info("Schema " + schemaName + " finished");

		VertxUtil.runOnContext(() ->
			backgroundProcessService.updateBackgroundProcess(
				processId, BackgroundProcess.Status.FINISHED,
				"Schema " + schemaName + " created",
				"create-schema")
		);

		return Behaviors.stopped();
	}

	private Behavior<SchemaMessage> onRollback(SchemaMessage.Rollback rollback) {
		if (schemaName != null) {
			liquibaseService.rollbackRunLiquibaseMigration(schemaName);
			getContext().getLog().warn("Rollback schema: " + schemaName);
			VertxUtil.runOnContext(() ->
				backgroundProcessService.updateBackgroundProcess(
					processId, BackgroundProcess.Status.ROOLBACK,
					"Schema " + schemaName + " rollbacked",
					"create-schema")
			);
		}
		return Behaviors.stopped();
	}

	private Behavior<SchemaMessage> onStart(SchemaMessage.Start createSchema) {

		VertxUtil.runOnContext(() ->
			backgroundProcessService.createBackgroundProcess(
					BackgroundProcess
						.builder()
						.processId(requestId)
						.name("create-schema")
						.message("Starting create schema: " + createSchema.schemaName())
						.status(BackgroundProcess.Status.IN_PROGRESS)
						.build()
				)
				.invoke(bp ->
					getContext()
						.getSelf()
						.tell(
							new SchemaMessage.ProcessCreatedId(
								bp.getId(),
								createSchema.virtualHost(),
								createSchema.schemaName(),
								createSchema.tenant()
							)
						)
				)
		);

		return this;

	}

	private final ActorRef<TenantMessage> tenantActor;
	private final DatasourceLiquibaseService liquibaseService;
	private final BackgroundProcessService backgroundProcessService;
	private String schemaName;
	private Long processId;
	private final UUID requestId;

}
