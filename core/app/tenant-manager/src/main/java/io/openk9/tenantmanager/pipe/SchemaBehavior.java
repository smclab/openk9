package io.openk9.tenantmanager.pipe;

import io.openk9.tenantmanager.actor.TypedActor;
import io.openk9.tenantmanager.service.BackgroundProcessService;
import io.openk9.tenantmanager.service.LiquibaseService;
import liquibase.exception.LiquibaseException;
import org.jboss.logging.Logger;

import java.util.UUID;

public class SchemaBehavior implements TypedActor.Behavior<TenantMessage> {

	public SchemaBehavior(
		UUID requestId, TypedActor.Address<TenantMessage> tenantActor,
		LiquibaseService liquibaseService,
		BackgroundProcessService backgroundProcessService) {
		this.requestId = requestId;
		this.tenantActor = tenantActor;
		this.liquibaseService = liquibaseService;
		this.backgroundProcessService = backgroundProcessService;
	}

	@Override
	public TypedActor.Effect<TenantMessage> apply(TenantMessage message) {
		if (message instanceof TenantMessage.CreateSchema) {

			TenantMessage.CreateSchema createSchema = (TenantMessage.CreateSchema)message;

			String schemaName = createSchema.schemaName();
			String virtualHost = createSchema.virtualHost();

			try {
				liquibaseService.runLiquibaseMigration(schemaName, virtualHost);
				this.schemaName = schemaName;
				createSchema
					.next()
					.tell(new TenantMessage.SchemaCreated(schemaName));
			}
			catch (LiquibaseException e) {
				tenantActor.tell(new TenantMessage.Error(e));
			}

		}
		else if (message instanceof TenantMessage.SimpleError) {
			if (schemaName != null) {
				liquibaseService.rollbackRunLiquibaseMigration(schemaName);
				LOGGER.warn("Rollback schema: " + schemaName);
			}
			return TypedActor.Die();
		}
		else if (message instanceof TenantMessage.Finished) {
			LOGGER.info("Schema " + schemaName + " finished");
			return TypedActor.Die();
		}

		return TypedActor.Stay();

 	}

	private final UUID requestId;
	private final TypedActor.Address<TenantMessage> tenantActor;
	private final LiquibaseService liquibaseService;
	private final BackgroundProcessService backgroundProcessService;
	private String schemaName;

	private static final Logger LOGGER = Logger.getLogger(SchemaBehavior.class);

}
