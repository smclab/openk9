package io.openk9.tenantmanager.pipe.tenant.create;

import io.openk9.tenantmanager.actor.TypedActor;
import io.openk9.tenantmanager.pipe.tenant.create.message.SchemaMessage;
import io.openk9.tenantmanager.pipe.tenant.create.message.TenantMessage;
import io.openk9.tenantmanager.service.DatasourceLiquibaseService;
import liquibase.exception.LiquibaseException;
import org.jboss.logging.Logger;

public class SchemaBehavior implements TypedActor.Behavior<SchemaMessage> {

	public SchemaBehavior(
		TypedActor.Address<TenantMessage> tenantActor,
		DatasourceLiquibaseService liquibaseService) {
		this.tenantActor = tenantActor;
		this.liquibaseService = liquibaseService;
	}

	@Override
	public TypedActor.Effect<SchemaMessage> apply(SchemaMessage message) {
		if (message instanceof SchemaMessage.Start) {

			SchemaMessage.Start createSchema =(SchemaMessage.Start)message;

			String schemaName = createSchema.schemaName();
			String virtualHost = createSchema.virtualHost();

			try {
				liquibaseService.runInitialization(schemaName, virtualHost);
				this.schemaName = schemaName;
				createSchema
					.tenant()
					.tell(new TenantMessage.SchemaCreated(schemaName));
			}
			catch (LiquibaseException e) {
				tenantActor.tell(new TenantMessage.Error(e));
			}

		}
		else if (message instanceof SchemaMessage.Rollback) {
			if (schemaName != null) {
				liquibaseService.rollbackRunLiquibaseMigration(schemaName);
				LOGGER.warn("Rollback schema: " + schemaName);
			}
			return TypedActor.Die();
		}
		else if (message instanceof SchemaMessage.Stop) {
			LOGGER.info("Schema " + schemaName + " finished");
			return TypedActor.Die();
		}

		return TypedActor.Stay();

 	}

	private final TypedActor.Address<TenantMessage> tenantActor;
	private final DatasourceLiquibaseService liquibaseService;
	private String schemaName;
	private static final Logger LOGGER = Logger.getLogger(SchemaBehavior.class);

}
