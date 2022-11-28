package io.openk9.tenantmanager.pipe;

import io.openk9.tenantmanager.actor.TypedActor;
import io.openk9.tenantmanager.model.BackgroundProcess;
import io.openk9.tenantmanager.service.BackgroundProcessService;
import io.openk9.tenantmanager.service.LiquibaseService;
import io.openk9.tenantmanager.util.VertxUtil;
import io.quarkus.runtime.util.ExceptionUtil;
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

			try {
				liquibaseService.runLiquibaseMigration(schemaName);
				this.schemaName = schemaName;
				createSchema
					.next()
					.tell(new TenantMessage.SchemaCreated(schemaName));
			}
			catch (LiquibaseException e) {

				VertxUtil.runOnContext(() ->
					backgroundProcessService.updateBackgroundProcessStatus(
						requestId, BackgroundProcess.Status.FAILED,
						ExceptionUtil.generateStackTrace(e)
					)
						.invoke(() -> tenantActor.tell(new TenantMessage.Error(e)))
				);

			}

		}
		else if (message instanceof TenantMessage.SimpleError) {
			if (schemaName != null) {
				liquibaseService.rollbackRunLiquibaseMigration(schemaName);
			}
			return TypedActor.Die();
		}
		else if (message instanceof TenantMessage.Finished) {
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
